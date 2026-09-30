package com.itszuvalex.femtocraft.power.menu

import com.itszuvalex.femtocraft.FemtoMenus
import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.power.tile.CrystalMountBlockEntity
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.entity.player.Inventory

/**
 * One crystal slot. Port of 1.7.10 `ContainerCrystalMount`.
 */
class CrystalMountMenu(containerId: Int, inventory: Inventory, val mount: CrystalMountBlockEntity?) :
    FemtoMenu(FemtoMenus.CRYSTAL_MOUNT.get(), containerId, inventory, mount) {

    constructor(containerId: Int, inventory: Inventory, buf: RegistryFriendlyByteBuf) :
        this(containerId, inventory, readBlockEntity<CrystalMountBlockEntity>(inventory, buf))

    init {
        mount?.let { addStorageSlot(it.inventory, 0, 80, 34) }
        addPlayerInventory()
    }
}
