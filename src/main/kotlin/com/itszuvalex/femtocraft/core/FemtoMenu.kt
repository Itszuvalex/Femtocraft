package com.itszuvalex.femtocraft.core

import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.wrappers.WrapperContainerIItemStorage
import net.minecraft.core.BlockPos
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.world.Container
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.DataSlot
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * Menu over a block entity's [IItemStorage]s. Port of ItszuLib 1.7.10's `ContainerInv`: machine slots first, then the
 * player inventory; shift-click moves between the two groups.
 *
 * Client-side instances are built from the block entity at the position sent in the open-menu buffer (see [readBlockEntity]).
 */
abstract class FemtoMenu(type: MenuType<*>, containerId: Int, protected val playerInventory: Inventory, val blockEntity: BlockEntity?) :
    AbstractContainerMenu(type, containerId) {

    private var machineSlots = 0

    /**
     * Adds a slot backed by one index of [storage]. Must be called before [addPlayerInventory].
     *
     * @param mayPlace Which items the slot accepts; defaults to `storage.canInsert`.
     */
    protected fun addStorageSlot(
        storage: IItemStorage,
        index: Int,
        x: Int,
        y: Int,
        mayPlace: (ItemStack) -> Boolean = { storage.canInsert(index, com.itszuvalex.itszulib.api.adapters.IItemStack.of(it)) },
    ): Slot {
        machineSlots++
        return addSlot(object : Slot(WrapperContainerIItemStorage(storage), index, x, y) {
            override fun mayPlace(stack: ItemStack): Boolean = mayPlace(stack)
        })
    }

    /**
     * Output-only slot.
     */
    protected fun addOutputSlot(storage: IItemStorage, index: Int, x: Int, y: Int): Slot = addStorageSlot(storage, index, x, y) { false }

    protected fun addPlayerInventory(x: Int = 8, y: Int = 84) = addStandardInventorySlots(playerInventory, x, y)

    /**
     * Syncs an int to the client (e.g. progress).
     */
    protected fun trackInt(get: () -> Int, set: (Int) -> Unit) {
        addDataSlot(object : DataSlot() {
            override fun get(): Int = get()
            override fun set(value: Int) = set(value)
        })
    }

    /**
     * Syncs a long (e.g. power) as two ints, like the 1.7.10 containers did.
     */
    protected fun trackLong(get: () -> Long, set: (Long) -> Unit) {
        trackInt({ (get() ushr 32).toInt() }, { hi -> set((hi.toLong() shl 32) or (get() and 0xFFFFFFFFL)) })
        trackInt({ get().toInt() }, { lo -> set((get() and -0x100000000L) or (lo.toLong() and 0xFFFFFFFFL)) })
    }

    override fun stillValid(player: Player): Boolean {
        val be = blockEntity ?: return false
        return !be.isRemoved && Container.stillValidBlockEntity(be, player)
    }

    override fun quickMoveStack(player: Player, index: Int): ItemStack {
        val slot = slots.getOrNull(index) ?: return ItemStack.EMPTY
        if (!slot.hasItem()) return ItemStack.EMPTY
        val stack = slot.item
        val original = stack.copy()
        if (index < machineSlots) {
            if (!moveItemStackTo(stack, machineSlots, slots.size, true)) return ItemStack.EMPTY
        } else if (!moveItemStackTo(stack, 0, machineSlots, false)) {
            return ItemStack.EMPTY
        }
        if (stack.isEmpty) slot.setByPlayer(ItemStack.EMPTY) else slot.setChanged()
        if (stack.count == original.count) return ItemStack.EMPTY
        slot.onTake(player, stack)
        return original
    }

    companion object {
        /**
         * Client side: the block entity at the position written by `Player.openMenu(provider, pos)`.
         */
        inline fun <reified T : BlockEntity> readBlockEntity(inventory: Inventory, buf: RegistryFriendlyByteBuf): T? {
            val pos: BlockPos = buf.readBlockPos()
            return inventory.player.level().getBlockEntity(pos) as? T
        }
    }
}
