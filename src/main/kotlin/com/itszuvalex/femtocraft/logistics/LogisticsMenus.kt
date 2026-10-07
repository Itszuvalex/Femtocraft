package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.core.FemtoMenu
import com.itszuvalex.femtocraft.nanite.NaniteMachineMenu
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.PlayerNanites
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.menu.MenuSync
import com.itszuvalex.itszulib.menu.MenuSyncs
import com.itszuvalex.itszulib.api.adapters.IItemStack
import net.minecraft.core.Direction
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player

/**
 * Item repository: six rows of nine. Port of v3's `ContainerItemRepository` (player inventory at y 129).
 */
class ItemRepositoryMenu(containerId: Int, inventory: Inventory, be: ItemRepositoryBlockEntity?) :
    FemtoMenu<ItemRepositoryBlockEntity>(LogisticsContent.ITEM_REPOSITORY_MENU.get(), containerId, inventory, be) {
    init {
        addStorageSlots(be?.storage ?: IItemStorage.Empty, 8, 18, count = if (be == null) 0 else ItemRepositoryBlockEntity.SIZE)
        addPlayerInventorySlots(inventory, 8, 140)
    }
}

/**
 * Fluid repository: the tank. Port of v3's `ContainerFluidRepository`.
 */
class FluidRepositoryMenu(containerId: Int, inventory: Inventory, be: FluidRepositoryBlockEntity?) :
    FemtoMenu<FluidRepositoryBlockEntity>(LogisticsContent.FLUID_REPOSITORY_MENU.get(), containerId, inventory, be) {
    var tank: IFluidStack = IFluidStack.Empty

    init {
        addPlayerInventorySlots(inventory)
        if (be != null) addSync(MenuSync({ be.tank.get(0) }, { tank = it }, MenuSyncs.FLUID, { a, b -> a.amount() == b.amount() && a.isFluidEqual(b) }, { it.copy() }))
    }
}

/**
 * Nanite repository: the tank, the player's nanites and fill/drain actions (as [NaniteMachineMenu]). Port of v3's
 * `ContainerNaniteRepository` + `GuiNaniteTank`.
 */
class NaniteRepositoryMenu(containerId: Int, inventory: Inventory, be: NaniteRepositoryBlockEntity?) :
    FemtoMenu<NaniteRepositoryBlockEntity>(LogisticsContent.NANITE_REPOSITORY_MENU.get(), containerId, inventory, be) {
    var tank: List<NaniteStack> = listOf()
    var playerTank: List<NaniteStack> = listOf()

    init {
        addPlayerInventorySlots(inventory)
        if (be != null) addSync(MenuSync({ be.naniteTank.contents() }, { tank = it }, NaniteMachineMenu.LIST))
        addSync(MenuSync({ PlayerNanites.tank(inventory.player).contents() }, { playerTank = it }, NaniteMachineMenu.LIST))
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        val tank = blockEntity?.naniteTank ?: return false
        when (action) {
            NaniteMachineMenu.ACTION_FILL -> PlayerNanites.fill(player, tank)
            NaniteMachineMenu.ACTION_DRAIN -> PlayerNanites.drain(player, tank)
            else -> return false
        }
        return true
    }
}

/**
 * Logistics conduit: one row of four chip slots per face (down, up, north, south, west, east), and actions that cycle a
 * chip's connection direction or interface face, or change its filter ([ACTION_FILTER]: an ItszuLib
 * [com.itszuvalex.itszulib.api.filter.FilterActions] action above the slot bits, applied with the carried stack: a cell
 * set to the held item or the fluid it holds, cleared by an empty hand, or the allow/deny mode or component matching
 * toggled). Port of
 * v3's `ContainerConduit`, `ContainerConduitSide` (a per-face view of one row) and the conduit messages (DECISIONS
 * D11).
 */
class ConduitMenu(containerId: Int, inventory: Inventory, be: ConduitBlockEntity?) :
    FemtoMenu<ConduitBlockEntity>(LogisticsContent.CONDUIT_MENU.get(), containerId, inventory, be) {
    init {
        if (be != null) {
            for (face in 0 until 6) addStorageSlots(be.conduit.chips[face], 26 + (face % 2) * 76, 18 + (face / 2) * 20, columns = 4)
        }
        addPlayerInventorySlots(inventory, 8, INVENTORY_Y)
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        val be = blockEntity ?: return false
        return applyChipAction(be, carried, action, data)
    }

    companion object {
        /**
         * Applies a chip action ([ACTION_MODE], [ACTION_INTERFACE] or [ACTION_FILTER], with its [data]) to the chip in
         * [be] it names, with [carried] in hand. @return False if the action or its chip does not exist.
         */
        fun applyChipAction(be: ConduitBlockEntity, carried: net.minecraft.world.item.ItemStack, action: Int, data: Int): Boolean {
            if (action != ACTION_MODE && action != ACTION_INTERFACE && action != ACTION_FILTER) return false
            val slot = data and SLOT_MASK
            if (slot >= 6 * LogisticsConduit.CHIPS_PER_FACE) return false
            val face = slot / LogisticsConduit.CHIPS_PER_FACE
            val index = slot % LogisticsConduit.CHIPS_PER_FACE
            val storage = be.conduit.chips[face]
            val chip = storage.get(index).toMinecraft()
            if (!Chips.isChip(chip)) return false
            val changed = if (action == ACTION_FILTER) {
                Chips.withFilter(chip, Direction.from3DDataValue(face), data ushr FILTER_SHIFT, carried) ?: return false
            } else {
                Chips.cycled(chip, Direction.from3DDataValue(face), action == ACTION_MODE, data and BACKWARD == 0)
            }
            storage.setSlot(index, IItemStack.of(changed))
            return true
        }

        const val ACTION_MODE = 0
        const val ACTION_INTERFACE = 1
        const val ACTION_FILTER = 2
        const val INVENTORY_Y = 94
        const val HEIGHT = INVENTORY_Y + 58 + 18 + 6

        /** In [ACTION_FILTER]'s data: the filter action, above the slot bits. */
        const val FILTER_SHIFT = 6

        /** [ACTION_FILTER] data: filter [action] ([com.itszuvalex.itszulib.api.filter.FilterActions]) on chip [index] in conduit face [face]. */
        fun filterData(face: Int, index: Int, action: Int): Int = (face * LogisticsConduit.CHIPS_PER_FACE + index) or (action shl FILTER_SHIFT)
        const val SLOT_MASK = 0x1F

        /** Flag in an action's data: cycle backwards. */
        const val BACKWARD = 0x20

        /**
         * Action data for chip [index] in conduit face [face].
         */
        fun data(face: Int, index: Int, backward: Boolean = false): Int = (face * LogisticsConduit.CHIPS_PER_FACE + index) or (if (backward) BACKWARD else 0)
    }
}

/**
 * One chip in a conduit, opened by using the chip where the conduit shows it ([ChipNodes]): the chip's slot (it can be
 * taken out or swapped) and the conduit menu's chip actions for that chip only ([ConduitMenu.applyChipAction]: the
 * same action ids and data, naming [chipSlot]).
 */
class ChipMenu(containerId: Int, inventory: Inventory, be: ConduitBlockEntity?, @JvmField val chipSlot: Int) :
    FemtoMenu<ConduitBlockEntity>(LogisticsContent.CHIP_MENU.get(), containerId, inventory, be) {
    /** The chip's face. */
    val face: Direction get() = Direction.from3DDataValue(chipSlot / LogisticsConduit.CHIPS_PER_FACE)

    init {
        if (be != null && chipSlot in 0 until ChipNodes.SLOTS) {
            addStorageSlots(be.conduit.chips[chipSlot / LogisticsConduit.CHIPS_PER_FACE], CHIP_X, CHIP_Y, first = chipSlot % LogisticsConduit.CHIPS_PER_FACE, count = 1)
        }
        addPlayerInventorySlots(inventory, 8, INVENTORY_Y)
    }

    override fun handleAction(player: Player, action: Int, data: Int): Boolean {
        val be = blockEntity ?: return false
        if (data and ConduitMenu.SLOT_MASK != chipSlot) return false
        return ConduitMenu.applyChipAction(be, carried, action, data)
    }

    companion object {
        const val CHIP_X = 8
        const val CHIP_Y = 18

        /** Below the chip, its controls (settings, buttons, filter: 65 high from y 40) and the inventory label. */
        const val INVENTORY_Y = 120
        const val HEIGHT = INVENTORY_Y + 58 + 18 + 6
    }
}
