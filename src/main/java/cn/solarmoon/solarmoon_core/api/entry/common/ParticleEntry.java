package cn.solarmoon.solarmoon_core.api.entry.common;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

@SuppressWarnings("unchecked")
public class ParticleEntry<P extends ParticleOptions> {

    private final DeferredRegister<ParticleType<?>> particleRegister;
    private String id;
    private Supplier<ParticleType<? extends P>> particleSupplier;
    private ParticleEngine.SpriteParticleRegistration<?> provider;
    private RegistryObject<ParticleType<? extends P>> particleTypeObject;

    public ParticleEntry(DeferredRegister<ParticleType<?>> particleRegister) {
        this.particleRegister = particleRegister;
    }

    public ParticleEntry<P> id(String id) {
        this.id = id;
        return this;
    }

    public ParticleEntry<P> bound(Supplier<ParticleType<? extends P>> particleSupplier) {
        this.particleSupplier = particleSupplier;
        return this;
    }

    public <T extends ParticleOptions> ParticleEntry<P> provider(ParticleEngine.SpriteParticleRegistration<T> provider) {
        this.provider = provider;
        return this;
    }

    public <T extends ParticleOptions> ParticleEntry<T> build() {
        this.particleTypeObject = particleRegister.register(id, particleSupplier);
        if (FMLEnvironment.dist.isClient()) {
            IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
            bus.addListener(this::registerParticleProviders);
        }
        return (ParticleEntry<T>) this;
    }

    public void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(getR(), provider);
    }

    public P get() {
        return (P) particleTypeObject.get();
    }

    private <T extends ParticleOptions> ParticleType<T> getR() {
        return (ParticleType<T>) particleTypeObject.get();
    }

}
