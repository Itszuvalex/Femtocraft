package com.itszuvalex.femtocraft.industry

import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.menu.MenuSyncs
import net.minecraft.world.entity.player.Inventory

/**
 * Single-block machines: input, output and crystal slots, progress, battery and (liquifier) tank. Port of v3's
 * `ContainerNanoFurnace`, `ContainerDemolisher`, `ContainerCrystalFurnace`, `ContainerCrystalCrusher` and
 * `ContainerCrystalLiquifier`.
 */
class MachineMenu(containerId: Int, inventory: Inventory, be: ProcessingMachineBlockEntity?) :
    FemtoMenu<ProcessingMachineBlockEntity>(IndustryContent.MACHINE_MENU.get(), containerId, inventory, be) {
    @JvmField
    val battery = FemtoMenu.BatteryView()

    var progress = 0.0
    var tank: IFluidStack = IFluidStack.Empty

    init {
        val storage = be?.inventory ?: IItemStorage.Empty
        when (be) {
            is ItemProcessingMachineBlockEntity -> {
                addStorageSlots(storage, 56, 35, count = 1)
                addStorageSlots(storage, 116, 35, first = 1, count = 1, output = true)
                if (be is CrystalItemMachineBlockEntity) addStorageSlots(storage, 56, 57, first = CrystalItemMachineBlockEntity.CRYSTAL_SLOT, count = 1)
            }
            is CrystalLiquifierBlockEntity -> {
                addStorageSlots(storage, 56, 35, count = 1)
                addStorageSlots(storage, 56, 57, first = CrystalLiquifierBlockEntity.CRYSTAL_SLOT, count = 1)
            }
            else -> {}
        }
        addPlayerInventorySlots(inventory)
        if (be != null) {
            syncBattery({ be.battery }, battery)
            addSync(MenuSyncs.double(be::progressFraction) { progress = it })
            if (be is CrystalLiquifierBlockEntity) addSync(MenuSyncs.fluid(object : com.itszuvalex.itszulib.api.storage.FluidStorageArray(1, 1) {
                override fun get(index: Int): IFluidStack = be.tank.get(0)
                override fun set(index: Int, stack: IFluidStack) {
                    tank = stack
                }
                override fun setQuietly(index: Int, stack: IFluidStack) {
                    tank = stack
                }
            }, 0))
        }
    }
}

/**
 * Germination chamber: input and three output slots, tank, battery and progress. Port of v3's
 * `ContainerGerminationChamber`.
 */
class GerminationChamberMenu(containerId: Int, inventory: Inventory, be: GerminationChamberBlockEntity?) :
    FemtoMenu<GerminationChamberBlockEntity>(IndustryContent.GERMINATION_CHAMBER_MENU.get(), containerId, inventory, be) {
    @JvmField
    val battery = FemtoMenu.BatteryView()

    var progress = 0.0
    var water = 0

    init {
        val storage = be?.storage ?: IItemStorage.Empty
        addStorageSlots(storage, 44, 35, count = if (be == null) 0 else 1)
        addStorageSlots(storage, 98, 35, first = 1, count = if (be == null) 0 else 3, output = true)
        addPlayerInventorySlots(inventory)
        if (be != null) {
            syncBattery({ be.battery }, battery)
            addSync(MenuSyncs.double({ be.state()?.task?.fraction(0.0) ?: 0.0 }, { progress = it }))
            addSync(MenuSyncs.int({ be.tank.get(0).amount() }, { water = it }))
        }
    }
}

/**
 * Crystal focusing chamber: four small crystal slots and the large crystal slot.
 */
class FocusingChamberMenu(containerId: Int, inventory: Inventory, be: CrystalFocusingChamberBlockEntity?) :
    FemtoMenu<CrystalFocusingChamberBlockEntity>(IndustryContent.FOCUSING_CHAMBER_MENU.get(), containerId, inventory, be) {
    init {
        addStorageSlots(be?.small ?: IItemStorage.Empty, 44, 26, columns = 2, count = if (be == null) 0 else 4)
        addStorageSlots(be?.large ?: IItemStorage.Empty, 116, 35, count = if (be == null) 0 else 1)
        addPlayerInventorySlots(inventory)
    }
}
