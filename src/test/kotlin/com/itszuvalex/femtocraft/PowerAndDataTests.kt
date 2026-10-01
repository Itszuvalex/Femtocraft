package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.power.IPowerNode
import com.itszuvalex.femtocraft.power.OwnPowerStorage
import com.itszuvalex.femtocraft.power.PowerNodeRules
import com.itszuvalex.femtocraft.power.PowerNodeTypes
import com.itszuvalex.femtocraft.power.FragPowerNode
import com.itszuvalex.femtocraft.power.item.CrystalData
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.Loc4Indirect
import net.minecraft.core.BlockPos
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.Identifier
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * A power node that is only a type and a location, for whitelist checks.
 */
private class FakeNode(private val type: String, private val loc: Loc4) : IPowerNode {
    override fun getType() = type
    override fun getParent(): IPowerNode? = null
    override fun getParentLoc(): Loc4? = null
    override fun canSetParent(parent: IPowerNode) = true
    override fun setParent(parent: IPowerNode?) = true
    override fun parentConnectionRadius() = PowerNodeTypes.DEFAULT_MAX_RADIUS
    override fun getChildren(): Set<IPowerNode> = emptySet()
    override fun getChildrenLocs(): Set<Loc4> = emptySet()
    override fun canAddChild(child: IPowerNode) = true
    override fun addChild(child: IPowerNode) = true
    override fun removeChild(child: IPowerNode) = true
    override fun childrenConnectionRadius() = PowerNodeTypes.DEFAULT_MAX_RADIUS
    override fun getNodeLoc() = loc
    override fun getPowerCurrent() = 0.0
    override fun getPowerMax() = 0.0
    override fun addPower(amount: Double, doFill: Boolean) = 0.0
    override fun setPower(amount: Double) {}
    override fun usePower(amount: Double, doUse: Boolean) = 0.0
    override fun getColor() = 0
}

class PowerNodeRulesTest {
    private val dim = Identifier.parse("test")
    private fun node(type: String, x: Int = 0) = FakeNode(type, Loc4Indirect(dim, BlockPos(x, 0, 0)))
    private val T = PowerNodeTypes

    @Test
    fun Transfer_AcceptsMountAndTransferParents_RejectsDiffusion() {
        val frag = FragPowerNode(PowerNodeRules.TRANSFER)
        Assertions.assertTrue(frag.canSetParent(node(T.CRYSTAL_MOUNT)))
        Assertions.assertTrue(frag.canSetParent(node(T.TRANSFER_NODE)))
        Assertions.assertFalse(frag.canSetParent(node(T.DIFFUSION_NODE)))
    }

    @Test
    fun DiffusionTarget_AcceptsNoChildren() {
        val frag = FragPowerNode(PowerNodeRules.DIFFUSION_TARGET)
        Assertions.assertFalse(frag.canAddChild(node(T.TRANSFER_NODE)))
        Assertions.assertTrue(frag.canSetParent(node(T.DIFFUSION_NODE)))
    }

    @Test
    fun Direct_IsLeaf() {
        val frag = FragPowerNode(PowerNodeRules.DIRECT)
        Assertions.assertNull(frag.getChildren())
        Assertions.assertNull(frag.getChildrenLocs())
        Assertions.assertFalse(frag.canAddChild(node(T.DIRECT_NODE)))
    }

    @Test
    fun Generation_NeverTakesAParent_AndReportsTransferType() {
        val frag = FragPowerNode(PowerNodeRules.GENERATION)
        Assertions.assertFalse(frag.canSetParent(node(T.CRYSTAL_MOUNT)))
        Assertions.assertEquals(T.TRANSFER_NODE, frag.getType())
        Assertions.assertTrue(frag.canAddChild(node(T.DIFFUSION_NODE)))
    }

    @Test
    fun Mount_ChildWhitelist() {
        val frag = FragPowerNode(PowerNodeRules.MOUNT)
        Assertions.assertTrue(frag.canAddChild(node(T.DIFFUSION_TARGET_NODE)))
        Assertions.assertFalse(frag.canAddChild(node(T.DIFFUSION_NODE)))
    }

    @Test
    fun OwnStorage_ClampsFillAndUse() {
        val storage = OwnPowerStorage(100.0)
        Assertions.assertEquals(100.0, storage.add(150.0, true))
        Assertions.assertEquals(40.0, storage.use(40.0, true))
        Assertions.assertEquals(60.0, storage.current())
        Assertions.assertEquals(10.0, storage.add(10.0, false))
        Assertions.assertEquals(60.0, storage.current(), "simulate must not change storage")
    }
}

class CrystalDataTest {
    @Test
    fun Codec_RoundTrip_KeepsAllFields() {
        val data = CrystalData("Test", CrystalData.TYPE_SMALL, 0x11223344, 12.5, 100.0, .25, .5f, 77)
        val tag = CrystalData.CODEC.encodeStart(NbtOps.INSTANCE, data).getOrThrow()
        Assertions.assertEquals(data, CrystalData.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow())
    }

    @Test
    fun Codec_UsesLegacyKeyNames() {
        val tag = CrystalData.CODEC.encodeStart(NbtOps.INSTANCE, CrystalData(storageCurrent = 3.0, transfer = 5)).getOrThrow() as net.minecraft.nbt.CompoundTag
        Assertions.assertTrue(tag.contains("Storage_Current"))
        Assertions.assertTrue(tag.contains("Transfer"))
    }
}
