package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.computation.ComputationConduitBlockEntity
import com.itszuvalex.femtocraft.computation.ComputationContent
import com.itszuvalex.femtocraft.computation.ComputationModules
import com.itszuvalex.femtocraft.computation.ConduitBlend
import com.itszuvalex.femtocraft.computation.MainframeBlockEntity
import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.industry.Autocrafter
import com.itszuvalex.femtocraft.industry.AutocrafterBlockEntity
import com.itszuvalex.femtocraft.industry.AutocrafterMenu
import com.itszuvalex.femtocraft.industry.CraftingTemplateItem
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.power.CrystalMountBlockEntity
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.power.PowerCrystalData
import com.itszuvalex.femtocraft.power.PowerCrystals
import com.itszuvalex.itszulib.api.adapters.IItemStack
import net.minecraft.core.BlockPos
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.inventory.ContainerInput
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType

/**
 * Game tests for the autocrafter and the computation conduit's colour (DECISIONS D31).
 */
object AutocrafterGameTests {
    private val POS = BlockPos(4, 1, 4)

    fun register() {
        DevGameTests.test("autocrafter_sets_recipe_from_ghosts", body = ::setsFromGhosts)
        DevGameTests.test("autocrafter_sets_recipe_from_real_items", body = ::setsFromRealItems)
        DevGameTests.test("autocrafter_holder_slots_take_what_the_recipe_accepts", body = ::holderSlots)
        DevGameTests.test("autocrafter_crafts_with_remainders", body = ::remainders)
        DevGameTests.test("autocrafter_clear_recipe_hands_items_back", body = ::clear)
        DevGameTests.test("autocrafter_menu_clicks_set_ghosts", body = ::menuGhosts)
        DevGameTests.test("autocrafter_crafts_on_mainframe_flops", 200, ::craftsOnFlops)
        DevGameTests.test("autocrafter_idle_takes_no_flops", 60, ::idle)
        DevGameTests.test("computation_conduit_blends_attached_colours", 100, ::conduitColours)
    }

    private fun autocrafter(helper: GameTestHelper, pos: BlockPos = POS): AutocrafterBlockEntity {
        val be = helper.place<AutocrafterBlockEntity>(pos, IndustryContent.AUTOCRAFTER.get())
        be.inventory.setSlot(Autocrafter.TEMPLATE_SLOT, IItemStack.of(ItemStack(IndustryContent.CRAFTING_TEMPLATE.get())))
        be.battery.setStorage(Autocrafter.BATTERY_SIZE)
        return be
    }

    /** Sticks: planks in grid slots 0 and 3. */
    private fun sticks(be: AutocrafterBlockEntity) {
        be.setGhost(0, ItemStack(Items.OAK_PLANKS))
        be.setGhost(3, ItemStack(Items.OAK_PLANKS))
        check(be.setRecipe()) { "stick recipe set" }
    }

    private fun outputCount(be: AutocrafterBlockEntity, item: net.minecraft.world.item.Item): Int =
        (0 until Autocrafter.OUTPUTS).sumOf { be.outputs.get(it).toMinecraft().let { s -> if (s.`is`(item)) s.count else 0 } }

    private fun setsFromGhosts(helper: GameTestHelper) {
        val be = autocrafter(helper)
        helper.assertFalse(be.setRecipe(), "an empty grid makes nothing")
        be.setGhost(0, ItemStack(Items.OAK_PLANKS, 5))
        be.setGhost(3, ItemStack(Items.OAK_PLANKS))
        helper.assertValueEqual(be.previewResult().count, 4, "preview while editing")
        helper.assertTrue(be.setRecipe(), "set")
        val pattern = CraftingTemplateItem.pattern(be.template())
        helper.assertTrue(pattern != null, "template encoded")
        helper.assertTrue(pattern!!.resultStack().`is`(Items.STICK) && pattern.resultStack().count == 4, "result is 4 sticks")
        helper.assertValueEqual(pattern.items()[0].count, 1, "examples are one each")
        helper.assertTrue(be.holders.isEmpty(), "no items were needed")
        helper.assertFalse(be.setGhost(1, ItemStack(Items.STONE)), "ghosts are fixed while encoded")
        helper.assertFalse(be.setRecipe(), "an encoded template is not overwritten")
        helper.succeed()
    }

    private fun setsFromRealItems(helper: GameTestHelper) {
        val be = autocrafter(helper)
        be.setGhost(0, ItemStack(Items.STONE))
        be.holders.setSlot(1, IItemStack.of(ItemStack(Items.BIRCH_PLANKS, 3)))
        be.holders.setSlot(4, IItemStack.of(ItemStack(Items.BIRCH_PLANKS, 2)))
        be.setGhost(0, ItemStack.EMPTY)
        helper.assertTrue(be.setRecipe(), "set from the grid's items")
        val items = be.pattern()!!.items()
        helper.assertTrue(items[1].`is`(Items.BIRCH_PLANKS) && items[4].`is`(Items.BIRCH_PLANKS) && items[0].isEmpty, "pattern from real items")
        helper.assertValueEqual(be.holders.get(1).toMinecraft().count, 3, "real items stay")
        helper.assertTrue(be.job.add(Autocrafter.FLOPS_PER_CRAFT) > 0.0, "takes FLOPS while it can craft")
        helper.assertTrue(be.tryCraft(), "crafted")
        helper.assertValueEqual(outputCount(be, Items.STICK), 4, "sticks out")
        helper.assertValueEqual(be.holders.get(1).toMinecraft().count, 2, "one taken from each slot")
        helper.assertValueEqual(be.holders.get(4).toMinecraft().count, 1, "one taken from each slot")
        helper.assertValueEqual(be.battery.storage(), Autocrafter.BATTERY_SIZE - Autocrafter.POWER_PER_CRAFT, "power spent")
        helper.assertFalse(be.tryCraft(), "no FLOPS left for another")
        helper.succeed()
    }

    private fun holderSlots(helper: GameTestHelper) {
        val be = autocrafter(helper)
        helper.assertFalse(be.inventory.canInsert(0, IItemStack.of(ItemStack(Items.OAK_PLANKS))), "nothing goes in while editing")
        sticks(be)
        helper.assertTrue(be.accepts(0, ItemStack(Items.BIRCH_PLANKS)), "other planks fit the recipe")
        helper.assertFalse(be.accepts(0, ItemStack(Items.STONE)), "stone does not")
        helper.assertFalse(be.accepts(1, ItemStack(Items.OAK_PLANKS)), "nothing goes where the recipe has nothing")
        helper.assertValueEqual(be.capacity(0), Autocrafter.BUFFERED_CRAFTS, "N x M = 1 x 4")
        val left = be.holders.insert(0, IItemStack.of(ItemStack(Items.OAK_PLANKS, 64)))
        helper.assertValueEqual(be.holders.get(0).toMinecraft().count, Autocrafter.BUFFERED_CRAFTS, "holds M")
        helper.assertValueEqual(left.toMinecraft().count, 64 - Autocrafter.BUFFERED_CRAFTS, "the rest refused")
        helper.assertFalse(be.inventory.canInsert(Autocrafter.FIRST_OUTPUT, IItemStack.of(ItemStack(Items.STICK))), "outputs take nothing from outside")
        helper.assertFalse(be.inventory.canInsert(Autocrafter.TEMPLATE_SLOT, IItemStack.of(ItemStack(Items.PAPER))), "template slot takes templates")
        helper.succeed()
    }

    /** Sugar from a honey bottle leaves its glass bottle, which goes to the output. */
    private fun remainders(helper: GameTestHelper) {
        val be = autocrafter(helper)
        be.setGhost(4, ItemStack(Items.HONEY_BOTTLE))
        helper.assertTrue(be.setRecipe(), "set")
        helper.assertValueEqual(be.capacity(4), Autocrafter.BUFFERED_CRAFTS, "M honey bottles")
        be.holders.insert(4, IItemStack.of(ItemStack(Items.HONEY_BOTTLE, 2)))
        be.job.add(Autocrafter.FLOPS_PER_CRAFT)
        helper.assertTrue(be.tryCraft(), "crafted")
        helper.assertValueEqual(outputCount(be, Items.SUGAR), 3, "sugar")
        helper.assertValueEqual(outputCount(be, Items.GLASS_BOTTLE), 1, "the bottle comes back")
        helper.succeed()
    }

    private fun clear(helper: GameTestHelper) {
        val be = autocrafter(helper)
        sticks(be)
        be.holders.insert(0, IItemStack.of(ItemStack(Items.OAK_PLANKS, 3)))
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        helper.assertTrue(be.clearRecipe(player), "cleared")
        helper.assertTrue(CraftingTemplateItem.isBlank(be.template()), "template blank again")
        helper.assertTrue(be.holders.isEmpty(), "grid emptied")
        helper.assertValueEqual(player.inventory.countItem(Items.OAK_PLANKS), 3, "items handed back")
        helper.assertTrue(be.ghost(0).`is`(Items.OAK_PLANKS), "ghosts keep the old pattern to edit")
        helper.assertFalse(be.clearRecipe(player), "nothing to clear")
        helper.succeed()
    }

    /**
     * While editing, clicking a grid slot with an item sets its ghost and takes nothing; with an empty hand it clears
     * it. Once encoded, clicks put items in as usual.
     */
    private fun menuGhosts(helper: GameTestHelper) {
        val be = autocrafter(helper)
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        val menu = AutocrafterMenu(1, player.inventory, be)
        menu.setCarried(ItemStack(Items.OAK_PLANKS, 10))
        menu.clicked(0, 0, ContainerInput.PICKUP, player)
        menu.clicked(3, 0, ContainerInput.PICKUP, player)
        menu.clicked(5, 0, ContainerInput.PICKUP, player)
        helper.assertValueEqual(menu.carried.count, 10, "nothing taken")
        helper.assertTrue(be.holders.isEmpty(), "no items placed")
        helper.assertTrue(be.ghost(5).`is`(Items.OAK_PLANKS), "ghost set")
        menu.setCarried(ItemStack.EMPTY)
        menu.clicked(5, 0, ContainerInput.PICKUP, player)
        helper.assertTrue(be.ghost(5).isEmpty, "ghost cleared")
        helper.assertTrue(menu.handleAction(player, AutocrafterMenu.ACTION_SET, 0), "set recipe action")
        menu.setCarried(ItemStack(Items.OAK_PLANKS, 10))
        menu.clicked(0, 0, ContainerInput.PICKUP, player)
        helper.assertValueEqual(be.holders.get(0).toMinecraft().count, Autocrafter.BUFFERED_CRAFTS, "items go in once encoded, up to M")
        helper.assertValueEqual(menu.carried.count, 10 - Autocrafter.BUFFERED_CRAFTS, "taken from the hand")
        helper.succeed()
    }

    private fun mainframe(helper: GameTestHelper, pos: BlockPos): MainframeBlockEntity {
        val be = helper.place<MainframeBlockEntity>(pos, ComputationContent.MAINFRAME.get())
        repeat(MainframeBlockEntity.SLOTS) { be.processors.setSlot(it, IItemStack.of(ItemStack(ComputationContent.MICRO_LOGIC_CORE.get()))) }
        be.battery.setStorage(MainframeBlockEntity.BATTERY_SIZE)
        return be
    }

    /** mainframe - conduit - autocrafter: the autocrafter crafts on the mainframe's FLOPS, the mainframe spends power. */
    private fun craftsOnFlops(helper: GameTestHelper) {
        val be = autocrafter(helper)
        sticks(be)
        be.holders.insert(0, IItemStack.of(ItemStack(Items.OAK_PLANKS, 16)))
        be.holders.insert(3, IItemStack.of(ItemStack(Items.OAK_PLANKS, 16)))
        // Refill as it crafts, as a pipe would.
        helper.onEachTick {
            be.holders.insert(0, IItemStack.of(ItemStack(Items.OAK_PLANKS, 4)))
            be.holders.insert(3, IItemStack.of(ItemStack(Items.OAK_PLANKS, 4)))
        }
        helper.place<ComputationConduitBlockEntity>(POS.west(), ComputationContent.COMPUTATION_CONDUIT.get())
        val mainframe = mainframe(helper, POS.west(2))
        helper.succeedWhen {
            helper.assertTrue(be.crafted >= 3, "crafted ${be.crafted}")
            helper.assertTrue(outputCount(be, Items.STICK) >= 12, "sticks out")
            helper.assertTrue(mainframe.battery.storage() < MainframeBlockEntity.BATTERY_SIZE, "mainframe spent power")
        }
    }

    /** Without a recipe, an autocrafter is no job: the mainframe beside it computes nothing. */
    private fun idle(helper: GameTestHelper) {
        val be = autocrafter(helper)
        helper.place<ComputationConduitBlockEntity>(POS.west(), ComputationContent.COMPUTATION_CONDUIT.get())
        val mainframe = mainframe(helper, POS.west(2))
        helper.assertTrue(be.getModule(ComputationModules.LEAF, null)?.computation() == null, "no participant")
        helper.runAfterDelay(40) {
            helper.assertValueEqual(mainframe.battery.storage(), MainframeBlockEntity.BATTERY_SIZE, "no power spent")
            helper.assertValueEqual(mainframe.lastFlops, 0.0, "nothing computed")
            helper.succeed()
        }
    }

    private fun mount(helper: GameTestHelper, pos: BlockPos, color: Int) {
        val be = helper.place<CrystalMountBlockEntity>(pos, PowerContent.CRYSTAL_MOUNT.get())
        val crystal = ItemStack(PowerContent.POWER_CRYSTAL.get())
        PowerCrystals.set(crystal, PowerCrystalData("Test", PowerCrystals.TYPE_SMALL, color, 0f, 100.0, 0.0, 0.0, 1000.0))
        be.storage.setSlot(0, IItemStack.of(crystal))
    }

    /**
     * red mount, mainframe, three conduits, mainframe, blue mount along x: each mainframe takes its nearest mount's
     * colour, the end conduits take their mainframe's, and the middle one blends them.
     */
    private fun conduitColours(helper: GameTestHelper) {
        val red = 0xFF0000
        val blue = 0x0000FF
        mount(helper, BlockPos(1, 1, 4), red)
        mount(helper, BlockPos(7, 1, 4), blue)
        mainframe(helper, BlockPos(2, 1, 4))
        mainframe(helper, BlockPos(6, 1, 4))
        val conduits = (3..5).map { helper.place<ComputationConduitBlockEntity>(BlockPos(it, 1, 4), ComputationContent.COMPUTATION_CONDUIT.get()) }
        helper.succeedWhen {
            conduits.forEach { it.updateBlend() }
            helper.assertValueEqual(conduits[0].blend?.color ?: 0, red or 0xFF000000.toInt(), "west end")
            helper.assertValueEqual(conduits[2].blend?.color ?: 0, blue or 0xFF000000.toInt(), "east end")
            helper.assertValueEqual(conduits[1].blend ?: ConduitBlend(0, -1), ConduitBlend(ConduitBlend.average(listOf(red or 0xFF000000.toInt(), blue or 0xFF000000.toInt())), 1), "middle blends")
        }
    }
}
