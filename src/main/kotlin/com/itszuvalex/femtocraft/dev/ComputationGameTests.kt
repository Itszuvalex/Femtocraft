package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.archive.ArchiveBlockEntity
import com.itszuvalex.femtocraft.archive.ArchiveContent
import com.itszuvalex.femtocraft.computation.ArchiveInterfaceBlockEntity
import com.itszuvalex.femtocraft.computation.ComputationConduitBlockEntity
import com.itszuvalex.femtocraft.computation.ComputationContent
import com.itszuvalex.femtocraft.computation.MainframeBlockEntity
import com.itszuvalex.femtocraft.computation.MainframeHeat
import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.logistics.ConduitBlockEntity
import com.itszuvalex.femtocraft.logistics.ItemChipKind
import com.itszuvalex.femtocraft.logistics.LogisticsContent
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.research.TechTree
import com.itszuvalex.itszulib.team.Research
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.resources.Identifier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.GameType
import net.minecraft.world.level.block.Blocks

/**
 * Game tests for computation (DECISIONS D19): mainframes, computation conduits, the Archive Interface and logistics
 * conduits as jobs.
 */
object ComputationGameTests {
    private val ARCHIVE = BlockPos(3, 1, 3)
    private val METALLURGY = Identifier.fromNamespaceAndPath(Femtocraft.ID, "metallurgy")

    fun register() {
        DevGameTests.test("mainframe_computes_research_for_an_archive", 200, ::archiveResearch)
        DevGameTests.test("mainframe_idles_without_a_job", 60, ::idles)
        DevGameTests.test("mainframe_cooling_from_cold_neighbours", body = ::cooling)
        DevGameTests.test("computation_speeds_up_logistics_chips", 60, ::logistics)
    }

    private fun mainframe(helper: GameTestHelper, pos: BlockPos, cores: Int = MainframeBlockEntity.SLOTS): MainframeBlockEntity {
        val be = helper.place<MainframeBlockEntity>(pos, ComputationContent.MAINFRAME.get())
        repeat(cores) { be.processors.setSlot(it, IItemStack.of(ItemStack(ComputationContent.MICRO_LOGIC_CORE.get()))) }
        be.battery.setStorage(MainframeBlockEntity.BATTERY_SIZE)
        return be
    }

    /**
     * mainframe - conduit - Archive Interface - Archive along x: once the team has a focus, the mainframe spends power
     * and the Archive gets computed points (and spends them on the focus). No nanite host is needed.
     */
    private fun archiveResearch(helper: GameTestHelper) {
        helper.assertTrue(ArchiveContent.MULTIBLOCK.formAt(helper.level, helper.absolutePos(ARCHIVE)), "archive formed")
        val archive = helper.getBlockEntity(ARCHIVE, ArchiveBlockEntity::class.java).state()!!
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        ItszuLib.TEAMS.change { it.ensurePlayer(player.uuid, "computation_test") }
        val team = ItszuLib.TEAMS.state.teamOf(player.uuid)!!.id
        archive.claim(player.uuid)
        helper.place<ArchiveInterfaceBlockEntity>(ARCHIVE.west(), ComputationContent.ARCHIVE_INTERFACE.get())
        val conduit = helper.place<ComputationConduitBlockEntity>(ARCHIVE.west(2), ComputationContent.COMPUTATION_CONDUIT.get())
        val mainframe = mainframe(helper, ARCHIVE.west(3))
        TechTree.queue(helper.level.server, team, METALLURGY)
        fun research() = ItszuLib.TEAMS.state.team(team)!![Research.TYPE]
        helper.succeedWhen {
            helper.assertTrue(conduit.conduit.leafFaces[Direction.EAST] && conduit.conduit.leafFaces[Direction.WEST], "conduit attached to both")
            helper.assertTrue(mainframe.battery.storage() < MainframeBlockEntity.BATTERY_SIZE, "mainframe spent power")
            helper.assertTrue(research().has(METALLURGY) || (research().progress[METALLURGY] ?: 0L) > 0L, "research progressed")
        }
    }

    /**
     * A mainframe on a conduit with nothing to compute for spends nothing.
     */
    private fun idles(helper: GameTestHelper) {
        val mainframe = mainframe(helper, BlockPos(2, 1, 2))
        helper.place<ComputationConduitBlockEntity>(BlockPos(3, 1, 2), ComputationContent.COMPUTATION_CONDUIT.get())
        helper.runAfterDelay(40) {
            helper.assertValueEqual(mainframe.battery.storage(), MainframeBlockEntity.BATTERY_SIZE, "no job, no power spent")
            helper.assertValueEqual(mainframe.lastFlops, 0.0, "nothing computed")
            helper.succeed()
        }
    }

    /**
     * Ice, packed ice and water beside a mainframe add to its cooling; other blocks do not.
     */
    private fun cooling(helper: GameTestHelper) {
        val mainframe = mainframe(helper, BlockPos(4, 1, 4))
        helper.assertValueEqual(mainframe.coolingRate(), MainframeHeat.BASE_COOLING, "nothing around")
        helper.setBlock(BlockPos(5, 1, 4), Blocks.PACKED_ICE)
        helper.setBlock(BlockPos(3, 1, 4), Blocks.STONE)
        helper.setBlock(BlockPos(4, 2, 4), Blocks.WATER)
        helper.assertTrue(kotlin.math.abs(mainframe.coolingRate() - (MainframeHeat.BASE_COOLING + 0.04 + 0.01)) < 1e-9, "rate ${mainframe.coolingRate()}")
        helper.succeed()
    }

    /**
     * A logistics conduit with an input chip beside a computation conduit and a mainframe: the chip's countdown runs
     * down faster than its passive rate (25 flops a tick).
     */
    private fun logistics(helper: GameTestHelper) {
        val logistics = helper.place<ConduitBlockEntity>(BlockPos(4, 1, 4), LogisticsContent.CONDUIT.get())
        helper.place<ComputationConduitBlockEntity>(BlockPos(5, 1, 4), ComputationContent.COMPUTATION_CONDUIT.get())
        val mainframe = mainframe(helper, BlockPos(6, 1, 4))
        val chip = ItemStack(LogisticsContent.ITEM_CHIP.get())
        val d = ItemChipKind.defaults(Direction.WEST)
        chip.set(ItemChipKind.component, d.with(settings = d.settings.copy(flops = 1_000_000.0)))
        val slots = logistics.conduit.chips[Direction.WEST.get3DDataValue()]
        slots.setSlot(0, IItemStack.of(chip))
        helper.runAfterDelay(40) {
            val left = slots.counter(0).flops
            helper.assertTrue(1_000_000.0 - left > 40 * 25.0 * 2, "counted down faster than passive: ${1_000_000.0 - left}")
            helper.assertTrue(mainframe.battery.storage() < MainframeBlockEntity.BATTERY_SIZE, "mainframe spent power")
            helper.succeed()
        }
    }
}
