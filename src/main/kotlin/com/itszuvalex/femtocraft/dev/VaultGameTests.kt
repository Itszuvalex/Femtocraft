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
        helper.assertValueEqual((0 until FluidReservoirState.TANKS).sumOf { client.tanks.get(it).amount() }, cap + 2000, "synced amounts")
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
}
