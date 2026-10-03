package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.logistics.ConduitBlockEntity
import com.itszuvalex.femtocraft.logistics.ConduitMenu
import com.itszuvalex.femtocraft.core.ConduitArms
import com.itszuvalex.femtocraft.logistics.ConnectionDirection
import com.itszuvalex.femtocraft.logistics.FluidRepositoryBlockEntity
import com.itszuvalex.femtocraft.logistics.ChipKind
import com.itszuvalex.femtocraft.logistics.Connection
import com.itszuvalex.femtocraft.logistics.FluidChipKind
import com.itszuvalex.femtocraft.logistics.ItemChipKind
import com.itszuvalex.femtocraft.logistics.NaniteChipKind
import com.itszuvalex.femtocraft.logistics.ItemRepositoryBlockEntity
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.femtocraft.logistics.NanoPackMenu
import com.itszuvalex.femtocraft.logistics.NaniteRepositoryBlockEntity
import com.itszuvalex.itszulib.api.storage.IndexedItemStorage
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.NaniteStrainVersion
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.api.storage.ItemStorageArray
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.network.chat.Component
import net.minecraft.world.InteractionHand
import net.minecraft.tags.ItemTags
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
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
        DevGameTests.test("conduit_fluid_chips_move_fluid_between_tanks", body = ::conduitMovesFluid)
        DevGameTests.test("conduit_nanite_chips_move_nanites_between_tanks", body = ::conduitMovesNanites)
        DevGameTests.test("conduit_drops_chips", body = ::conduitDrops)
        DevGameTests.test("conduit_arms_follow_connections", body = ::conduitArms)
        DevGameTests.test("conduit_chip_progress_moves_with_the_chip", body = ::chipProgress)
        DevGameTests.test("conduit_menu_cycles_chip_mode_and_interface", body = ::conduitMenu)
        DevGameTests.test("nano_pack_saves_contents_and_locks_its_slot", body = ::nanoPack)
        DevGameTests.test("nano_pack_cannot_be_swapped_into_itself", body = ::nanoPackSwap)
        DevGameTests.test("indexed_storage_finds_slots_by_item_and_tag", body = ::indexedStorage)
    }

    /**
     * The model's arms are block state set from the conduit's connections: towards another conduit and an inventory,
     * and gone when the neighbour is.
     */
    private fun conduitArms(helper: GameTestHelper) {
        helper.place<ConduitBlockEntity>(CENTER, LogisticsContent.CONDUIT.get())
        helper.place<ConduitBlockEntity>(CENTER.east(), LogisticsContent.CONDUIT.get())
        helper.place<ChestBlockEntity>(CENTER.west(), Blocks.CHEST)
        fun arm(pos: BlockPos, face: Direction) = helper.getBlockState(pos).getValue(ConduitArms.PROPERTIES.getValue(face))
        helper.startSequence()
            .thenWaitUntil {
                helper.assertTrue(arm(CENTER, Direction.EAST) && arm(CENTER.east(), Direction.WEST), "arms between the conduits")
                helper.assertTrue(arm(CENTER, Direction.WEST), "arm into the chest")
                helper.assertTrue(!arm(CENTER, Direction.NORTH) && !arm(CENTER, Direction.UP), "no arm towards air")
            }
            .thenExecute { helper.destroyBlock(CENTER.east()) }
            .thenWaitUntil { helper.assertTrue(!arm(CENTER, Direction.EAST), "arm removed with the neighbour") }
            .thenSucceed()
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

    /** Femtocraft indexes storage with ItszuLib's IndexedItemStorage: by item id and tag, kept current by writes. */
    private fun indexedStorage(helper: GameTestHelper) {
        val index = IndexedItemStorage(ItemStorageArray(4))
        val diamond = BuiltInRegistries.ITEM.getKey(Items.DIAMOND)
        index.setSlot(0, IItemStack.of(ItemStack(Items.OAK_LOG, 3)))
        index.setSlot(2, IItemStack.of(ItemStack(Items.DIAMOND)))
        index.setSlot(3, IItemStack.of(ItemStack(Items.BIRCH_LOG)))
        helper.assertValueEqual(index.slotsOf(diamond), setOf(2), "diamond slot")
        helper.assertValueEqual(index.slotsOfTag(ItemTags.LOGS), setOf(0, 3), "log slots")

        // A named diamond is still a diamond to the index; count tells them apart with a matcher.
        index.setSlot(1, IItemStack.of(ItemStack(Items.DIAMOND).also { it.set(DataComponents.CUSTOM_NAME, Component.literal("x")) }))
        helper.assertValueEqual(index.slotsOf(diamond), setOf(1, 2), "diamonds by item id")
        helper.assertValueEqual(index.count(diamond) { !it.hasComponents() }, 1, "plain diamonds")
        index.setSlot(1, IItemStack.of(ItemStack(Items.OAK_LOG)))
        helper.assertValueEqual(index.slotsOf(diamond), setOf(2), "replaced")
        helper.succeed()
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
    private fun chip(face: Direction, channel: String = "default", kind: ChipKind<*> = ItemChipKind, flops: Double = 1.0): ItemStack {
        val stack = ItemStack(chipItem(kind))
        primed(kind, stack, face, channel, flops)
        return stack
    }

    private fun chipItem(kind: ChipKind<*>) = when (kind) {
        FluidChipKind -> LogisticsContent.FLUID_CHIP.get()
        NaniteChipKind -> LogisticsContent.NANITE_CHIP.get()
        else -> LogisticsContent.ITEM_CHIP.get()
    }

    private fun <B : Any> primed(kind: ChipKind<B>, stack: ItemStack, face: Direction, channel: String, flops: Double) {
        val d = kind.defaults(face)
        stack.set(kind.component, d.with(settings = d.settings.copy(flops = flops, channel = channel)))
    }

    private fun flopsOf(stack: ItemStack): Double = stack.get(ItemChipKind.component)!!.settings.flops

    /**
     * The countdown ticks in the conduit without rewriting the chip (REVIEW O5), is written to the chip when it is taken
     * out, and is picked up by the next conduit.
     */
    private fun chipProgress(helper: GameTestHelper) {
        val first = helper.place<ConduitBlockEntity>(CENTER, LogisticsContent.CONDUIT.get())
        val second = helper.place<ConduitBlockEntity>(CENTER.offset(0, 0, 3), LogisticsContent.CONDUIT.get())
        val stored = chip(Direction.WEST, flops = 3000.0)
        val slots = first.conduit.chips[Direction.WEST.get3DDataValue()]
        slots.setSlot(0, IItemStack.of(stored))
        // Saving writes the countdown into the chip, and the test server saves a new chunk early at an unpredictable
        // tick. A chunk is saved at most every 10 s, so over 20 ticks the chip may change once while the counter moves
        // every tick.
        val chipValues = mutableSetOf(flopsOf(stored))
        helper.onEachTick { chipValues += flopsOf(stored) }
        helper.runAfterDelay(20) {
            helper.assertTrue(chipValues.size <= 2, "ticking leaves the chip alone, saw $chipValues")
            val counted = slots.counter(0).flops
            helper.assertTrue(counted < 3000.0 - 15 * 25.0 && counted > 0.0, "the conduit counted down, at $counted")
            val taken = slots.split(0, 1).toMinecraft()
            helper.assertValueEqual(flopsOf(taken), counted, "taking the chip writes its progress")
            val target = second.conduit.chips[Direction.EAST.get3DDataValue()]
            target.setSlot(2, IItemStack.of(taken))
            helper.assertValueEqual(target.counter(2).flops, counted, "the next conduit starts from it")
            helper.succeed()
        }
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

    /**
     * [source] - conduit - conduit - [target] along x, with an input chip of [kind] facing the source and an output chip
     * facing the target. @return The two conduits.
     */
    private fun chipLine(helper: GameTestHelper, kind: ChipKind<*>): Pair<ConduitBlockEntity, ConduitBlockEntity> {
        val first = helper.place<ConduitBlockEntity>(BlockPos(3, 1, 4), LogisticsContent.CONDUIT.get())
        val second = helper.place<ConduitBlockEntity>(BlockPos(4, 1, 4), LogisticsContent.CONDUIT.get())
        first.conduit.chips[Direction.WEST.get3DDataValue()].setSlot(0, IItemStack.of(chip(Direction.WEST, kind = kind)))
        second.conduit.chips[Direction.EAST.get3DDataValue()].setSlot(0, IItemStack.of(chip(Direction.EAST, kind = kind)))
        return first to second
    }

    private fun <B : Any> buffered(kind: ChipKind<B>, conduits: Pair<ConduitBlockEntity, ConduitBlockEntity>): Int =
        (conduits.first.conduit.connections() + conduits.second.conduit.connections())
            .filter { it.kind === kind }
            .sumOf { @Suppress("UNCHECKED_CAST") kind.amount((it as Connection<B>).buffer) }

    private fun conduitMovesFluid(helper: GameTestHelper) {
        val source = helper.place<FluidRepositoryBlockEntity>(BlockPos(2, 1, 4), LogisticsContent.FLUID_REPOSITORY.get())
        val target = helper.place<FluidRepositoryBlockEntity>(BlockPos(5, 1, 4), LogisticsContent.FLUID_REPOSITORY.get())
        source.tank.fill(IFluidStack.of(FluidStack(Fluids.WATER, 600)), true)
        val conduits = chipLine(helper, FluidChipKind)
        helper.assertTrue(conduits.first.conduit.inventoryFaces[Direction.WEST], "tank face connected")
        helper.succeedWhen {
            val arrived = target.tank.get(0).toMinecraft()
            helper.assertTrue(arrived.fluid == Fluids.WATER && arrived.amount >= FluidChipKind.perOp, "water arrived, at ${arrived.amount}")
            helper.assertValueEqual(source.tank.get(0).amount() + arrived.amount + buffered(FluidChipKind, conduits), 600, "nothing lost or duplicated")
        }
    }

    private fun conduitMovesNanites(helper: GameTestHelper) {
        val source = helper.place<NaniteRepositoryBlockEntity>(BlockPos(2, 1, 4), LogisticsContent.NANITE_REPOSITORY.get())
        val target = helper.place<NaniteRepositoryBlockEntity>(BlockPos(5, 1, 4), LogisticsContent.NANITE_REPOSITORY.get())
        source.naniteTank.fill(NaniteRegistry.dumb(30), true)
        val conduits = chipLine(helper, NaniteChipKind)
        helper.succeedWhen {
            val arrived = target.naniteTank.contents().singleOrNull()
            helper.assertTrue(arrived != null && arrived.isSameNanite(NaniteRegistry.dumb(1)) && arrived.amount >= NaniteChipKind.perOp, "nanites arrived, at $arrived")
            helper.assertValueEqual(source.naniteTank.amount + target.naniteTank.amount + buffered(NaniteChipKind, conduits), 30, "nothing lost or duplicated")
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
        fun data() = chips.get(1).toMinecraft().get(ItemChipKind.component)!!.settings
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
