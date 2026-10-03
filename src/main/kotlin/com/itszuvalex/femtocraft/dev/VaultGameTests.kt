package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.industry.FrameMultiblocks
import com.itszuvalex.femtocraft.logistics.FluidReservoirBlockEntity
import com.itszuvalex.femtocraft.logistics.FluidReservoirMenu
import com.itszuvalex.femtocraft.logistics.FluidReservoirState
import com.itszuvalex.femtocraft.logistics.ItemVaultBlockEntity
import com.itszuvalex.femtocraft.logistics.ItemVaultMenu
import com.itszuvalex.femtocraft.logistics.ItemVaultState
import com.itszuvalex.femtocraft.logistics.NaniteVaultBlockEntity
import com.itszuvalex.femtocraft.logistics.NaniteVaultState
import com.itszuvalex.femtocraft.nanite.NaniteModules
import com.itszuvalex.femtocraft.nanite.NaniteRegistry
import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.NaniteStrainVersion
import com.itszuvalex.itszulib.api.Modules
import com.itszuvalex.itszulib.api.adapters.IFluidStack
import com.itszuvalex.itszulib.api.adapters.IItemStack
import com.itszuvalex.itszulib.menu.MenuActionPayload
import com.itszuvalex.itszulib.menu.StorageTerminal
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.phys.Vec3
import net.neoforged.neoforge.capabilities.Capabilities
import net.neoforged.neoforge.fluids.FluidStack
import net.neoforged.neoforge.transfer.ResourceHandlerUtil
import net.neoforged.neoforge.transfer.item.ItemResource

/**
 * Game tests for the frame-built storage multiblocks: the item vault (indexed storage and its terminal), the fluid
 * reservoir and the nanite vault.
 */
object VaultGameTests {
    /** The vaults' home corner; they reach 3 blocks east, up and south. */
    private val AT = BlockPos(3, 1, 3)

    fun register() {
        DevGameTests.test("item_vault_stores_through_outer_faces_and_terminal", body = ::itemVault)
        DevGameTests.test("item_vault_drops_its_items_when_broken", body = ::itemVaultDrops)
        DevGameTests.test("fluid_reservoir_fills_tanks_by_fluid", body = ::fluidReservoir)
        DevGameTests.test("nanite_vault_holds_many_strains", body = ::naniteVault)
        DevGameTests.test("fluid_reservoir_syncs_tanks_for_rendering", body = ::reservoirSync)
        DevGameTests.test("fluid_reservoir_locked_tanks_keep_their_fluid", body = ::reservoirLocks)
        DevGameTests.test("fluid_reservoir_linked_tanks_act_as_one", body = ::reservoirLinks)
    }

    private fun itemVault(helper: GameTestHelper) {
        helper.assertTrue(FrameMultiblocks.ITEM_VAULT.formAt(helper.level, helper.absolutePos(AT)), "vault formed")
        val home = helper.getBlockEntity(AT, ItemVaultBlockEntity::class.java)
        val corner = helper.getBlockEntity(AT.offset(2, 2, 2), ItemVaultBlockEntity::class.java)
        helper.assertValueEqual(home.storage.size(), ItemVaultState.SLOTS, "shared slots")

        // Outer faces reach the vault; faces between vault blocks reach nothing.
        val outer = corner.getModule(Modules.ITEM_STORAGE, Direction.UP)!!
        helper.assertValueEqual(outer.size(), ItemVaultState.SLOTS, "outer face")
        helper.assertValueEqual(corner.getModule(Modules.ITEM_STORAGE, Direction.DOWN)?.size() ?: 0, 0, "inner face")
        val handler = helper.level.getCapability(Capabilities.Item.BLOCK, helper.absolutePos(AT.offset(2, 2, 2)), Direction.UP)!!
        helper.assertValueEqual(handler.size(), ItemVaultState.SLOTS, "NeoForge handler on an outer face")
        helper.assertValueEqual(ResourceHandlerUtil.insertStacking(handler, ItemResource.of(Items.IRON_INGOT), 320, null), 320, "iron inserted")
        helper.assertValueEqual(ResourceHandlerUtil.insertStacking(handler, ItemResource.of(Items.DIAMOND), 10, null), 10, "diamonds inserted")

        // The index answers from the vault's state, seen from any block.
        val index = home.index()!!
        val iron = BuiltInRegistries.ITEM.getKey(Items.IRON_INGOT)
        helper.assertValueEqual(index.count(iron), 320L, "iron counted through the index")
        helper.assertValueEqual(home.state()!!.storage.slotsOf(iron).size, 5, "iron in five slots")

        // The terminal lists two kinds and takes a stack out.
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        player.setPos(Vec3.atCenterOf(helper.absolutePos(AT.west())))
        val menu = ItemVaultMenu(3, player.inventory, home)
        menu.collectSyncPayload(helper.level.registryAccess(), all = false)
        helper.assertValueEqual(menu.vault.stacks.size, 2, "terminal entries")
        val entry = menu.vault.stacks.indexOfFirst { it.stack.`is`(Items.DIAMOND) }
        helper.assertTrue(MenuActionPayload.dispatch(menu, MenuActionPayload(3, StorageTerminal.ACTION_EXTRACT, StorageTerminal.extractData(entry, false, false)), player), "extract")
        helper.assertValueEqual(menu.carried.count, 10, "diamonds carried")
        helper.assertValueEqual(index.count(BuiltInRegistries.ITEM.getKey(Items.DIAMOND)), 0L, "diamonds gone from the vault")
        helper.succeed()
    }

    private fun itemVaultDrops(helper: GameTestHelper) {
        helper.assertTrue(FrameMultiblocks.ITEM_VAULT.formAt(helper.level, helper.absolutePos(AT)), "vault formed")
        val home = helper.getBlockEntity(AT, ItemVaultBlockEntity::class.java)
        home.storage.insert(0, IItemStack.of(ItemStack(Items.EMERALD, 7)))
        helper.destroyBlock(AT.offset(1, 1, 1))
        helper.assertBlockNotPresent(com.itszuvalex.femtocraft.logistics.LogisticsContent.ITEM_VAULT.get(), AT)
        helper.assertItemEntityPresent(Items.EMERALD, AT, 2.0)
        helper.succeed()
    }

    private fun fluidReservoir(helper: GameTestHelper) {
        helper.assertTrue(FrameMultiblocks.FLUID_RESERVOIR.formAt(helper.level, helper.absolutePos(AT)), "reservoir formed")
        val home = helper.getBlockEntity(AT, FluidReservoirBlockEntity::class.java)
        val side = helper.getBlockEntity(AT.offset(0, 1, 1), FluidReservoirBlockEntity::class.java).getModule(Modules.FLUID_STORAGE, Direction.WEST)!!
        val cap = FluidReservoirState.CAPACITY
        helper.assertValueEqual(side.fill(IFluidStack.of(FluidStack(Fluids.WATER, cap + 1000)), true), cap + 1000, "water filled")
        helper.assertValueEqual(side.fill(IFluidStack.of(FluidStack(Fluids.LAVA, 500)), true), 500, "lava filled")
        val tanks = home.state()!!.tanks
        val water = BuiltInRegistries.FLUID.getKey(Fluids.WATER)
        helper.assertValueEqual(tanks.amount(water), (cap + 1000).toLong(), "water across tanks")
        helper.assertValueEqual(tanks.tanksOf(water).size, 2, "water in two tanks")
        helper.assertValueEqual(tanks.tanksOf(BuiltInRegistries.FLUID.getKey(Fluids.LAVA)).size, 1, "lava apart")
        helper.assertValueEqual(side.fill(IFluidStack.of(FluidStack(Fluids.WATER, 500)), true), 500, "water joins its tank")
        helper.assertValueEqual(tanks.tanksOf(water).size, 2, "no third water tank")

        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        val registries = helper.level.registryAccess()
        val server = FluidReservoirMenu(4, player.inventory, home)
        val client = FluidReservoirMenu(4, player.inventory, null)
        client.applySyncPayload(server.collectSyncPayload(registries, all = false)!!, registries)
        helper.assertValueEqual((0 until FluidReservoirState.TANKS).sumOf { client.view.cell(it).amount() }, cap + 2000, "synced amounts")
        helper.succeed()
    }

    private fun naniteVault(helper: GameTestHelper) {
        helper.assertTrue(FrameMultiblocks.NANITE_VAULT.formAt(helper.level, helper.absolutePos(AT)), "nanite vault formed")
        val home = helper.getBlockEntity(AT, NaniteVaultBlockEntity::class.java)
        val face = home.getModule(NaniteModules.NANITE_TANK, Direction.DOWN)!!
        helper.assertValueEqual(face.capacity, NaniteVaultState.VOLUME, "outer face")
        helper.assertValueEqual(home.getModule(NaniteModules.NANITE_TANK, Direction.UP)?.capacity ?: 0, 0, "inner face")
        face.fill(NaniteRegistry.dumb(100), true)
        face.fill(NaniteStack("Dumb", "Dumb", NaniteStrainVersion(1, 0), 50), true)
        face.fill(NaniteRegistry.archive(25), true)
        helper.assertValueEqual(home.state()!!.tank.contents().size, 3, "three strains")
        helper.assertValueEqual(home.tank.amount, 175, "amount")
        helper.succeed()
    }

    /**
     * The home block sends its tanks to clients (the renderer draws them through the windows), at once for a new fluid
     * or a large change, and small changes after a while.
     */
    private fun reservoirSync(helper: GameTestHelper) {
        helper.assertTrue(FrameMultiblocks.FLUID_RESERVOIR.formAt(helper.level, helper.absolutePos(AT)), "reservoir formed")
        val home = helper.getBlockEntity(AT, FluidReservoirBlockEntity::class.java)
        home.tanks.fill(IFluidStack.of(FluidStack(Fluids.WATER, 70_000)), true)
        home.tanks.fill(IFluidStack.of(FluidStack(Fluids.LAVA, 2_000)), true)
        val registries = helper.level.registryAccess()
        val client = FluidReservoirBlockEntity(home.blockPos, home.blockState)
        client.handleUpdateTag(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registries, home.getUpdateTag(registries)))
        val water = (0 until client.clientTanks.size()).map { client.clientTanks.get(it) }.filter { it.toMinecraft().`is`(Fluids.WATER) }
        helper.assertValueEqual(water.sumOf { it.amount() }, 70_000, "client sees the water")
        helper.assertValueEqual((0 until client.clientTanks.size()).count { client.clientTanks.get(it).toMinecraft().`is`(Fluids.LAVA) }, 1, "and the lava tank")

        fun stacks(vararg amounts: Int) = amounts.map { if (it == 0) IFluidStack.Empty else IFluidStack.of(FluidStack(Fluids.WATER, it)) }
        val step = FluidReservoirState.SYNC_STEP
        helper.assertFalse(FluidReservoirState.needsSync(stacks(1000, 0), stacks(1000, 0), 100), "unchanged")
        helper.assertTrue(FluidReservoirState.needsSync(stacks(1000, 0), stacks(1000, 5), 0), "a new fluid at once")
        helper.assertTrue(FluidReservoirState.needsSync(stacks(1000, 0), stacks(1000 + step, 0), 0), "a large change at once")
        helper.assertFalse(FluidReservoirState.needsSync(stacks(1000, 0), stacks(1010, 0), 5), "a small change waits")
        helper.assertTrue(FluidReservoirState.needsSync(stacks(1000, 0), stacks(1010, 0), FluidReservoirState.SYNC_TICKS), "then syncs")
        helper.succeed()
    }

    private fun reservoir(helper: GameTestHelper): Pair<FluidReservoirState, com.itszuvalex.itszulib.api.storage.IFluidStorage> {
        helper.assertTrue(FrameMultiblocks.FLUID_RESERVOIR.formAt(helper.level, helper.absolutePos(AT)), "reservoir formed")
        val home = helper.getBlockEntity(AT, FluidReservoirBlockEntity::class.java)
        val side = helper.getBlockEntity(AT.offset(0, 1, 1), FluidReservoirBlockEntity::class.java).getModule(Modules.FLUID_STORAGE, Direction.WEST)!!
        return home.state()!! to side
    }

    private fun water(amount: Int) = IFluidStack.of(FluidStack(Fluids.WATER, amount))
    private fun lava(amount: Int) = IFluidStack.of(FluidStack(Fluids.LAVA, amount))
    private val WATER_ID get() = BuiltInRegistries.FLUID.getKey(Fluids.WATER)
    private val LAVA_ID get() = BuiltInRegistries.FLUID.getKey(Fluids.LAVA)

    /**
     * A locked tank takes only its fluid, even while empty, and is filled before free tanks; locking an empty tank
     * needs a fluid to lock it to; the menu locks to the carried bucket's fluid.
     */
    private fun reservoirLocks(helper: GameTestHelper) {
        val (state, side) = reservoir(helper)
        val cells = state.cells
        val cap = FluidReservoirState.CAPACITY
        helper.assertFalse(state.toggleLock(3, null), "an empty tank needs a fluid to lock to")
        helper.assertTrue(state.toggleLock(3, WATER_ID), "locked to water")
        side.fill(water(1000), true)
        helper.assertValueEqual(cells.cell(3).amount(), 1000, "water goes to its locked tank first")
        helper.assertValueEqual(cells.cell(0).amount(), 0, "not to a free tank")

        // Through the menu: lock tank 2 to lava from a carried lava bucket.
        val player = helper.makeMockPlayer(GameType.SURVIVAL)
        player.setPos(Vec3.atCenterOf(helper.absolutePos(AT.west())))
        val home = helper.getBlockEntity(AT, FluidReservoirBlockEntity::class.java)
        val menu = FluidReservoirMenu(5, player.inventory, home)
        menu.setCarried(ItemStack(Items.LAVA_BUCKET))
        helper.assertTrue(MenuActionPayload.dispatch(menu, MenuActionPayload(5, FluidReservoirMenu.ACTION_LOCK, 2), player), "lock action")
        helper.assertTrue(cells.lockOf(cells.tankOfCell(2)) == LAVA_ID, "locked to the bucket's lava")
        val registries = helper.level.registryAccess()
        val client = FluidReservoirMenu(5, player.inventory, null)
        client.applySyncPayload(menu.collectSyncPayload(registries, all = true)!!, registries)
        helper.assertTrue(client.view.lockOf(0) == null, "the client sees a free tank as free")
        helper.assertTrue(client.view.lockOf(2) == LAVA_ID, "and the lava lock")

        helper.assertValueEqual(side.fill(water(200_000), true), 2 * cap - 1000 + cap, "water fills only the tanks that are not lava's")
        helper.assertValueEqual(cells.cell(2).amount(), 0, "the lava tank stayed empty")
        helper.assertValueEqual(side.fill(lava(500), true), 500, "lava goes to its tank")
        helper.assertTrue(cells.cell(2).toMinecraft().`is`(Fluids.LAVA), "lava in its tank")

        // Unlocked again, it keeps its lava but would take other fluids once empty.
        helper.assertTrue(MenuActionPayload.dispatch(menu, MenuActionPayload(5, FluidReservoirMenu.ACTION_LOCK, 2), player), "unlock")
        helper.assertTrue(cells.lockOf(cells.tankOfCell(2)) == null, "unlocked")
        helper.succeed()
    }

    /**
     * Linked cells are one tank of their combined capacity until unlinked; tanks with different fluids or locks cannot
     * link; a link merges locks; links and locks are saved.
     */
    private fun reservoirLinks(helper: GameTestHelper) {
        val (state, side) = reservoir(helper)
        val cells = state.cells
        val cap = FluidReservoirState.CAPACITY
        side.fill(water(cap + 10_000), true)
        helper.assertTrue(state.toggleLink(0), "cells 0 and 1 linked")
        helper.assertValueEqual(side.size(), 3, "three tanks")
        helper.assertValueEqual(state.tanks.capacity(0), 2 * cap, "one tank of both cells")
        helper.assertValueEqual(state.tanks.get(0).amount(), cap + 10_000, "holding both cells' water")
        side.fill(lava(100), true)
        helper.assertTrue(cells.cell(2).toMinecraft().`is`(Fluids.LAVA), "lava in the next free cell")
        helper.assertFalse(state.toggleLink(1), "water and lava cannot link")

        helper.assertTrue(state.toggleLock(3, WATER_ID), "cell 3 locked to water")
        helper.assertFalse(state.toggleLink(2), "lava and a water lock cannot link")
        side.drain(lava(100), true)
        helper.assertTrue(state.toggleLink(2), "an empty cell links with a water lock")
        helper.assertTrue(cells.lockOf(cells.tankOfCell(2)) == WATER_ID, "the linked tank keeps the lock")

        // Saved and loaded: the same tanks, links and locks.
        val registries = helper.level.registryAccess()
        val out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING, registries)
        state.serialize(out)
        val copy = FluidReservoirState {}
        copy.deserialize(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, registries, out.buildResult()))
        helper.assertValueEqual(copy.cells.linkMask(), cells.linkMask(), "links saved")
        helper.assertTrue(copy.cells.lockOf(copy.cells.tankOfCell(3)) == WATER_ID, "lock saved")
        helper.assertValueEqual(copy.tanks.get(0).amount(), cap + 10_000, "contents saved")

        helper.assertTrue(state.toggleLink(0), "unlinked")
        helper.assertValueEqual(cells.cell(0).amount(), cap, "first cell keeps its share")
        helper.assertValueEqual(cells.cell(1).amount(), 10_000, "second cell keeps the rest")
        helper.assertValueEqual(state.tanks.tanksOf(WATER_ID).size, 2, "the index follows")
        helper.succeed()
    }
}
