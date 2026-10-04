package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.menu.DistributionView
import com.itszuvalex.itszulib.menu.MenuCore
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * The power tab's data: the wireless and wired networks a block with power is on (for a multiblock, any of its loaded
 * parts'), with each network's block count. Synced by [FemtoMenu] for every block with power. Port of v3's
 * `ContainerWirelessPowerNetwork` (a tab of every power block's GUI), with wired networks added.
 */
class PowerNetworksView(
    @JvmField val wireless: DistributionView,
    @JvmField val wired: DistributionView,
    /** Whether the block can join a wireless network (a node or leaf) and a wired one (a wired leaf). */
    @JvmField val canWireless: Boolean,
    @JvmField val canWired: Boolean,
) {
    companion object {
        private fun canWireless(be: BlockEntity?): Boolean {
            val ibe = be as? IBlockEntity ?: return false
            return ibe.getModule(PowerModules.WIRELESS_NODE, null) != null || ibe.getModule(PowerModules.WIRELESS_LEAF, null) != null
        }

        private fun canWired(be: BlockEntity?): Boolean = (be as? IBlockEntity)?.getModule(PowerModules.WIRED_LEAF, null) != null

        /**
         * True if [be] takes part in power networks: a wireless node or leaf, or a wired leaf.
         */
        @JvmStatic
        fun hasPower(be: BlockEntity?): Boolean = canWireless(be) || canWired(be)

        @JvmStatic
        fun addTo(menu: MenuCore, be: BlockEntity): PowerNetworksView {
            fun wireless() = powerBlocks(be).firstNotNullOfOrNull(::wirelessNetworkOf)
            fun wired() = powerBlocks(be).firstNotNullOfOrNull(::wiredNetworkOf)
            return PowerNetworksView(
                menu.syncDistribution({ wireless()?.statistics }, { wireless()?.size() ?: 0 }),
                menu.syncDistribution({ wired()?.statistics }, { wired()?.size() ?: 0 }),
                canWireless(be),
                canWired(be),
            )
        }

        /**
         * [be], or every loaded part of the multiblock it is a formed part of (home first).
         */
        private fun powerBlocks(be: BlockEntity): List<BlockEntity> {
            val membership = (be as? IBlockEntity)?.getModule(Modules.MULTIBLOCK_MEMBER, null)?.membership ?: return listOf(be)
            val level = be.level ?: return listOf(be)
            val anchor = be.blockPos.subtract(membership.offset)
            return membership.shape.positions(anchor).sortedBy { it != anchor }
                .mapNotNull { if (level.isLoaded(it)) level.getBlockEntity(it) else null }
        }
    }
}

/**
 * Crystal mount: its crystal slot (the network statistics are in the power tab, [PowerNetworksView]). Port of v3's `ContainerCrystalMount`.
 */
class CrystalMountMenu(containerId: Int, inventory: Inventory, be: CrystalMountBlockEntity?) :
    FemtoMenu<CrystalMountBlockEntity>(PowerContent.CRYSTAL_MOUNT_MENU.get(), containerId, inventory, be) {
    init {
        addStorageSlots(be?.storage ?: IItemStorage.Empty, 80, 35, count = if (be == null) 0 else 1, hint = net.minecraft.world.item.ItemStack(PowerContent.POWER_CRYSTAL.get()))
        addPlayerInventorySlots(inventory)
    }
}

/**
 * Charging array, storage array and heat exchanger: their crystal (and fuel) slots, battery and generation (the network
 * statistics are in the power tab); the heat exchanger also syncs its burn time. Port of v3's `ContainerCrystalChargingArray`,
 * `ContainerCrystalStorageArray` and `ContainerCrystalHeatExchanger`.
 */
class CrystalMachineMenu(containerId: Int, inventory: Inventory, be: CrystalMachineBlockEntity?) :
    FemtoMenu<CrystalMachineBlockEntity>(PowerContent.CRYSTAL_MACHINE_MENU.get(), containerId, inventory, be) {
    @JvmField
    var battery = com.itszuvalex.itszulib.menu.EnergyView()

    var powerPerTick = 0.0
    var burnTime = 0
    var burnMax = 0

    init {
        val storage = be?.storage ?: IItemStorage.Empty
        addStorageSlots(storage, SLOTS_X, SLOTS_Y, columns = 3)
        addPlayerInventorySlots(inventory)
        if (be != null) {
            battery = syncEnergy { be.battery }
            addSync(MenuSyncs.double(be::powerPerTick) { powerPerTick = it })
            if (be is CrystalHeatExchangerBlockEntity) {
                addSync(MenuSyncs.int({ be.burnTime }, { burnTime = it }))
                addSync(MenuSyncs.int({ be.burnMax }, { burnMax = it }))
            }
        }
    }

    companion object {
        const val SLOTS_X = 62
        const val SLOTS_Y = 26
    }
}
