package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.industry.FrameMultiblocks
import com.itszuvalex.femtocraft.logistics.ChipData
import com.itszuvalex.femtocraft.logistics.ItemRepositoryBlockEntity
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.femtocraft.logistics.ChipKind
import com.itszuvalex.femtocraft.logistics.Chips
import com.itszuvalex.femtocraft.logistics.ConnectionDirection
import com.itszuvalex.femtocraft.logistics.ConnectionSettings
import com.itszuvalex.femtocraft.logistics.FluidChipKind
import com.itszuvalex.femtocraft.logistics.ItemChipKind
import com.itszuvalex.femtocraft.logistics.NaniteChipKind
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.NaniteStrainVersion
import com.itszuvalex.itszulib.api.filter.FilterMode
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.material.Fluids
import net.neoforged.neoforge.fluids.FluidStack

/**
 * Game tests for the plumbing that other systems stand on, where a bug would show up far from its cause: what the
 * frames ask for, and what chips remember.
 */
object InfrastructureGameTests {
    fun register() {
        DevGameTests.test("frame_requirement_slots_cover_every_requirement", body = ::frameSlots)
        DevGameTests.test("chip_data_round_trips_through_its_codec", body = ::chipCodec)
        DevGameTests.test("repositories_keep_their_contents_and_say_what_they_hold", body = ::repositories)
    }

    private fun lines(helper: GameTestHelper, stack: ItemStack): List<String> =
        stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.of(helper.level), null, net.minecraft.world.item.TooltipFlag.NORMAL).map { it.string }

    /** Repositories keep what they hold on the dropped item, which says how much it holds and can hold. */
    private fun repositories(helper: GameTestHelper) {
        val at = net.minecraft.core.BlockPos(4, 1, 4)
        fun fresh(item: net.minecraft.world.item.Item) = lines(helper, ItemStack(item))
        helper.assertTrue(fresh(LogisticsContent.ITEM_REPOSITORY.get().asItem()).any { "0 / 54 slots" in it }, "item repository holds 54 slots")
        helper.assertTrue(fresh(LogisticsContent.FLUID_REPOSITORY.get().asItem()).any { "0 / 5,000 mB" in it }, "fluid repository holds 5,000 mB")
        helper.assertTrue(fresh(LogisticsContent.NANITE_REPOSITORY.get().asItem()).any { "Nanites: 0 / 250" in it }, "nanite repository holds 250 nanites")

        helper.setBlock(at, LogisticsContent.ITEM_REPOSITORY.get())
        val repository = helper.getBlockEntity(at, ItemRepositoryBlockEntity::class.java)
        repository.storage.setSlot(0, com.itszuvalex.itszulib.api.adapters.IItemStack.of(ItemStack(Items.DIAMOND, 40)))
        repository.storage.setSlot(5, com.itszuvalex.itszulib.api.adapters.IItemStack.of(ItemStack(Items.GOLD_INGOT, 3)))
        helper.level.destroyBlock(helper.absolutePos(at), true)
        val drops = helper.level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity::class.java, net.minecraft.world.phys.AABB(helper.absolutePos(at)).inflate(3.0)).map { it.item }
        helper.assertValueEqual(drops.size, 1, "one item drops, not its contents: $drops")
        helper.assertTrue(lines(helper, drops[0]).any { "2 / 54 slots used (43 items)" in it }, "it says what it holds: ${lines(helper, drops[0])}")
        helper.succeed()
    }

    /** Each requirement is covered exactly, in slots no bigger than a stack, in every frame multiblock. */
    private fun frameSlots(helper: GameTestHelper) {
        val problems = ArrayList<String>()
        for (multiblock in FrameMultiblocks.all()) {
            val required = multiblock.required()
            val slots = multiblock.requirementSlots()
            for (slot in slots) {
                if (slot.isEmpty) problems += "${multiblock.id} has an empty requirement slot"
                if (slot.count > slot.maxStackSize) problems += "${multiblock.id} has a slot of ${slot.count} ${slot.item}, over a stack"
            }
            for (need in required) {
                val given = slots.filter { ItemStack.isSameItemSameComponents(it, need) }.sumOf { it.count }
                if (given != need.count) problems += "${multiblock.id} asks for ${need.count} ${need.item} but its slots take $given"
            }
            if (slots.sumOf { it.count } != required.sumOf { it.count }) problems += "${multiblock.id}'s slots hold more than it requires"
        }
        helper.assertValueEqual(problems, emptyList<String>(), "frame problems")
        helper.succeed()
    }

    private fun <B : Any> roundTrip(helper: GameTestHelper, kind: ChipKind<B>, data: ChipData<B>): String? {
        val ops = RegistryOps.create(NbtOps.INSTANCE, helper.level.registryAccess())
        val encoded = kind.codec.encodeStart(ops, data).result()
        if (encoded.isEmpty) return "${kind.name} chip data does not encode"
        val decoded = kind.codec.parse(ops, encoded.get()).result()
        if (decoded.isEmpty) return "${kind.name} chip data does not decode"
        return if (decoded.get() == data) null else "${kind.name} chip data changed in a round trip: ${decoded.get()} vs $data"
    }

    private fun <B : Any> roundTripDefaults(helper: GameTestHelper, kind: ChipKind<B>) = roundTrip(helper, kind, kind.defaults(null))

    /** Settings, buffer and filter (mode, component matching and entries) survive saving, for every kind of chip. */
    private fun chipCodec(helper: GameTestHelper) {
        val settings = ConnectionSettings(1234.5, "red", true, ConnectionDirection.OUTPUT, Direction.EAST)
        val problems = ArrayList<String>()

        val items = ItemChipKind.defaults(Direction.UP).with(
            settings, ItemStack(Items.DIAMOND, 7),
            ItemChipKind.emptyFilter().withEntry(2, ItemStack(Items.GOLD_INGOT)).withMode(FilterMode.DENY).withMatchComponents(true),
        )
        val fluids = FluidChipKind.defaults(Direction.DOWN).with(
            settings, FluidStack(Fluids.WATER, 600),
            FluidChipKind.emptyFilter().withEntry(0, FluidStack(Fluids.LAVA, 1)),
        )
        val nanites = NaniteChipKind.defaults(Direction.NORTH).with(settings, NaniteStack("Dumb", "Dumb", NaniteStrainVersion(1, 2), 5))
        roundTrip(helper, ItemChipKind, items)?.let { problems += it }
        roundTrip(helper, FluidChipKind, fluids)?.let { problems += it }
        roundTrip(helper, NaniteChipKind, nanites)?.let { problems += it }
        for (kind in Chips.KINDS) roundTripDefaults(helper, kind)?.let { problems += it }

        helper.assertValueEqual(problems, emptyList<String>(), "chip codec problems")
        helper.succeed()
    }
}
