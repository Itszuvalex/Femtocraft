package com.itszuvalex.femtocraft.computation

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.LeafConduit
import com.itszuvalex.femtocraft.power.WirelessPowerNetwork
import com.itszuvalex.itszulib.api.adapters.IBlockEntity
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.adapters.Module
import com.itszuvalex.itszulib.api.utility.NBTSerializationScope
import com.itszuvalex.itszulib.core.DistributingTileNetwork
import com.itszuvalex.itszulib.core.Distributable
import com.itszuvalex.itszulib.core.DistributionParticipant
import com.itszuvalex.itszulib.core.DistributionRole
import com.itszuvalex.itszulib.core.IDistributionNode
import com.itszuvalex.itszulib.core.frag.BlockEntityFragment
import com.itszuvalex.itszulib.util.FaceBitSet
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.neoforged.fml.LogicalSide

/**
 * Computation (DECISIONS D19): computers turn power into FLOPS, which computation conduits carry to jobs every tick.
 * FLOPS are not stored; a computer only spends power on what a job takes. After v3's `api/computation`, which was
 * never finished.
 */
object ComputationModules {
    @JvmField
    val CONDUIT: IModule<ComputationConduit> = Module.registerModule(id("computation_node"), null)

    @JvmField
    val LEAF: IModule<IComputationLeaf> = Module.registerModule(id("computation_leaf"), null)

    private fun id(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)

    fun init() {}
}

/**
 * What a computation leaf brings to its network this tick: a computer ([DistributionRole.PRODUCER], whose
 * [Distributable.remove] computes and spends power) or a job ([DistributionRole.CONSUMER], whose [Distributable.add]
 * takes FLOPS).
 */
data class ComputationParticipant(val role: DistributionRole, val resource: Distributable)

/**
 * A block that takes part in computation through computation conduits on its faces.
 */
interface IComputationLeaf {
    fun canConnectComputation(face: Direction): Boolean = true

    fun connectComputation(face: Direction) {}

    fun disconnectComputation(face: Direction) {}

    /** Null while it has nothing to give or take. */
    fun computation(): ComputationParticipant?
}

/**
 * A computation leaf on every face: [participant] says what the block brings. The faces a conduit is attached to are
 * saved and synced (key `Con`).
 */
class FragComputationLeaf(private val participant: () -> ComputationParticipant?) : BlockEntityFragment<IComputationLeaf>(), IComputationLeaf {
    @JvmField
    val connections = FaceBitSet()

    override fun computation(): ComputationParticipant? = participant()

    override fun connectComputation(face: Direction) {
        if (!connections[face]) {
            connections.set(face)
            markDirtyAndSync()
        }
    }

    override fun disconnectComputation(face: Direction) {
        if (connections[face]) {
            connections.clear(face)
            markDirtyAndSync()
        }
    }

    override fun name(): String = "ComputationLeaf"
    override fun module(): IModule<IComputationLeaf> = ComputationModules.LEAF
    override fun faceToModuleMapper(be: IBlockEntity): (Direction?) -> IComputationLeaf? = { this }
    override fun handlesScope(scope: NBTSerializationScope): Boolean = scope != NBTSerializationScope.ITEM
    override fun serializeTo(scope: NBTSerializationScope, output: ValueOutput) = output.putInt(CON_KEY, connections.bits)
    override fun deserialize(input: ValueInput, scope: NBTSerializationScope) = connections.load(input.getIntOr(CON_KEY, 0))

    companion object {
        const val CON_KEY = "Con"
    }
}

/**
 * Network of connected computation conduits: an ItszuLib distributing network over the computers and jobs attached to
 * its conduits (each block or multiblock once). Computers with the best FLOPS per power give first
 * ([Distributable.priority]); jobs with the least done take first.
 */
class ComputationNetwork(id: Int) : DistributingTileNetwork<ComputationConduit, ComputationNetwork>(id, LogicalSide.SERVER) {
    override fun networkModule(): IModule<ComputationConduit> = ComputationModules.CONDUIT

    override fun create(): ComputationNetwork = ComputationNetwork(WirelessPowerNetwork.nextId())
}

/**
 * A computation conduit: joins neighbouring computation conduits into a [ComputationNetwork] and attaches to
 * computation leaves on its other faces.
 */
class ComputationConduit : LeafConduit<ComputationConduit, ComputationNetwork, IComputationLeaf>({ ComputationNetwork(WirelessPowerNetwork.nextId()) }),
    IDistributionNode {
    override fun module(): IModule<ComputationConduit> = ComputationModules.CONDUIT

    override fun leafModule(): IModule<IComputationLeaf> = ComputationModules.LEAF

    override fun canAttach(leaf: IComputationLeaf, face: Direction): Boolean = leaf.canConnectComputation(face)

    override fun attach(leaf: IComputationLeaf, face: Direction) = leaf.connectComputation(face)

    override fun detach(leaf: IComputationLeaf, face: Direction) = leaf.disconnectComputation(face)

    override fun distributionParticipants(): Sequence<DistributionParticipant> = attachedLeaves().asSequence().mapNotNull { (key, leaf) ->
        leaf.computation()?.let { DistributionParticipant(key, it.role, it.resource) }
    }
}
