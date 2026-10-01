package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.industry.ConfiguratorItem
import com.itszuvalex.femtocraft.industry.ConfiguratorMode
import com.itszuvalex.femtocraft.industry.IndustryContent
import com.itszuvalex.femtocraft.nanite.CybermaterialNanites
import com.itszuvalex.femtocraft.nanite.NaniteContent
import com.itszuvalex.femtocraft.nanite.NaniteExtractorBlockEntity
import com.itszuvalex.femtocraft.nanite.NaniteInfuserBlockEntity
import com.itszuvalex.femtocraft.nanite.NaniteMachineMenu
import com.itszuvalex.femtocraft.nanite.NaniteModules
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.NaniteStrainVersion
import com.itszuvalex.femtocraft.nanite.NaniteTank
import com.itszuvalex.femtocraft.nanite.NanoLashEntity
import com.itszuvalex.femtocraft.nanite.PlayerNanites
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.core.EnumAutomaticIO
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.NbtOps
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items

/**
 * Game tests for the nanite area.
 */
object NaniteGameTests {
    private val CENTER = BlockPos(4, 1, 4)

    fun register() {
        DevGameTests.test("nanite_tank_fill_drain_save", body = ::tank)
        DevGameTests.test("nanite_extractor_extracts_cybermaterial", 400, ::extractor)
        DevGameTests.test("nanite_infuser_activates_riftiron", 600, ::infuser)
        DevGameTests.test("nanite_infuser_waits_for_nanites", 200, ::infuserNeedsNanites)
        DevGameTests.test("nanite_auto_io_moves_between_machines", body = ::autoIO)
        DevGameTests.test("player_nanites_fill_and_drain_machine", body = ::playerTransfer)
        DevGameTests.test("configurator_nanite_mode_cycles_nanite_faces", body = ::configurator)
        DevGameTests.test("nano_lash_throws_and_cools_down", body = ::nanoLash)
    }

    private fun tank(helper: GameTestHelper) {
        val tank = NaniteTank(10)
        val other = NaniteStack("Dumb", "Dumb", NaniteStrainVersion(1, 0), 3)
        helper.assertValueEqual(tank.fill(NaniteRegistry.dumb(8), true).amount, 0, "fits")
        helper.assertValueEqual(tank.fill(other, true).amount, 1, "only two more fit (shared capacity)")
        helper.assertValueEqual(tank.contents().size, 2, "two versions kept apart")
        helper.assertValueEqual(tank.drain(NaniteRegistry.dumb(5), true).amount, 5, "drain by strain and version")
        val ops = helper.level.registryAccess().createSerializationContext(NbtOps.INSTANCE)
        val tag = NaniteTank.LIST.encodeStart(ops, tank.contents()).getOrThrow()
        helper.assertTrue(NaniteTank.LIST.parse(ops, tag).getOrThrow() == tank.contents(), "codec round trip")
        helper.succeed()
    }

    private fun extractor(helper: GameTestHelper) {
        CybermaterialNanites.register(Items.SLIME_BALL, NaniteRegistry.dumb(2))
        val be = helper.place<NaniteExtractorBlockEntity>(CENTER, NaniteContent.NANITE_EXTRACTOR.get())
        be.battery.setStorage(5000.0)
        be.inventory.setSlot(0, IItemStack.of(ItemStack(Items.SLIME_BALL)))
        helper.succeedWhen {
            helper.assertValueEqual(be.naniteTank.amount, 2, "nanites extracted")
            CybermaterialNanites.unregister(Items.SLIME_BALL)
        }
    }

    private fun infuser(helper: GameTestHelper) {
        val be = helper.place<NaniteInfuserBlockEntity>(CENTER, NaniteContent.NANITE_INFUSER.get())
        be.battery.setStorage(4000.0)
        be.naniteTank.fill(NaniteRegistry.dumb(1), true)
        be.inventory.setSlot(0, IItemStack.of(ItemStack(IndustryContent.RIFTIRON_INGOT_DEVOID.get())))
        helper.succeedWhen {
            helper.assertTrue(be.inventory.get(1).toMinecraft().`is`(IndustryContent.RIFTIRON_INGOT_ACTIVATED.get()), "activated")
            helper.assertValueEqual(be.naniteTank.amount, 0, "one nanite used")
        }
    }

    private fun infuserNeedsNanites(helper: GameTestHelper) {
        val be = helper.place<NaniteInfuserBlockEntity>(CENTER, NaniteContent.NANITE_INFUSER.get())
        be.battery.setStorage(4000.0)
        be.inventory.setSlot(0, IItemStack.of(ItemStack(Items.REDSTONE, 4)))
        helper.runAfterDelay(40) {
            helper.assertTrue(be.processing.isEmpty, "does not start without nanites")
            helper.assertValueEqual(be.inventory.get(0).stackSize(), 4, "input untouched")
            helper.succeed()
        }
    }

    private fun autoIO(helper: GameTestHelper) {
        val extractor = helper.place<NaniteExtractorBlockEntity>(BlockPos(3, 1, 4), NaniteContent.NANITE_EXTRACTOR.get())
        val infuser = helper.place<NaniteInfuserBlockEntity>(BlockPos(4, 1, 4), NaniteContent.NANITE_INFUSER.get())
        extractor.naniteTank.fill(NaniteRegistry.dumb(3), true)
        val config = extractor.getModule(NaniteModules.NANITE_STORAGE_CONFIGURABLE, null)!!
        config.cycleRelativeFacingIOForward(Direction.EAST)
        config.cycleRelativeFacingIOForward(Direction.EAST)
        helper.assertValueEqual(config.getIOForAbsoluteFacing(Direction.EAST), EnumAutomaticIO.OUTPUT, "east face outputs")
        helper.succeedWhen {
            helper.assertTrue(infuser.naniteTank.amount >= 1, "nanites pushed into the infuser")
            helper.assertValueEqual(infuser.naniteTank.amount + extractor.naniteTank.amount, 3, "conserved")
        }
    }

    private fun playerTransfer(helper: GameTestHelper) {
        val be = helper.place<NaniteInfuserBlockEntity>(CENTER, NaniteContent.NANITE_INFUSER.get())
        val player = helper.makeMockServerPlayerInLevel()
        player.setData(PlayerNanites.ATTACHMENT.get(), listOf(NaniteRegistry.dumb(3)))
        val menu = NaniteMachineMenu(1, player.inventory, be)
        helper.assertTrue(menu.handleAction(player, NaniteMachineMenu.ACTION_FILL, 0), "fill handled")
        helper.assertValueEqual(be.naniteTank.amount, 1, "one nanite into the machine")
        helper.assertValueEqual(PlayerNanites.tank(player).amount, 2, "two left on the player")
        menu.handleAction(player, NaniteMachineMenu.ACTION_DRAIN, 0)
        helper.assertValueEqual(be.naniteTank.amount, 0, "drained back")
        helper.assertValueEqual(PlayerNanites.tank(player).amount, 3, "player full again")
        helper.succeed()
    }

    private fun configurator(helper: GameTestHelper) {
        val be = helper.place<NaniteExtractorBlockEntity>(CENTER, NaniteContent.NANITE_EXTRACTOR.get())
        helper.assertTrue(ConfiguratorItem.MODULES[ConfiguratorMode.NANITE] === NaniteModules.NANITE_STORAGE_CONFIGURABLE, "nanite mode registered")
        val config = be.getModule(NaniteModules.NANITE_STORAGE_CONFIGURABLE, null)!!
        ConfiguratorItem.cycle(config, Direction.UP, backward = false)
        helper.assertValueEqual(config.getIOForAbsoluteFacing(Direction.UP), EnumAutomaticIO.INPUT, "nanite face IO cycled")
        helper.succeed()
    }

    private fun nanoLash(helper: GameTestHelper) {
        val player = helper.makeMockServerPlayerInLevel()
        val at = helper.absolutePos(CENTER)
        player.snapTo(at.x + 0.5, at.y.toDouble(), at.z + 0.5, 0f, 0f)
        val stack = ItemStack(NaniteContent.NANO_LASH.get())
        player.setItemInHand(InteractionHand.MAIN_HAND, stack)
        stack.use(helper.level, player, InteractionHand.MAIN_HAND)
        helper.assertTrue(player.cooldowns.isOnCooldown(stack), "cooldown started")
        helper.assertValueEqual(stack.count, 1, "the lash is not used up")
        val lashes = helper.level.getEntities(NaniteContent.NANO_LASH_ENTITY.get()) { true }
        helper.assertTrue(lashes.size == 1, "one lash thrown, found ${lashes.size}")
        helper.assertTrue(lashes.single().getOwner() === player, "owned by the thrower")
        helper.succeed()
    }
}
