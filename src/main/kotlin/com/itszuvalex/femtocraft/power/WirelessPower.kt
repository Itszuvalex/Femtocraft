package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.core.DistributableBattery
import com.itszuvalex.itszulib.core.DistributionAlgorithm
import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.adapters.IModule
import com.itszuvalex.itszulib.api.utility.Loc4
import com.itszuvalex.itszulib.api.utility.LocationTracker
import com.itszuvalex.itszulib.core.TileNetwork
import net.neoforged.fml.LogicalSide

/**
 * Wireless power network: nodes (crystal mounts) within each other's radius, plus their leaves. Each tick it
 * distributes power between every loaded storage of the network. Port of v3's `WirelessPowerNetwork`.
 *
 * Differences from v3: statistics count the power actually moved (v3 read each node's `changeForLastTick`, which every
 * implementation left at 0, so the network screen always showed zeros), and render locations are recomputed whenever
 * the network's shape changes, not only on add/remove calls (splits and takeovers skipped them).
 */
class WirelessPowerNetwork(id: Int) : TileNetwork<IWirelessPowerNetworkNode, WirelessPowerNetwork>(id, LogicalSide.SERVER) {
    @JvmField
    val statistics = Statistics()

    private var shape = -1

    override fun networkModule(): IModule<IWirelessPowerNetworkNode> = PowerModules.WIRELESS_NODE

    override fun create(): WirelessPowerNetwork = WirelessPowerNetwork(nextId())

    /**
     * Every loaded storage of the network once, by location.
     */
    fun storageNodes(): Collection<IWirelessPowerStorageNode> {
        val seen = LinkedHashMap<Loc4, IWirelessPowerStorageNode>()
        getNodes().forEach { node -> node.storageNodes().forEach { seen.putIfAbsent(it.storageLoc, it) } }
        return seen.values
    }

    override fun onTickEnd() {
        val nodes = storageNodes()
        val producers = nodes.filter { it.storageType == PowerStorageNodeType.PRODUCER }
        val storage = nodes.filter { it.storageType == PowerStorageNodeType.STORAGE }
        val consumers = nodes.filter { it.storageType == PowerStorageNodeType.CONSUMER }
        val result = DistributionAlgorithm(
            producers.map { DistributableBattery(it.battery, it::transferRate) },
            storage.map { DistributableBattery(it.battery, it::transferRate) },
            consumers.map { DistributableBattery(it.battery, it::transferRate) },
        ).distribute()
        statistics.record(producers, storage, consumers, result)
        updateRenderLocations()
    }

    /**
     * Gives each node that draws beams its edges in the minimal spanning tree, when the network's shape changed.
     */
    fun updateRenderLocations() {
        val connections = getConnections()
        val signature = connections.entries.sumOf { (k, v) -> k.hashCode() * 31 + v.hashCode() } + size()
        if (signature == shape) return
        shape = signature
        val tree = MinimalSpanningTree.calculate(connections)
        getNodes().filter { it.rendersConnections }.forEach { it.renderLocations = tree[it.getLoc()] ?: emptySet() }
    }

    /**
     * Last-tick figures for the network screen. Port of v3's `WirelessPowerNetwork.Statistics`.
     */
    class Statistics {
        private val trend = DoubleArray(TICKS_TO_AVERAGE)
        private var trendCount = 0
        private var trendIndex = 0

        var producerCount = 0
            private set
        var storageCount = 0
            private set
        var consumerCount = 0
            private set

        /** Power taken from producers last tick. */
        var produced = 0.0
            private set

        /** Power given to consumers last tick. */
        var consumed = 0.0
            private set

        /** Net change of dedicated storage last tick (positive: charging). */
        var storageDelta = 0.0
            private set

        var dedicatedStored = 0.0
            private set
        var dedicatedStorage = 0.0
            private set
        var totalStored = 0.0
            private set
        var totalStorage = 0.0
            private set

        /** Average over [TICKS_TO_AVERAGE] ticks of [storageDelta]; positive means the network is filling up. */
        val averageTrend: Double get() = if (trendCount == 0) 0.0 else trend.sum() / trendCount

        fun record(
            producers: Collection<IWirelessPowerStorageNode>,
            storage: Collection<IWirelessPowerStorageNode>,
            consumers: Collection<IWirelessPowerStorageNode>,
            result: DistributionAlgorithm.Result,
        ) {
            producerCount = producers.size
            storageCount = storage.size
            consumerCount = consumers.size
            produced = result.fromProducers
            consumed = result.toConsumers
            storageDelta = result.toStorage - result.fromStorage
            dedicatedStored = storage.sumOf { it.battery.storage() }
            dedicatedStorage = storage.sumOf { it.battery.maxStorage() }
            val all = producers + storage + consumers
            totalStored = all.sumOf { it.battery.storage() }
            totalStorage = all.sumOf { it.battery.maxStorage() }
            trend[trendIndex] = storageDelta
            trendIndex = (trendIndex + 1) % TICKS_TO_AVERAGE
            trendCount = minOf(TICKS_TO_AVERAGE, trendCount + 1)
        }
    }

    companion object {
        const val TICKS_TO_AVERAGE = 20 * 10

        fun nextId(): Int = ItszuLib.NETWORK_MANAGER.get(LogicalSide.SERVER)?.getNextID() ?: 0
    }
}

/**
 * Finds wireless nodes and leaves by location and links them: nodes join the networks of nodes in range, leaves
 * attach to the nearest node. Server side only; cleared when the server stops. Port of v3's `WirelessPowerManager`.
 *
 * Differences from v3:
 * - A new node in range of two networks merges them (v3 added it to each network in turn, and adding a node to a
 *   network removes it from its previous one, so the networks stayed apart).
 * - Nothing is force-loaded: v3 looked up a broken node's leaves and a broken leaf's parent with chunk loading.
 *   Instead, loading reconciles: a leaf whose parent is loaded but no longer has it drops the parent, and a node drops
 *   leaves that are loaded but no longer point at it.
 */
object WirelessPowerManager {
    private val nodeTracker = LocationTracker()
    private val leafTracker = LocationTracker()

    fun addNode(node: IWirelessPowerNetworkNode) {
        val loc = node.getLoc()
        nodeTracker.trackLocation(loc)
        val candidates = inRange(nodeTracker, loc, node.connectionRadius, PowerModules.WIRELESS_NODE)
            .filter { it !== node && it.canConnect(loc) && node.canConnect(it.getLoc()) }
        val network = node.getNetwork() ?: candidates.firstNotNullOfOrNull { it.getNetwork() } ?: WirelessPowerNetwork(WirelessPowerNetwork.nextId()).also { it.register() }
        network.addNode(node)
        // addNode connects to the nodes of that network; join any other network in range too.
        candidates.filter { it.getNetwork() !== node.getNetwork() }.forEach { other -> node.getNetwork()?.addConnectionNodes(node, other) }
        node.getNetwork()?.updateRenderLocations()
        refreshLeavesOf(node)
    }

    fun removeNode(node: IWirelessPowerNetworkNode) {
        nodeTracker.removeLocation(node.getLoc())
        node.getNetwork()?.let {
            it.removeNode(node)
            it.updateRenderLocations()
        }
    }

    /**
     * A node's block was broken: its loaded leaves look for new parents.
     */
    fun onNodeBroken(node: IWirelessPowerNetworkNode) {
        node.leafNodes().forEach { it.onParentBroken(node) }
    }

    /**
     * Attaches parentless leaves in range of [node] to it, nearest first.
     */
    fun refreshLeavesOf(node: IWirelessPowerNetworkNode) {
        inRange(leafTracker, node.getLoc(), node.connectionRadius, PowerModules.WIRELESS_LEAF)
            .filter { it.parent == null && it.storageLoc != node.getLoc() && it.canSetParent(node) && node.canAddLeafNode(it) }
            .sortedBy { it.storageLoc.distSqr(node.getLoc()) }
            .forEach { leaf ->
                node.addLeafNode(leaf)
                leaf.setParent(node)
            }
    }

    fun addLeaf(leaf: IWirelessPowerLeafNode) {
        leafTracker.trackLocation(leaf.storageLoc)
        val parent = leaf.parent
        if (parent != null) {
            val level = (leaf.storageLoc as? com.itszuvalex.itszulib.api.utility.Loc4Level)?.level
            if (level != null && level.isLoaded(parent.pos)) {
                val node = level.getBlockEntity(parent.pos)?.let(com.itszuvalex.itszulib.api.adapters.IBlockEntity::of)
                    ?.getModule(PowerModules.WIRELESS_NODE, null)
                if (node == null || leaf.storageLoc !in node.leafLocs()) leaf.setParent(null)
            }
        }
        refreshLeaf(leaf)
    }

    /**
     * Attaches a parentless leaf to the nearest node in range.
     */
    fun refreshLeaf(leaf: IWirelessPowerLeafNode) {
        if (leaf.parent != null) return
        val node = inRange(nodeTracker, leaf.storageLoc, leaf.connectionRadius, PowerModules.WIRELESS_NODE)
            .filter { it.getLoc() != leaf.storageLoc && it.canAddLeafNode(leaf) && leaf.canSetParent(it) }
            .minByOrNull { it.getLoc().distSqr(leaf.storageLoc) } ?: return
        node.addLeafNode(leaf)
        leaf.setParent(node)
    }

    fun removeLeaf(leaf: IWirelessPowerLeafNode) {
        leafTracker.removeLocation(leaf.storageLoc)
    }

    /**
     * A leaf's block was broken: its parent (if loaded) forgets it.
     */
    fun onLeafBroken(leaf: IWirelessPowerLeafNode) {
        val parent = leaf.parent ?: return
        nodeAt(leaf.storageLoc, parent)?.removeLeafNode(leaf)
    }

    /**
     * The node at [loc] if its chunk is loaded, using [from]'s level.
     */
    fun nodeAt(from: Loc4, loc: Loc4): IWirelessPowerNetworkNode? {
        val level = (from as? com.itszuvalex.itszulib.api.utility.Loc4Level)?.level ?: return null
        if (level.dimension().identifier() != loc.dimensionId || !level.isLoaded(loc.pos)) return null
        return level.getBlockEntity(loc.pos)?.let(com.itszuvalex.itszulib.api.adapters.IBlockEntity::of)?.getModule(PowerModules.WIRELESS_NODE, null)
    }

    private fun <T : Any> inRange(tracker: LocationTracker, loc: Loc4, radius: Float, module: IModule<T>): List<T> {
        val level = (loc as? com.itszuvalex.itszulib.api.utility.Loc4Level)?.level ?: return emptyList()
        return tracker.getLocationsInRange(loc, radius).filter { it != loc }.mapNotNull { l ->
            if (!level.isLoaded(l.pos)) null
            else level.getBlockEntity(l.pos)?.let(com.itszuvalex.itszulib.api.adapters.IBlockEntity::of)?.getModule(module, null)
        }.toList()
    }

    fun clear() {
        nodeTracker.clear()
        leafTracker.clear()
    }

    init {
        Femtocraft.LOGGER.debug("Wireless power manager ready")
    }
}
