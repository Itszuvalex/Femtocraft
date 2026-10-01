package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.logistics.ConduitBlockEntity
import com.itszuvalex.femtocraft.logistics.ConduitMenu
import com.itszuvalex.femtocraft.logistics.ConnectionDirection
import com.itszuvalex.femtocraft.logistics.FluidRepositoryBlockEntity
import com.itszuvalex.femtocraft.logistics.ItemChips
import com.itszuvalex.femtocraft.logistics.ItemRepositoryBlockEntity
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.femtocraft.logistics.NanoPackMenu
import com.itszuvalex.femtocraft.logistics.NaniteRepositoryBlockEntity
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.NaniteStrainVersion
import com.itszuvalex.itszulib.api.adapters.IItemStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.transfer.ResourceHandlerUtil
import net.neoforged.neoforge.transfer.item.ItemResource

/**
 * Game tests for the logistics area.
 */
object LogisticsGameTests {
    private val CENTER = BlockPos(4, 1, 4)

    fun register() {
        DevGameTests.test("item_repository_exposes_slots_and_drops_contents", body = ::itemRepository)
        DevGameTests.test("fluid_repository_fills_from_water_below", body = ::fluidRepository)
        DevGameTests.test("nanite_repository_holds_one_strain", body = ::naniteRepository)
        DevGameTests.test("conduit_chips_move_items_between_inventories", body = ::conduitMovesItems)
        DevGameTests.test("conduit_channels_keep_items_apart", body = ::conduitChannels)
        DevGameTests.test("conduit_drops_chips", body = ::conduitDrops)
        DevGameTests.test("conduit_menu_cycles_chip_mode_and_interface", body = ::conduitMenu)
        DevGameTests.test("nano_pack_saves_contents_and_locks_its_slot", body = ::nanoPack)
        DevGameTests.test("nano_pack_cannot_be_swapped_into_itself", body = ::nanoPackSwap)
    }

    private fun itemRepository(helper: GameTestHelper) {
        val be = helper.place<ItemRepositoryBlockEntity>(CENTER, LogisticsContent.ITEM_REPOSITORY.get())
        val handler = helper.level.getCapability(Capabilities.Item.BLOCK, helper.absolutePos(CENTER), Direction.UP)
        helper.assertTrue(handler != null, "item handler exposed")
        helper.assertValueEqual(handler!!.size(), ItemRepositoryBlockEntity.SIZE, "54 slots")
        helper.assertValueEqual(ResourceHandlerUtil.insertStacking(handler, ItemResource.of(Items.DIAMOND), 70, null), 70, "inserted")
        helper.assertValueEqual(be.storage.get(1).stackSize(), 6, "spilled into the next slot")
        helper.destroyBlock(CENTER)
        helper.assertItemEntityCountIs(Items.DIAMOND, CENTER, 2.0, 70)
        helper.succeed()
    }

    private fun fluidRepository(helper: GameTestHelper) {
        helper.setBlock(CENTER, Blocks.WATER)
        val be = helper.place<FluidRepositoryBlockEntity>(CENTER.above(), LogisticsContent.FLUID_REPOSITORY.get())
        helper.succeedWhen {
            val fluid = be.tank.get(0).toMinecraft()
            helper.assertTrue(fluid.fluid == Fluids.WATER, "filled with water")
            helper.assertTrue(fluid.amount >= 10 * FluidRepositoryBlockEntity.WATER_PER_TICK, "keeps filling, at ${fluid.amount}")
        }
    }

    private fun naniteRepository(helper: GameTestHelper) {
        val be = helper.place<NaniteRepositoryBlockEntity>(CENTER, LogisticsContent.NANITE_REPOSITORY.get())
        helper.assertValueEqual(be.naniteTank.fill(NaniteRegistry.dumb(10), true).amount, 0, "first strain fits")
        val other = NaniteStack("Dumb", "Dumb", NaniteStrainVersion(1, 0), 5)
        helper.assertValueEqual(be.naniteTank.fill(other, true).amount, 5, "second strain refused")
        helper.assertValueEqual(be.naniteTank.fill(NaniteRegistry.dumb(500), true).amount, 500 - (NaniteRepositoryBlockEntity.VOLUME - 10), "capacity")
        helper.succeed()
    }

    /**
     * A chip that runs its first operation on the next tick.
     */
    private fun chip(face: Direction, channel: String = "default"): ItemStack {
        val stack = ItemStack(LogisticsContent.ITEM_CHIP.get())
        stack.set(ItemChips.CONNECTION.get(), ItemChips.defaults(face).copy(flops = 1.0, channel = channel))
        return stack
    }

    /**
     * chest - conduit - conduit - chest along x, with an input chip facing the first chest and an output chip facing
     * the second.
     */
    private fun line(helper: GameTestHelper, outputChannel: String): Pair<ChestBlockEntity, ChestBlockEntity> {
        val source = helper.place<ChestBlockEntity>(BlockPos(2, 1, 4), Blocks.CHEST)
        val first = helper.place<ConduitBlockEntity>(BlockPos(3, 1, 4), LogisticsContent.CONDUIT.get())
        val second = helper.place<ConduitBlockEntity>(BlockPos(4, 1, 4), LogisticsContent.CONDUIT.get())
        val target = helper.place<ChestBlockEntity>(BlockPos(5, 1, 4), Blocks.CHEST)
        source.setItem(0, ItemStack(Items.COBBLESTONE, 3))
        first.conduit.chips[Direction.WEST.get3DDataValue()].setSlot(0, IItemStack.of(chip(Direction.WEST)))
        second.conduit.chips[Direction.EAST.get3DDataValue()].setSlot(0, IItemStack.of(chip(Direction.EAST, outputChannel)))
        helper.assertTrue(first.conduit.connections().single().direction == ConnectionDirection.INPUT, "west chip inputs")
        helper.assertTrue(second.conduit.connections().single().direction == ConnectionDirection.OUTPUT, "east chip outputs")
        helper.assertTrue(first.conduit.inventoryFaces[Direction.WEST], "chest face connected")
        return source to target
    }

    private fun conduitMovesItems(helper: GameTestHelper) {
        val (source, target) = line(helper, "default")
        helper.succeedWhen {
            helper.assertTrue(target.getItem(0).`is`(Items.COBBLESTONE), "item arrived")
            helper.assertValueEqual(source.getItem(0).count + target.getItem(0).count, 3, "nothing lost or duplicated")
        }
    }

    private fun conduitChannels(helper: GameTestHelper) {
        val (source, target) = line(helper, "other")
        helper.runAfterDelay(20) {
            helper.assertTrue(target.isEmpty, "other channel received nothing")
            helper.assertValueEqual(source.getItem(0).count, 2, "one item waits in the input chip's buffer")
            helper.succeed()
        }
    }

    private fun conduitDrops(helper: GameTestHelper) {
        val be = helper.place<ConduitBlockEntity>(CENTER, LogisticsContent.CONDUIT.get())
        be.conduit.chips[Direction.UP.get3DDataValue()].setSlot(2, IItemStack.of(chip(Direction.UP)))
        helper.assertTrue(!be.conduit.chips[0].canInsert(0, IItemStack.of(ItemStack(Items.DIAMOND))), "only chips go in")
        helper.destroyBlock(CENTER)
        helper.assertItemEntityPresent(LogisticsContent.ITEM_CHIP.get(), CENTER, 2.0)
        helper.succeed()
    }

    private fun conduitMenu(helper: GameTestHelper) {
        val be = helper.place<ConduitBlockEntity>(CENTER, LogisticsContent.CONDUIT.get())
        val chips = be.conduit.chips[Direction.UP.get3DDataValue()]
        chips.setSlot(1, IItemStack.of(chip(Direction.UP)))
        val player = helper.makeMockServerPlayerInLevel()
        val menu = ConduitMenu(1, player.inventory, be)
        fun data() = chips.get(1).toMinecraft().get(ItemChips.CONNECTION.get())!!
        helper.assertTrue(data().direction == ConnectionDirection.OUTPUT, "up chip starts as output")
        helper.assertTrue(menu.handleAction(player, ConduitMenu.ACTION_MODE, ConduitMenu.data(Direction.UP.get3DDataValue(), 1)), "handled")
        helper.assertTrue(data().direction == ConnectionDirection.DISABLED, "output -> disabled")
        menu.handleAction(player, ConduitMenu.ACTION_MODE, ConduitMenu.data(Direction.UP.get3DDataValue(), 1, backward = true))
        menu.handleAction(player, ConduitMenu.ACTION_MODE, ConduitMenu.data(Direction.UP.get3DDataValue(), 1, backward = true))
        helper.assertTrue(data().direction == ConnectionDirection.INPUT, "back twice -> input")
        menu.handleAction(player, ConduitMenu.ACTION_INTERFACE, ConduitMenu.data(Direction.UP.get3DDataValue(), 1))
        helper.assertTrue(data().interfaceDirection == Direction.UP, "interface down -> up")
        helper.assertTrue(!menu.handleAction(player, ConduitMenu.ACTION_MODE, ConduitMenu.data(Direction.UP.get3DDataValue(), 0)), "empty slot ignored")
        helper.succeed()
    }

    private fun nanoPack(helper: GameTestHelper) {
        val player = helper.makeMockServerPlayerInLevel()
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack(LogisticsContent.NANO_PACK.get()))
        val menu = NanoPackMenu(1, player.inventory, InteractionHand.MAIN_HAND)
        menu.storage.setSlot(3, IItemStack.of(ItemStack(Items.DIAMOND, 5)))
        val saved = player.mainHandItem.get(DataComponents.CONTAINER)
        helper.assertTrue(saved != null && saved.nonEmptyItemCopyStream().anyMatch { it.`is`(Items.DIAMOND) && it.count == 5 }, "saved to the pack")
        val reopened = NanoPackMenu(2, player.inventory, InteractionHand.MAIN_HAND)
        helper.assertValueEqual(reopened.storage.get(3).stackSize(), 5, "read back")
        val held = reopened.slots.first { it.container === player.inventory && it.containerSlot == player.inventory.selectedSlot }
        helper.assertTrue(!held.mayPickup(player), "the pack's own slot is locked")
        helper.succeed()
    }

    /**
     * Pressing the hotbar key of the slot holding the pack over a pack slot (SWAP) used to move the pack into its own
     * storage, which is saved to the pack: the pack and everything in it were deleted.
     */
    private fun nanoPackSwap(helper: GameTestHelper) {
        val player = helper.makeMockServerPlayerInLevel()
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL)
        val pack = ItemStack(LogisticsContent.NANO_PACK.get())
        player.setItemInHand(InteractionHand.MAIN_HAND, pack)
        val menu = NanoPackMenu(1, player.inventory, InteractionHand.MAIN_HAND)
        menu.storage.setSlot(0, IItemStack.of(ItemStack(Items.DIAMOND, 5)))
        menu.clicked(1, player.inventory.selectedSlot, net.minecraft.world.inventory.ContainerInput.SWAP, player)
        helper.assertTrue(player.mainHandItem.`is`(LogisticsContent.NANO_PACK.get()), "the pack stays in the hand")
        helper.assertTrue(menu.storage.get(1).isEmpty(), "the pack is not inside itself")
        helper.assertTrue(!menu.slots[1].mayPlace(ItemStack(LogisticsContent.NANO_PACK.get())), "no nano packs in nano packs")
        helper.assertValueEqual(menu.storage.get(0).stackSize(), 5, "contents kept")
        helper.succeed()
    }
}
