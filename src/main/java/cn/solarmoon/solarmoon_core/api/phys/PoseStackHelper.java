package cn.solarmoon.solarmoon_core.api.phys;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.core.Direction;

public class PoseStackHelper {

    public static void rotateByDirection(Direction direction, PoseStack poseStack) {
        poseStack.translate(0.5f, 0, 0.5f);
        poseStack.mulPose(Axis.YN.rotationDegrees(direction.toYRot() - 180));
        poseStack.translate(-0.5f, 0, -0.5f);
    }

}
