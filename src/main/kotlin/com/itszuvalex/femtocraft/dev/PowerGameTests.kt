package com.itszuvalex.femtocraft.dev

import com.itszuvalex.femtocraft.dev.DevGameTests.place
import com.itszuvalex.femtocraft.power.CrystalMountBlock
import com.itszuvalex.femtocraft.power.CrystalChargingArrayBlockEntity
import com.itszuvalex.femtocraft.power.CrystalHeatExchangerBlockEntity
import com.itszuvalex.femtocraft.power.CrystalMountBlockEntity
import com.itszuvalex.femtocraft.power.CrystalStorageArrayBlockEntity
import com.itszuvalex.femtocraft.power.PowerConduitBlockEntity
import com.itszuvalex.femtocraft.power.PowerContent
import com.itszuvalex.femtocraft.power.PowerCrystalData
import com.itszuvalex.femtocraft.power.PowerCrystals
import com.itszuvalex.femtocraft.power.PowerModules
import com.itszuvalex.itszulib.api.adapters.IItemStack
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.gametest.framework.GameTestHelper
import net.minecraft.nbt.NbtOps
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.entity.BlockEntity

/**
 * Game tests for the power area: crystals, wireless networks and leaves, crystal machines, wired conduits.
 */
object PowerGameTests {
    fun crystal(storage: Double = 0.0, max: Double = 1000.0, passive: Float = 0f, transfer: Double = 100.0, type: String = PowerCrystals.TYPE_SMALL): ItemStack {
        val stack = ItemStack(PowerContent.POWER_CRYSTAL.get())
        PowerCrystals.set(stack, PowerCrystalData("Test", type, PowerCrystalItemColor, passive, transfer, 0.0, storage, max))
        return stack
    }

    private const val PowerCrystalItemColor = 0x00FFFF

    private fun mount(helper: GameTestHelper, pos: BlockPos, crystal: ItemStack = crystal()): CrystalMountBlockEntity {
        val be = helper.place<CrystalMountBlockEntity>(pos, PowerContent.CRYSTAL_MOUNT.get())
        be.storage.setSlot(0, IItemStack.of(crystal))
        return be
    }

    /**
     * The mount's model draws its top half under a solid block and its bottom half over one (block state, as v3's
     * renderer chose).
     */
    private fun mountHalves(helper: GameTestHelper) {
        val at = BlockPos(2, 1, 2)
        helper.setBlock(at, PowerContent.CRYSTAL_MOUNT.get())
        helper.setBlock(at.below(), net.minecraft.world.level.block.Blocks.STONE)
        helper.setBlock(at.above(), net.minecraft.world.level.block.Blocks.STONE)
        helper.assertTrue(helper.getBlockState(at).getValue(CrystalMountBlock.TOP), "top half under stone")
        helper.assertTrue(helper.getBlockState(at).getValue(CrystalMountBlock.BOTTOM), "bottom half on the floor")
        helper.setBlock(at.above(), net.minecraft.world.level.block.Blocks.AIR)
        helper.assertTrue(!helper.getBlockState(at).getValue(CrystalMountBlock.TOP), "no top half under air")
        helper.setBlock(at.below(), net.minecraft.world.level.block.Blocks.AIR)
        helper.assertTrue(!helper.getBlockState(at).getValue(CrystalMountBlock.BOTTOM), "no bottom half over air")
        helper.succeed()
    }

    fun register() {
        DevGameTests.test("atmospheric_pole_charges_its_base", 80, ::atmosphericCharges)
        DevGameTests.test("atmospheric_addons_need_support_and_air", 20, ::atmosphericSupport)
        DevGameTests.test("atmospheric_lightning_strike_is_harmless_power", 20, ::atmosphericStrike)
        DevGameTests.test("cryo_coils_charge_the_base_from_ice", 80, ::cryoCharges)
        DevGameTests.test("cryo_active_handler_freezes_water_lava_and_air", body = ::cryoActive)
        DevGameTests.test("cryo_coil_without_base_does_nothing", 60, ::cryoNoBase)
        DevGameTests.test("power_crystal_battery_and_trickle", body = ::crystalBatteryAndTrickle)
        DevGameTests.test("crystal_mount_model_follows_solid_neighbours", body = ::mountHalves)
        DevGameTests.test("power_crystal_codec_round_trip", body = ::crystalCodec)
        DevGameTests.test("mounts_form_wireless_network", body = ::mountsFormNetwork)
        DevGameTests.test("mount_bridging_two_networks_merges_them", body = ::mountMergesNetworks)
        DevGameTests.test("leaf_attaches_to_nearest_mount", body = ::leafAttachesToNearest)
        DevGameTests.test("leaf_reparents_when_mount_broken", body = ::leafReparents)
        DevGameTests.test("wireless_network_distributes_power", body = ::wirelessDistribution)
        DevGameTests.test("charging_array_full_battery_keeps_crystal_power", body = ::chargingArrayFull)
        DevGameTests.test("charging_array_adds_passive_generation", body = ::chargingArrayPassive)
        DevGameTests.test("storage_array_drains_crystals", body = ::storageArrayDrains)
        DevGameTests.test("heat_exchanger_burns_fuel", body = ::heatExchangerBurns)
        DevGameTests.test("heat_exchanger_keeps_lava_bucket", body = ::heatExchangerKeepsBucket)
        DevGameTests.test("conduits_form_network_and_attach_leaves", body = ::conduitsAttachLeaves)
        DevGameTests.test("wireless_leaf_parent_saves", body = ::leafParentSaves)
    }

    private fun crystalBatteryAndTrickle(helper: GameTestHelper) {
        val stack = crystal(storage = 10.0, max = 100.0, passive = 0.5f)
        val battery = PowerCrystals.battery(stack)!!
        helper.assertValueEqual(battery.fill(200.0), 90.0, "fill clamps to room")
        helper.assertValueEqual(battery.drain(30.0), 30.0, "drain")
        helper.assertValueEqual(PowerCrystals.data(stack)!!.storage, 70.0, "storage written to the stack")
        // Trickle: 0.5/tick averages one unit every two ticks.
        repeat(4) { PowerCrystals.onTick(stack) }
        helper.assertValueEqual(PowerCrystals.data(stack)!!.storage, 72.0, "trickle charge after 4 ticks")
        helper.assertFalse(PowerCrystals.isCrystal(ItemStack(Items.DIAMOND)), "a diamond is not a crystal")
        helper.succeed()
    }

    private fun crystalCodec(helper: GameTestHelper) {
        val stack = PowerCrystals.initialize(ItemStack(PowerContent.POWER_CRYSTAL.get()), "Power Crystal", PowerCrystals.TYPE_LARGE, 0x112233, 4000.0, 0.75f, 250.0)
        val ops = helper.level.registryAccess().createSerializationContext(NbtOps.INSTANCE)
        val tag = ItemStack.CODEC.encodeStart(ops, stack).getOrThrow()
        val back = ItemStack.CODEC.parse(ops, tag).getOrThrow()
        helper.assertTrue(PowerCrystals.data(back) == PowerCrystals.data(stack), "crystal data survives a save")
        helper.assertValueEqual(PowerCrystals.data(back)!!.storage, 2000.0, "initialize starts half charged (v3)")
        helper.succeed()
    }

    private fun mountsFormNetwork(helper: GameTestHelper) {
        val a = mount(helper, BlockPos(1, 1, 1))
        val b = mount(helper, BlockPos(5, 1, 1))
        helper.succeedWhen {
            val na = a.node.getNetwork()
            helper.assertTrue(na != null, "first mount has a network")
            helper.assertTrue(na === b.node.getNetwork(), "mounts within range share a network")
            helper.assertValueEqual(na!!.size(), 2, "network size")
            helper.assertTrue(a.node.renderLocations.contains(b.node.getLoc()), "spanning tree gives a beam to the other mount")
        }
    }

    /**
     * v3 added a node in range of two networks to each in turn; adding to the second removed it from the first, so the
     * two networks never merged.
     */
    private fun mountMergesNetworks(helper: GameTestHelper) {
        val a = mount(helper, BlockPos(0, 1, 0))
        val c = mount(helper, BlockPos(8, 1, 8))
        helper.runAfterDelay(2) {
            helper.assertTrue(a.node.getNetwork() != null && a.node.getNetwork() !== c.node.getNetwork(), "out of range: separate networks")
            val b = mount(helper, BlockPos(4, 1, 4))
            helper.succeedWhen {
                val n = b.node.getNetwork()
                helper.assertTrue(n != null && n === a.node.getNetwork() && n === c.node.getNetwork(), "bridge merges both networks")
                helper.assertValueEqual(n!!.size(), 3, "merged size")
            }
        }
    }

    private fun leafAttachesToNearest(helper: GameTestHelper) {
        val near = mount(helper, BlockPos(2, 1, 2))
        mount(helper, BlockPos(7, 1, 2))
        val array = helper.place<CrystalChargingArrayBlockEntity>(BlockPos(3, 1, 2), PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        helper.succeedWhen {
            val leaf = array.getModule(PowerModules.WIRELESS_LEAF, null)!!
            helper.assertTrue(leaf.parent == near.node.getLoc(), "parent is the nearest mount")
            helper.assertTrue(leaf.storageLoc in near.node.leafLocs(), "mount lists the leaf")
        }
    }

    private fun leafReparents(helper: GameTestHelper) {
        mount(helper, BlockPos(2, 1, 2))
        val other = mount(helper, BlockPos(6, 1, 2))
        val array = helper.place<CrystalChargingArrayBlockEntity>(BlockPos(3, 1, 2), PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        val leaf = array.getModule(PowerModules.WIRELESS_LEAF, null)!!
        helper.runAfterDelay(2) {
            helper.assertTrue(leaf.parent != null, "attached")
            helper.setBlock(BlockPos(2, 1, 2), Blocks.AIR)
            helper.succeedWhen {
                helper.assertTrue(leaf.parent == other.node.getLoc(), "re-parented to the remaining mount")
                helper.assertTrue(leaf.storageLoc in other.node.leafLocs(), "new parent lists the leaf")
            }
        }
    }

    private fun wirelessDistribution(helper: GameTestHelper) {
        val m = mount(helper, BlockPos(2, 1, 2), crystal(storage = 0.0, max = 5000.0, transfer = 1000.0))
        val array = helper.place<CrystalChargingArrayBlockEntity>(BlockPos(4, 1, 2), PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        array.battery.setStorage(1000.0)
        helper.succeedWhen {
            val stored = m.battery.storage()
            helper.assertTrue(stored >= 100.0, "mount crystal charged from the producer (has $stored)")
            helper.assertValueEqual(stored + array.battery.storage(), 1000.0, "power is conserved")
            helper.assertTrue(m.node.getNetwork()!!.statistics.producerCount == 1, "statistics count the producer")
        }
    }

    /**
     * v3 drained the crystal's full transfer rate and clamped the battery, so a full array destroyed crystal power.
     */
    private fun chargingArrayFull(helper: GameTestHelper) {
        val array = helper.place<CrystalChargingArrayBlockEntity>(BlockPos(4, 1, 4), PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        array.battery.setStorage(array.battery.maxStorage())
        array.storage.setSlot(0, IItemStack.of(crystal(storage = 500.0)))
        helper.runAfterDelay(5) {
            helper.assertValueEqual(PowerCrystals.data(array.storage.get(0).toMinecraft())!!.storage, 500.0, "crystal keeps its power")
            helper.succeed()
        }
    }

    private fun chargingArrayPassive(helper: GameTestHelper) {
        val array = helper.place<CrystalChargingArrayBlockEntity>(BlockPos(4, 1, 4), PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        array.storage.setSlot(0, IItemStack.of(crystal(storage = 0.0, passive = 1f)))
        helper.runAfterDelay(10) {
            helper.assertTrue(array.battery.storage() >= 15.0, "two power per tick per unit of passive generation (has ${array.battery.storage()})")
            helper.assertValueEqual(array.powerPerTick(), 2.0, "power per tick")
            helper.succeed()
        }
    }

    private fun storageArrayDrains(helper: GameTestHelper) {
        val array = helper.place<CrystalStorageArrayBlockEntity>(BlockPos(4, 1, 4), PowerContent.CRYSTAL_STORAGE_ARRAY.get())
        array.storage.setSlot(0, IItemStack.of(crystal(storage = 300.0, max = 1000.0, transfer = 100.0)))
        helper.assertValueEqual(array.battery.maxStorage(), 2000.0, "capacity is twice the crystals'")
        helper.succeedWhen {
            helper.assertValueEqual(array.battery.storage(), 300.0, "crystal drained into the array")
            helper.assertValueEqual(PowerCrystals.data(array.storage.get(0).toMinecraft())!!.storage, 0.0, "crystal empty")
        }
    }

    private fun heatExchangerBurns(helper: GameTestHelper) {
        val be = helper.place<CrystalHeatExchangerBlockEntity>(BlockPos(4, 1, 4), PowerContent.CRYSTAL_HEAT_EXCHANGER.get())
        be.storage.setSlot(CrystalHeatExchangerBlockEntity.FUEL_INDEX, IItemStack.of(ItemStack(Items.COAL, 2)))
        be.storage.setSlot(CrystalHeatExchangerBlockEntity.CRYSTAL_INDEX, IItemStack.of(crystal(passive = 1f)))
        helper.runAfterDelay(5) {
            helper.assertValueEqual(be.burnMax, 800, "coal burns for half its vanilla time")
            helper.assertValueEqual(be.storage.get(CrystalHeatExchangerBlockEntity.FUEL_INDEX).stackSize(), 1, "one coal used")
            helper.assertTrue(be.battery.storage() >= 30.0, "ten power per tick per unit of passive generation (has ${be.battery.storage()})")
            helper.succeed()
        }
    }

    /**
     * v3 split one fuel item and lost its container.
     */
    private fun heatExchangerKeepsBucket(helper: GameTestHelper) {
        val be = helper.place<CrystalHeatExchangerBlockEntity>(BlockPos(4, 1, 4), PowerContent.CRYSTAL_HEAT_EXCHANGER.get())
        be.storage.setSlot(CrystalHeatExchangerBlockEntity.FUEL_INDEX, IItemStack.of(ItemStack(Items.LAVA_BUCKET)))
        be.storage.setSlot(CrystalHeatExchangerBlockEntity.CRYSTAL_INDEX, IItemStack.of(crystal()))
        helper.succeedWhen {
            helper.assertTrue(be.burnTime > 0, "burning")
            helper.assertTrue(be.storage.get(CrystalHeatExchangerBlockEntity.FUEL_INDEX).toMinecraft().`is`(Items.BUCKET), "bucket left in the fuel slot")
        }
    }

    private fun conduitsAttachLeaves(helper: GameTestHelper) {
        val array = helper.place<CrystalChargingArrayBlockEntity>(BlockPos(2, 1, 2), PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        val c1 = helper.place<PowerConduitBlockEntity>(BlockPos(3, 1, 2), PowerContent.POWER_CONDUIT.get())
        val c2 = helper.place<PowerConduitBlockEntity>(BlockPos(4, 1, 2), PowerContent.POWER_CONDUIT.get())
        helper.succeedWhen {
            helper.assertTrue(c1.conduit.getNetwork() != null && c1.conduit.getNetwork() === c2.conduit.getNetwork(), "conduits share a network")
            helper.assertTrue(c1.conduit.leafFaces[Direction.WEST], "conduit attached to the array")
            val leaf = array.getModule(PowerModules.WIRED_LEAF, null)!!
            helper.assertTrue(leaf.isConnectedWiredPower(Direction.EAST), "array knows the conduit side")
            helper.assertValueEqual(c1.conduit.getNetwork()!!.leaves().size, 1, "network sees one leaf")
        }
    }

    private fun leafParentSaves(helper: GameTestHelper) {
        val m = mount(helper, BlockPos(2, 1, 2))
        val array = helper.place<CrystalChargingArrayBlockEntity>(BlockPos(4, 1, 2), PowerContent.CRYSTAL_CHARGING_ARRAY.get())
        helper.succeedWhen {
            val registries = helper.level.registryAccess()
            val loaded = BlockEntity.loadStatic(array.blockPos, array.blockState, array.saveWithFullMetadata(registries), registries) as CrystalChargingArrayBlockEntity
            helper.assertTrue(loaded.getModule(PowerModules.WIRELESS_LEAF, null)?.parent == m.node.getLoc(), "parent saved")
            val loadedMount = BlockEntity.loadStatic(m.blockPos, m.blockState, m.saveWithFullMetadata(registries), registries) as CrystalMountBlockEntity
            helper.assertTrue(array.getModule(PowerModules.WIRELESS_LEAF, null)!!.storageLoc in loadedMount.node.leafLocs(), "leaf list saved")
        }
    }

    /**
     * A base on two coils, the lower one surrounded by ice: the coils count, and the base charges at about 1.25 DE a
     * tick per ice block.
     */
    private fun cryoCharges(helper: GameTestHelper) {
        val base = helper.place<com.itszuvalex.femtocraft.power.CryoChargingBaseBlockEntity>(BlockPos(4, 3, 4), PowerContent.CRYO_BASE.get())
        helper.place<com.itszuvalex.femtocraft.power.CryoChargingCoilBlockEntity>(BlockPos(4, 2, 4), PowerContent.CRYO_COIL.get())
        helper.place<com.itszuvalex.femtocraft.power.CryoChargingCoilBlockEntity>(BlockPos(4, 1, 4), PowerContent.CRYO_COIL.get())
        for (face in listOf(net.minecraft.core.Direction.NORTH, net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.WEST)) {
            helper.setBlock(BlockPos(4, 1, 4).relative(face), net.minecraft.world.level.block.Blocks.ICE)
        }
        helper.assertValueEqual(base.coils(), 2, "coils under the base")
        helper.runAfterDelay(40) {
            helper.assertTrue(base.battery.storage() >= 40 * 4 * com.itszuvalex.femtocraft.power.CryogenRegistry.ICE_PER_TICK - 10, "charged ${base.battery.storage()}")
            helper.succeed()
        }
    }

    private fun cryoActive(helper: GameTestHelper) {
        val level = helper.level
        val registry = com.itszuvalex.femtocraft.power.CryogenRegistry
        val water = BlockPos(2, 1, 2)
        val lava = BlockPos(4, 1, 2)
        helper.setBlock(water, net.minecraft.world.level.block.Blocks.WATER)
        helper.setBlock(lava, net.minecraft.world.level.block.Blocks.LAVA)
        helper.setBlock(BlockPos(6, 1, 2), net.minecraft.world.level.block.Blocks.STONE)
        helper.assertValueEqual(registry.activePower(level, helper.absolutePos(water)), registry.WATER_TO_ICE, "water")
        helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.ICE, water)
        helper.assertValueEqual(registry.activePower(level, helper.absolutePos(lava)), registry.LAVA_TO_OBSIDIAN, "lava")
        helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.OBSIDIAN, lava)
        helper.assertValueEqual(registry.activePower(level, helper.absolutePos(BlockPos(6, 2, 2))), registry.AIR_TO_SNOW_LAYER, "air over stone")
        helper.assertBlockPresent(net.minecraft.world.level.block.Blocks.SNOW, BlockPos(6, 2, 2))
        helper.assertValueEqual(registry.activePower(level, helper.absolutePos(BlockPos(6, 4, 2))), 0.0, "air over air")
        helper.succeed()
    }

    /**
     * A coil with no base above it makes and freezes nothing.
     */
    private fun cryoNoBase(helper: GameTestHelper) {
        val coil = helper.place<com.itszuvalex.femtocraft.power.CryoChargingCoilBlockEntity>(BlockPos(4, 1, 4), PowerContent.CRYO_COIL.get())
        helper.setBlock(BlockPos(4, 2, 4), net.minecraft.world.level.block.Blocks.STONE)
        helper.assertTrue(coil.base() == null, "no base")
        helper.succeed()
    }

    private val ATMO_BASE = BlockPos(4, 1, 4)

    private fun pole(helper: GameTestHelper, coils: Int, cap: Boolean): com.itszuvalex.femtocraft.power.AtmosphericChargingBaseBlockEntity {
        val base = helper.place<com.itszuvalex.femtocraft.power.AtmosphericChargingBaseBlockEntity>(ATMO_BASE, PowerContent.ATMOSPHERIC_BASE.get())
        for (i in 1..coils) helper.setBlock(ATMO_BASE.above(i), PowerContent.ATMOSPHERIC_COIL.get())
        if (cap) helper.setBlock(ATMO_BASE.above(coils + 1), PowerContent.ATMOSPHERIC_CAPACITOR.get())
        return base
    }

    /**
     * Three coils and a capacitor (the game test server keeps the weather clear): 0.1 DE/t a coil plus the
     * capacitor's 20%.
     */
    private fun atmosphericCharges(helper: GameTestHelper) {
        val base = pole(helper, 3, true)
        helper.runAfterDelay(50) {
            helper.assertValueEqual(base.coils, 3, "coils")
            helper.assertTrue(base.capped, "capped")
            helper.assertTrue(kotlin.math.abs(base.powerPerTick - 0.36) < 1e-9, "power per tick ${base.powerPerTick}")
            helper.assertTrue(base.battery.storage() in 15.0..19.0, "charged ${base.battery.storage()}")
            helper.succeed()
        }
    }

    /**
     * Addons stand only on the base or a coil (not on a capacitor), with air on all four sides; a block placed beside
     * one breaks it. Two bases may not stand side by side.
     */
    private fun atmosphericSupport(helper: GameTestHelper) {
        val level = helper.level
        val coil = PowerContent.ATMOSPHERIC_COIL.get().defaultBlockState()
        pole(helper, 1, true)
        helper.assertFalse(coil.canSurvive(level, helper.absolutePos(ATMO_BASE.above(3))), "nothing on a capacitor")
        helper.setBlock(BlockPos(1, 1, 1), net.minecraft.world.level.block.Blocks.STONE)
        helper.assertFalse(coil.canSurvive(level, helper.absolutePos(BlockPos(1, 2, 1))), "not on stone")
        helper.assertFalse(PowerContent.ATMOSPHERIC_BASE.get().defaultBlockState().canSurvive(level, helper.absolutePos(ATMO_BASE.east())), "not beside a base")
        helper.setBlock(ATMO_BASE.above().east(), net.minecraft.world.level.block.Blocks.STONE)
        helper.runAfterDelay(3) {
            helper.assertBlockNotPresent(PowerContent.ATMOSPHERIC_COIL.get(), ATMO_BASE.above())
            helper.assertBlockNotPresent(PowerContent.ATMOSPHERIC_CAPACITOR.get(), ATMO_BASE.above(2))
            helper.succeed()
        }
    }

    /**
     * A strike lands on the capacitor's top, stores its power, and sets no fire.
     */
    private fun atmosphericStrike(helper: GameTestHelper) {
        val base = pole(helper, 2, true)
        val top = helper.absolutePos(ATMO_BASE.above(3))
        base.strike(helper.level, top)
        helper.assertValueEqual(base.battery.storage(), com.itszuvalex.femtocraft.power.AtmosphericChargingBaseBlockEntity.STRIKE_POWER, "strike power")
        helper.assertValueEqual(base.strikes, 1, "counted")
        val bolts = helper.level.getEntitiesOfClass(net.minecraft.world.entity.LightningBolt::class.java, net.minecraft.world.phys.AABB(top).inflate(1.0))
        helper.assertValueEqual(bolts.size, 1, "a bolt at the top")
        helper.assertTrue(bolts.single().y >= top.y + 0.8, "on the capacitor's top: ${bolts.single().y}")
        helper.runAfterDelay(10) {
            for (pos in BlockPos.betweenClosed(top.offset(-2, -4, -2), top.offset(2, 2, 2))) {
                helper.assertFalse(helper.level.getBlockState(pos).`is`(net.minecraft.tags.BlockTags.FIRE), "fire at $pos")
            }
            helper.assertBlockPresent(PowerContent.ATMOSPHERIC_CAPACITOR.get(), ATMO_BASE.above(3))
            helper.succeed()
        }
    }
}
