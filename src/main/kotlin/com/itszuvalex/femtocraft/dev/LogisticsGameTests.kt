package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.logistics.ConduitBlockEntity
import com.itszuvalex.femtocraft.logistics.ConduitMenu
import com.itszuvalex.femtocraft.logistics.ChipMenu
import com.itszuvalex.femtocraft.logistics.ChipNodes
import com.itszuvalex.femtocraft.logistics.Chips
import com.itszuvalex.femtocraft.logistics.LogisticsConduit
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
        DevGameTests.test("conduit_shows_its_chips_in_the_world", body = ::chipsShown)
        DevGameTests.test("using_a_shown_chip_opens_its_own_menu", body = ::chipMenu)
        DevGameTests.test("chip_filter_limits_what_an_input_chip_pulls", body = ::filteredInput)
        DevGameTests.test("chip_filter_limits_what_an_output_chip_takes", body = ::filteredOutput)
        DevGameTests.test("chip_pulls_filtered_items_through_a_vault_index", body = ::filteredVault)
        DevGameTests.test("conduit_menu_sets_chip_filters_from_the_held_stack", body = ::filterMenu)
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

    /**
     * The conduit sums up its chips' kinds per slot ([ChipNodes]) and syncs it; the block's shape includes a cube per
     * chip, at its arm's end where the face has an arm and against the core where it has none.
     */
    private fun chipsShown(helper: GameTestHelper) {
        val be = helper.place<ConduitBlockEntity>(CENTER, LogisticsContent.CONDUIT.get())
        helper.place<ConduitBlockEntity>(CENTER.north(), LogisticsContent.CONDUIT.get())
        val up = Direction.UP.get3DDataValue() * LogisticsConduit.CHIPS_PER_FACE + 1
        val north = Direction.NORTH.get3DDataValue() * LogisticsConduit.CHIPS_PER_FACE + 3
        be.conduit.chips[Direction.UP.get3DDataValue()].setSlot(1, IItemStack.of(chip(Direction.UP)))
        be.conduit.chips[Direction.NORTH.get3DDataValue()].setSlot(3, IItemStack.of(chip(Direction.NORTH, kind = FluidChipKind)))
        helper.assertValueEqual(ChipNodes.kindAt(be.conduit.chipLayout, up), Chips.KINDS.indexOf(ItemChipKind) + 1, "item chip in the layout")
        helper.assertValueEqual(ChipNodes.kindAt(be.conduit.chipLayout, north), Chips.KINDS.indexOf(FluidChipKind) + 1, "fluid chip in the layout")
        helper.assertValueEqual((0 until ChipNodes.SLOTS).count { ChipNodes.kindAt(be.conduit.chipLayout, it) != 0 }, 2, "nothing else")

        val registries = helper.level.registryAccess()
        val client = ConduitBlockEntity(be.blockPos, be.blockState)
        client.handleUpdateTag(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registries, be.getUpdateTag(registries)))
        helper.assertValueEqual(client.conduit.chipLayout, be.conduit.chipLayout, "clients get the layout")

        helper.runAfterDelay(2) {
            val state = helper.getBlockState(CENTER)
            helper.assertTrue(be.hasArm(state, Direction.NORTH) && !be.hasArm(state, Direction.UP), "an arm north only")
            val northBox = ChipNodes.box(north, true)
            helper.assertTrue(northBox.minZ == 0.0, "the north chip touches the next block, at ${northBox}")
            val upBox = ChipNodes.box(up, false)
            helper.assertTrue(upBox.minY == 10.0 / 16 && upBox.maxY < 1.0, "the up chip sits against the core, at $upBox")
            val shape = state.getShape(helper.level, be.blockPos)
            helper.assertTrue(shape.bounds().minZ == 0.0, "the shape reaches the arm's end")
            helper.assertTrue(!shape.isEmpty && shape.toAabbs().any { it.contains(upBox.center) }, "the shape holds the up chip's cube")
            val world = net.minecraft.world.phys.Vec3.atLowerCornerOf(be.blockPos)
            helper.assertValueEqual(be.chipSlotAt(upBox.center.add(world)), up, "aiming at the up chip finds it")
            helper.assertValueEqual(be.chipSlotAt(net.minecraft.world.phys.Vec3(0.5, 0.5, 0.5).add(world)), -1, "the core is no chip")

            be.conduit.chips[Direction.UP.get3DDataValue()].setSlot(1, IItemStack.Empty)
            helper.assertValueEqual(ChipNodes.kindAt(be.conduit.chipLayout, up), 0, "taking the chip out clears it")
            helper.succeed()
        }
    }

    /**
     * Using a chip on the conduit opens a menu for it alone: its slot and its actions; actions naming another chip are
     * refused. (Opening it sends NeoForge's menu-with-data payload, which mock players cannot receive, so the menu is
     * made here for the slot the hit finds.)
     */
    private fun chipMenu(helper: GameTestHelper) {
        val be = helper.place<ConduitBlockEntity>(CENTER, LogisticsContent.CONDUIT.get())
        val chips = be.conduit.chips[Direction.EAST.get3DDataValue()]
        chips.setSlot(2, IItemStack.of(chip(Direction.EAST)))
        chips.setSlot(0, IItemStack.of(chip(Direction.EAST)))
        val slot = Direction.EAST.get3DDataValue() * LogisticsConduit.CHIPS_PER_FACE + 2
        val player = helper.makeMockServerPlayerInLevel()
        val hit = ChipNodes.box(slot, false).center.add(net.minecraft.world.phys.Vec3.atLowerCornerOf(be.blockPos))
        helper.assertValueEqual(be.chipSlotAt(hit), slot, "the hit finds the chip")
        val menu = ChipMenu(1, player.inventory, be, slot)
        helper.assertTrue(menu.slots[0].item.`is`(LogisticsContent.ITEM_CHIP.get()), "its first slot holds the chip")
        fun direction(i: Int) = chips.get(i).toMinecraft().get(ItemChipKind.component)!!.settings.direction
        val before = direction(2)
        helper.assertTrue(menu.handleAction(player, ConduitMenu.ACTION_MODE, ConduitMenu.data(Direction.EAST.get3DDataValue(), 2)), "its mode cycles")
        helper.assertTrue(direction(2) != before, "changed")
        val other = direction(0)
        helper.assertFalse(menu.handleAction(player, ConduitMenu.ACTION_MODE, ConduitMenu.data(Direction.EAST.get3DDataValue(), 0)), "another chip's action refused")
        helper.assertTrue(direction(0) == other, "the other chip is unchanged")
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

    /** A chip of [kind] in face [face] set to [direction], allowing [allowed] (anything if none). */
    private fun <B : Any> filtered(kind: ChipKind<B>, face: Direction, direction: ConnectionDirection, vararg allowed: B): ItemStack {
        val stack = ItemStack(chipItem(kind))
        val d = kind.defaults(face)
        stack.set(kind.component, d.with(settings = d.settings.copy(flops = 1.0, direction = direction), filter = com.itszuvalex.itszulib.api.filter.ResourceFilter(kind.filterKind, ChipKind.FILTER_SLOTS, allowed.toList())))
        return stack
    }

    /** chest - conduit - conduit - chest, as [line], with the chips given. */
    private fun filteredLine(helper: GameTestHelper, input: ItemStack, output: ItemStack): Pair<ChestBlockEntity, ChestBlockEntity> {
        val source = helper.place<ChestBlockEntity>(BlockPos(2, 1, 4), Blocks.CHEST)
        val first = helper.place<ConduitBlockEntity>(BlockPos(3, 1, 4), LogisticsContent.CONDUIT.get())
        val second = helper.place<ConduitBlockEntity>(BlockPos(4, 1, 4), LogisticsContent.CONDUIT.get())
        val target = helper.place<ChestBlockEntity>(BlockPos(5, 1, 4), Blocks.CHEST)
        first.conduit.chips[Direction.WEST.get3DDataValue()].setSlot(0, IItemStack.of(input))
        second.conduit.chips[Direction.EAST.get3DDataValue()].setSlot(0, IItemStack.of(output))
        return source to target
    }

    /** An input chip allowing diamonds skips the cobblestone before them. */
    private fun filteredInput(helper: GameTestHelper) {
        val (source, target) = filteredLine(helper,
            filtered(ItemChipKind, Direction.WEST, ConnectionDirection.INPUT, ItemStack(Items.DIAMOND)),
            filtered(ItemChipKind, Direction.EAST, ConnectionDirection.OUTPUT))
        source.setItem(0, ItemStack(Items.COBBLESTONE, 3))
        source.setItem(1, ItemStack(Items.DIAMOND, 2))
        helper.succeedWhen {
            // One item per operation, then a 200-tick countdown: the first diamond is enough.
            helper.assertTrue((0 until target.containerSize).any { target.getItem(it).`is`(Items.DIAMOND) }, "a diamond arrived")
            helper.assertTrue((0 until target.containerSize).none { target.getItem(it).`is`(Items.COBBLESTONE) }, "no cobblestone arrived")
            helper.assertValueEqual(source.getItem(0).count, 3, "cobblestone stayed")
        }
    }

    /** An output chip allowing only dirt takes no cobblestone, which waits in the input chip's buffer. */
    private fun filteredOutput(helper: GameTestHelper) {
        val (source, target) = filteredLine(helper,
            filtered(ItemChipKind, Direction.WEST, ConnectionDirection.INPUT),
            filtered(ItemChipKind, Direction.EAST, ConnectionDirection.OUTPUT, ItemStack(Items.DIRT)))
        source.setItem(0, ItemStack(Items.COBBLESTONE, 3))
        helper.runAfterDelay(20) {
            helper.assertTrue(target.isEmpty, "the dirt-only output took nothing")
            helper.succeed()
        }
    }

    /**
     * An input chip on a vault's outer face, filtered to emeralds, takes them through the vault's index (iron in the
     * vault stays); an inner face exposes no index.
     */
    private fun filteredVault(helper: GameTestHelper) {
        val at = BlockPos(3, 1, 3)
        helper.assertTrue(com.itszuvalex.femtocraft.industry.FrameMultiblocks.ITEM_VAULT.formAt(helper.level, helper.absolutePos(at)), "vault formed")
        val vault = helper.getBlockEntity(at, com.itszuvalex.femtocraft.logistics.ItemVaultBlockEntity::class.java)
        val storage = vault.state()!!.storage
        repeat(3) { storage.insert(IItemStack.of(ItemStack(Items.IRON_INGOT, 64))) }
        storage.insert(IItemStack.of(ItemStack(Items.EMERALD, 5)))
        val west = helper.getBlockEntity(at.offset(0, 1, 1), com.itszuvalex.femtocraft.logistics.ItemVaultBlockEntity::class.java)
        val index = west.getModule(com.itszuvalex.femtocraft.logistics.LogisticsModules.ITEM_INDEX, Direction.WEST)
        helper.assertTrue(index != null, "an outer face exposes the index")
        helper.assertTrue(west.getModule(com.itszuvalex.femtocraft.logistics.LogisticsModules.ITEM_INDEX, Direction.EAST) == null, "an inner face does not")
        val direct = ItemChipKind.pullIndexed(index!!, ItemStack.EMPTY, 2, com.itszuvalex.itszulib.api.filter.ResourceFilter(ItemChipKind.filterKind, ChipKind.FILTER_SLOTS, listOf(ItemStack(Items.EMERALD))))
        helper.assertTrue(direct.`is`(Items.EMERALD) && direct.count == 2, "the index gives emeralds, got $direct")
        storage.insert(IItemStack.of(direct))

        val conduit = helper.place<ConduitBlockEntity>(BlockPos(2, 2, 4), LogisticsContent.CONDUIT.get())
        val chest = helper.place<ChestBlockEntity>(BlockPos(1, 2, 4), Blocks.CHEST)
        conduit.conduit.chips[Direction.EAST.get3DDataValue()].setSlot(0, IItemStack.of(filtered(ItemChipKind, Direction.EAST, ConnectionDirection.INPUT, ItemStack(Items.EMERALD))))
        conduit.conduit.chips[Direction.WEST.get3DDataValue()].setSlot(0, IItemStack.of(filtered(ItemChipKind, Direction.WEST, ConnectionDirection.OUTPUT)))
        val emerald = BuiltInRegistries.ITEM.getKey(Items.EMERALD)
        val iron = BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT)
        helper.succeedWhen {
            helper.assertTrue((0 until chest.containerSize).any { chest.getItem(it).`is`(Items.EMERALD) }, "an emerald arrived")
            helper.assertTrue((0 until chest.containerSize).none { chest.getItem(it).`is`(Items.IRON_INGOT) }, "no iron arrived")
            helper.assertTrue(vault.index()!!.count(emerald) < 5L, "emeralds left the vault")
            helper.assertValueEqual(vault.index()!!.count(iron), 192L, "iron stayed")
        }
    }

    /**
     * The conduit menu changes a chip's ItszuLib filter: cells set from the held item (or the held bucket's fluid), an
     * empty hand clears, and allow/deny and component matching toggle; nanite chips take no filter.
     */
    private fun filterMenu(helper: GameTestHelper) {
        val be = helper.place<ConduitBlockEntity>(CENTER, LogisticsContent.CONDUIT.get())
        val chips = be.conduit.chips[Direction.UP.get3DDataValue()]
        chips.setSlot(0, IItemStack.of(chip(Direction.UP)))
        chips.setSlot(1, IItemStack.of(chip(Direction.UP, kind = FluidChipKind)))
        chips.setSlot(2, IItemStack.of(chip(Direction.UP, kind = NaniteChipKind)))
        val player = helper.makeMockServerPlayerInLevel()
        val menu = ConduitMenu(1, player.inventory, be)
        val up = Direction.UP.get3DDataValue()
        val actions = com.itszuvalex.itszulib.api.filter.FilterActions
        fun act(index: Int, action: Int) = menu.handleAction(player, ConduitMenu.ACTION_FILTER, ConduitMenu.filterData(up, index, action))
        fun itemFilter() = chips.get(0).toMinecraft().get(ItemChipKind.component)!!.filter

        menu.setCarried(ItemStack(Items.DIAMOND, 7))
        helper.assertTrue(act(0, actions.set(3)), "item filter set")
        helper.assertTrue(itemFilter().entries[3].`is`(Items.DIAMOND) && itemFilter().entries[3].count == 1, "one diamond in entry 3, got ${itemFilter().entries[3]}")
        helper.assertValueEqual(menu.carried.count, 7, "the held stack is not used up")
        helper.assertFalse(act(1, actions.set(0)), "a diamond is no fluid filter")
        helper.assertFalse(act(2, actions.set(0)), "nanite chips take no filter")

        helper.assertTrue(act(0, actions.mode()), "mode toggled")
        helper.assertTrue(itemFilter().mode == com.itszuvalex.itszulib.api.filter.FilterMode.DENY, "now a denylist")
        helper.assertFalse(itemFilter().test(ItemStack(Items.DIAMOND)), "diamonds kept out")
        helper.assertTrue(act(0, actions.components()), "component matching toggled")
        helper.assertFalse(itemFilter().matchComponents, "matching the item alone")

        menu.setCarried(ItemStack(Items.WATER_BUCKET))
        helper.assertTrue(act(1, actions.set(0)), "fluid filter set from a bucket")
        helper.assertTrue(chips.get(1).toMinecraft().get(FluidChipKind.component)!!.filter.entries[0].fluid == Fluids.WATER, "water listed")

        menu.setCarried(ItemStack.EMPTY)
        helper.assertTrue(act(0, actions.set(3)), "cleared")
        helper.assertTrue(itemFilter().isEmpty, "nothing listed: the item chip takes anything again")
        helper.succeed()
    }
}
