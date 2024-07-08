package cn.solarmoon.solarmoon_core.api.tile.inventory;

import net.minecraftforge.items.ItemStackHandler;

/**
 * 具有物品容器信息的方块实体<br/>
 * 接入后能够实现save - load容器信息、 附加forgeItemHandler能力，以及一些实用方法<br/>
 * 需手动实现inventory逻辑，一般直接新建一个ItemStackHandler即可
 */
public interface IContainerTile {

    ItemStackHandler getInventory();

}
