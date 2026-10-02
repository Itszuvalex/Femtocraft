package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.api.adapters.IBattery
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.adapters.Module
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.core.INetworkNode
import net.minecraft.core.Direction
import net.minecraft.resources.Identifier

/**
 * How a power storage takes part in distribution. Port of v3's `PowerStorageNodeType`.
 */
enum class PowerStorageNodeType {
    PRODUCER,
    CONSUMER,
    STORAGE,
    NONE,
    ;

    fun storesPower(): Boolean = this != NONE
}

/**
 * A battery that a wireless power network distributes to or from. Port of v3's `IWirelessPowerStorageNode`.
 */
interface IWirelessPowerStorageNode {
    val battery: IBattery

    val storageType: PowerStorageNodeType

    /**
     * Most power that may move into or out of [battery] per tick.
     */
    fun transferRate(): Double

    val storageLoc: Loc4
}

/**
 * A power storage that attaches to the nearest wireless power node within [connectionRadius] (its parent). Port of v3's
 * `IWirelessPowerLeafNode`.
 */
interface IWirelessPowerLeafNode : IWirelessPowerStorageNode {
    val connectionRadius: Float

    /**
     * The parent node's location, or null if this leaf has none.
     */
    val parent: Loc4?

    /**
     * Sets (or with null, clears) the parent.
     */
    fun setParent(node: IWirelessPowerNetworkNode?)

    fun canSetParent(node: IWirelessPowerNetworkNode): Boolean =
        node.getLoc().distSqr(storageLoc) <= connectionRadius.toDouble() * connectionRadius

    /**
     * The parent was broken: forget it and look for another.
     */
    fun onParentBroken(node: IWirelessPowerNetworkNode)
}

/**
 * A node of a [WirelessPowerNetwork]: connects to other nodes within its radius and parents leaves. Port of v3's
 * `IWirelessPowerNetworkNode`.
 */
interface IWirelessPowerNetworkNode : INetworkNode<IWirelessPowerNetworkNode, WirelessPowerNetwork> {
    val connectionRadius: Float

    /**
     * Locations of this node's leaves, loaded or not.
     */
    fun leafLocs(): Set<Loc4>

    /**
     * This node's loaded leaves. Never loads a chunk.
     */
    fun leafNodes(): Collection<IWirelessPowerLeafNode>

    fun canAddLeafNode(node: IWirelessPowerLeafNode): Boolean =
        node.storageLoc.distSqr(getLoc()) <= connectionRadius.toDouble() * connectionRadius

    fun addLeafNode(node: IWirelessPowerLeafNode)

    fun removeLeafNode(node: IWirelessPowerLeafNode)

    /**
     * The storages this node contributes to distribution: its own (if its block has one) and its loaded leaves'.
     */
    fun storageNodes(): Collection<IWirelessPowerStorageNode>

    /**
     * Whether beams are drawn from this node (client rendering, follow-up work).
     */
    val rendersConnections: Boolean

    /**
     * The nodes this one draws beams to: its edges in the network's minimal spanning tree. Synced to clients.
     */
    var renderLocations: Set<Loc4>

    fun leafTransferRate(): Double

    override fun canConnect(loc: Loc4): Boolean =
        loc != getLoc() && getLoc().distSqr(loc) <= connectionRadius.toDouble() * connectionRadius
}

/**
 * A side of a block that wired power conduits can attach to. Port of v3's `IWiredPowerConnectable`.
 */
interface IWiredPowerConnectable {
    fun isConnectedWiredPower(face: Direction): Boolean

    fun canConnectWiredPower(face: Direction): Boolean

    fun connectWiredPower(face: Direction): Boolean

    fun disconnectWiredPower(face: Direction): Boolean
}

/**
 * A battery reached through wired power conduits. Port of v3's `IWiredPowerLeafNode`.
 */
interface IWiredPowerLeafNode : IWiredPowerConnectable {
    val battery: IBattery

    val powerType: PowerStorageNodeType

    fun transferRate(): Double
}

/**
 * Conduit tiers; conduits only connect to their own tier. Port of v3's `IConduitTier` (only the crystal tier had
 * conduits).
 */
enum class ConduitTier {
    CRYSTAL,
    DENSE,
    NANO,
}

/**
 * Femtocraft's power modules. None has a NeoForge capability (DECISIONS D10): they are reached through ItszuLib's
 * `IModuleProvider` on Femtocraft block entities.
 */
object PowerModules {
    @JvmField
    val POWER_STORAGE: IModule<IBattery> = Module.registerModule(id("power_storage"), null)

    @JvmField
    val WIRELESS_NODE: IModule<IWirelessPowerNetworkNode> = Module.registerModule(id("wireless_power_node"), null)

    @JvmField
    val WIRELESS_LEAF: IModule<IWirelessPowerLeafNode> = Module.registerModule(id("wireless_power_leaf_node"), null)

    @JvmField
    val WIRELESS_STORAGE: IModule<IWirelessPowerStorageNode> = Module.registerModule(id("wireless_power_storage_node"), null)

    @JvmField
    val WIRED_CONDUIT: IModule<WiredPowerConduit> = Module.registerModule(id("wired_power_node"), null)

    @JvmField
    val WIRED_LEAF: IModule<IWiredPowerLeafNode> = Module.registerModule(id("wired_power_leaf_node"), null)

    private fun id(path: String) = Identifier.fromNamespaceAndPath(Femtocraft.ID, path)

    fun init() {}
}
