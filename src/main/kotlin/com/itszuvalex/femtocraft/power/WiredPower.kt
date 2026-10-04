package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.core.LeafConduit
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.core.DistributableBattery
import com.itszuvalex.itszulib.core.DistributingTileNetwork
import com.itszuvalex.itszulib.core.DistributionParticipant
import com.itszuvalex.itszulib.core.DistributionRole
import com.itszuvalex.itszulib.core.IDistributionNode
import net.minecraft.core.Direction
import net.neoforged.fml.LogicalSide

/**
 * Network of connected power conduits: an ItszuLib [DistributingTileNetwork], which each tick distributes power between
 * the wired leaves attached to its conduits (each block once, and each multiblock once). Port of v3's
 * `WiredPowerNetwork`.
 *
 * Difference from v3: a failure while distributing is no longer swallowed (v3 caught every `Throwable`).
 */
class WiredPowerNetwork(id: Int) : DistributingTileNetwork<WiredPowerConduit, WiredPowerNetwork>(id, LogicalSide.SERVER) {
    override fun networkModule(): IModule<WiredPowerConduit> = PowerModules.WIRED_CONDUIT

    override fun create(): WiredPowerNetwork = WiredPowerNetwork(WirelessPowerNetwork.nextId())

    /**
     * The loaded leaves attached to this network's conduits, deduplicated by block (or multiblock structure).
     */
    fun leaves(): Collection<IWiredPowerLeafNode> {
        val seen = LinkedHashMap<Any, IWiredPowerLeafNode>()
        getNodes().forEach { conduit ->
            conduit.attachedLeaves().forEach { (key, leaf) -> seen.putIfAbsent(key, leaf) }
        }
        return seen.values
    }
}

/**
 * A power conduit: joins neighbouring conduits of the same [tier] into a [WiredPowerNetwork] and attaches to wired
 * leaves on its other faces ([LeafConduit]). Port of v3's `ModulePowerConduitCrystal`.
 */
class WiredPowerConduit(val tier: ConduitTier) :
    LeafConduit<WiredPowerConduit, WiredPowerNetwork, IWiredPowerLeafNode>({ WiredPowerNetwork(WirelessPowerNetwork.nextId()) }), IDistributionNode {
    override fun module(): IModule<WiredPowerConduit> = PowerModules.WIRED_CONDUIT

    override fun leafModule(): IModule<IWiredPowerLeafNode> = PowerModules.WIRED_LEAF

    override fun shouldConnect(face: Direction, other: WiredPowerConduit): Boolean = other.tier == tier

    override fun canAttach(leaf: IWiredPowerLeafNode, face: Direction): Boolean = leaf.canConnectWiredPower(face)

    override fun attach(leaf: IWiredPowerLeafNode, face: Direction) {
        leaf.connectWiredPower(face)
    }

    override fun detach(leaf: IWiredPowerLeafNode, face: Direction) {
        leaf.disconnectWiredPower(face)
    }

    override fun distributionParticipants(): Sequence<DistributionParticipant> = attachedLeaves().asSequence().mapNotNull { (key, leaf) ->
        val role = when (leaf.powerType) {
            PowerStorageNodeType.PRODUCER -> DistributionRole.PRODUCER
            PowerStorageNodeType.STORAGE -> DistributionRole.STORAGE
            PowerStorageNodeType.CONSUMER -> DistributionRole.CONSUMER
            PowerStorageNodeType.NONE -> return@mapNotNull null
        }
        DistributionParticipant(key, role, DistributableBattery(leaf.battery, leaf::transferRate))
    }
}
