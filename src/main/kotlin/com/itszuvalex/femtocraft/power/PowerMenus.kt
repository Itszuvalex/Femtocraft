package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.menu.MenuCore
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * Client copy of a wireless network's statistics, synced by [PowerNetworkView.addTo]. Port of v3's
 * `ContainerWirelessPowerNetwork` (shown in a tab of every power block's GUI).
 */
class PowerNetworkView {
    var connected = false
    var producers = 0
    var storage = 0
    var consumers = 0
    var produced = 0.0
    var consumed = 0.0
    var storageDelta = 0.0
    var averageTrend = 0.0
    var dedicatedStored = 0.0
    var dedicatedStorage = 0.0
    var totalStored = 0.0
    var totalStorage = 0.0

    companion object {
        fun addTo(menu: MenuCore, view: PowerNetworkView, be: BlockEntity?) {
            fun stats() = be?.let(::wirelessNetworkOf)?.statistics
            menu.addSync(MenuSyncs.boolean({ be?.let(::wirelessNetworkOf) != null }, { view.connected = it }))
            menu.addSync(MenuSyncs.int({ stats()?.producerCount ?: 0 }, { view.producers = it }))
            menu.addSync(MenuSyncs.int({ stats()?.storageCount ?: 0 }, { view.storage = it }))
            menu.addSync(MenuSyncs.int({ stats()?.consumerCount ?: 0 }, { view.consumers = it }))
            menu.addSync(MenuSyncs.double({ stats()?.produced ?: 0.0 }, { view.produced = it }))
            menu.addSync(MenuSyncs.double({ stats()?.consumed ?: 0.0 }, { view.consumed = it }))
            menu.addSync(MenuSyncs.double({ stats()?.storageDelta ?: 0.0 }, { view.storageDelta = it }))
            menu.addSync(MenuSyncs.double({ stats()?.averageTrend ?: 0.0 }, { view.averageTrend = it }))
            menu.addSync(MenuSyncs.double({ stats()?.dedicatedStored ?: 0.0 }, { view.dedicatedStored = it }))
            menu.addSync(MenuSyncs.double({ stats()?.dedicatedStorage ?: 0.0 }, { view.dedicatedStorage = it }))
            menu.addSync(MenuSyncs.double({ stats()?.totalStored ?: 0.0 }, { view.totalStored = it }))
            menu.addSync(MenuSyncs.double({ stats()?.totalStorage ?: 0.0 }, { view.totalStorage = it }))
        }
    }
}

/**
 * Crystal mount: its crystal slot and the network statistics. Port of v3's `ContainerCrystalMount`.
 */
class CrystalMountMenu(containerId: Int, inventory: Inventory, be: CrystalMountBlockEntity?) :
    FemtoMenu<CrystalMountBlockEntity>(PowerContent.CRYSTAL_MOUNT_MENU.get(), containerId, inventory, be) {
    @JvmField
    val network = PowerNetworkView()

    init {
        addStorageSlots(be?.storage ?: IItemStorage.Empty, 80, 35, count = if (be == null) 0 else 1, hint = net.minecraft.world.item.ItemStack(PowerContent.POWER_CRYSTAL.get()))
        addPlayerInventorySlots(inventory)
        PowerNetworkView.addTo(this, network, be)
    }
}

/**
 * Charging array, storage array and heat exchanger: their crystal (and fuel) slots, battery, generation and the
 * network statistics; the heat exchanger also syncs its burn time. Port of v3's `ContainerCrystalChargingArray`,
 * `ContainerCrystalStorageArray` and `ContainerCrystalHeatExchanger`.
 */
class CrystalMachineMenu(containerId: Int, inventory: Inventory, be: CrystalMachineBlockEntity?) :
    FemtoMenu<CrystalMachineBlockEntity>(PowerContent.CRYSTAL_MACHINE_MENU.get(), containerId, inventory, be) {
    @JvmField
    var battery = com.itszuvalex.itszulib.menu.EnergyView()

    @JvmField
    val network = PowerNetworkView()

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
        PowerNetworkView.addTo(this, network, be)
    }

    companion object {
        const val SLOTS_X = 62
        const val SLOTS_Y = 26
    }
}
