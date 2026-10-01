package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import com.itszuvalex.itszulib.menu.MenuCore
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ItemContainerContents
import net.minecraft.world.level.Level

/**
 * A portable 18-slot inventory. Port of v3's `ItemNanoPack` (whose `ItemStorageNBT` lived in the stack's NBT); the
 * contents are the vanilla `container` component.
 */
class NanoPackItem(properties: Properties) : Item(properties) {
    override fun use(level: Level, player: Player, hand: InteractionHand): InteractionResult {
        if (!level.isClientSide) {
            player.openMenu(SimpleMenuProvider({ id, inv, _ -> NanoPackMenu(id, inv, hand) }, Component.translatable("item.femtocraft.nano_pack"))) { buf -> buf.writeEnum(hand) }
        }
        return InteractionResult.SUCCESS
    }

    companion object {
        const val SIZE = 18
    }
}

/**
 * The nano pack's slots (two rows of nine) and the player's inventory. Port of v3's `ContainerNanoPack`. The pack is
 * read into a live storage when the menu opens and written back to the held stack after every change, since vanilla
 * slots change their stacks in place (ItszuLib REVIEW O1). The held pack's own slot cannot be used, and nano packs do
 * not go into nano packs: a hotbar-key swap could otherwise move the held pack into itself and delete it.
 */
class NanoPackMenu(containerId: Int, private val inventory: Inventory, private val hand: InteractionHand) :
    MenuCore(LogisticsContent.NANO_PACK_MENU.get(), containerId, inventory.player) {
    private val pack: ItemStack get() = inventory.player.getItemInHand(hand)

    @JvmField
    val storage = object : ItemStorageArray(NanoPackItem.SIZE, { save() }) {
        override fun canInsert(index: Int, stack: IItemStack): Boolean = stack.toMinecraft().item !is NanoPackItem
    }

    init {
        val contents = pack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY)
        val items = net.minecraft.core.NonNullList.withSize(NanoPackItem.SIZE, ItemStack.EMPTY)
        contents.copyInto(items)
        items.forEachIndexed { i, s -> storage.setSlotQuietly(i, IItemStack.of(s)) }
        addStorageSlots(storage, 8, 18)
        addPlayerInventorySlots(inventory)
        val held = if (hand == InteractionHand.MAIN_HAND) inventory.selectedSlot else -1
        if (held >= 0) {
            // Lock the hotbar slot holding the pack.
            val index = slots.indexOfFirst { it.container === inventory && it.containerSlot == held }
            if (index >= 0) slots[index] = object : Slot(inventory, held, slots[index].x, slots[index].y) {
                override fun mayPickup(player: Player): Boolean = false
                override fun mayPlace(stack: ItemStack): Boolean = false
            }.also { it.index = index }
        }
    }

    private fun save() {
        val stack = pack
        if (stack.item !is NanoPackItem) return
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems((0 until storage.size()).map { storage.get(it).toMinecraft().copy() }))
    }

    override fun broadcastChanges() {
        save()
        super.broadcastChanges()
    }

    override fun stillValid(player: Player): Boolean = pack.item is NanoPackItem
}
