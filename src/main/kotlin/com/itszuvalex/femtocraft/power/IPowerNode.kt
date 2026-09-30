package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.utility.Loc4

/**
 * A node in the crystal power tree. Each node has at most one parent and any number of children, found by radius.
 * Power flows along parent/child links (see [com.itszuvalex.femtocraft.power.tile.CrystalMountBlockEntity.distributePower]).
 *
 * Exposed on block entities through [com.itszuvalex.femtocraft.FemtoModules.POWER_NODE].
 */
interface IPowerNode {
    /**
     * @return The type of node, one of [PowerNodeTypes]. Parent/child whitelists are expressed in types.
     */
    fun getType(): String

    /**
     * @return The parent node, if it is loaded. A generation node is its own parent.
     */
    fun getParent(): IPowerNode?

    /**
     * @return Location of this node's parent, or null if it has none. Kept even while the parent's chunk is unloaded.
     */
    fun getParentLoc(): Loc4?

    fun canSetParent(parent: IPowerNode): Boolean

    /**
     * @param parent New parent, or null to clear it.
     * @return True if the parent was set.
     */
    fun setParent(parent: IPowerNode?): Boolean

    /**
     * @return Maximum distance to look for parents in.
     */
    fun parentConnectionRadius(): Float

    /**
     * @return Loaded children, or null for a leaf node.
     */
    fun getChildren(): Set<IPowerNode>?

    /**
     * @return Children locations, or null for a leaf node.
     */
    fun getChildrenLocs(): Set<Loc4>?

    fun canAddChild(child: IPowerNode): Boolean

    fun addChild(child: IPowerNode): Boolean

    /**
     * @return True if the child was a child of this node and was removed.
     */
    fun removeChild(child: IPowerNode): Boolean

    /**
     * @return Maximum distance children can be from this node to connect.
     */
    fun childrenConnectionRadius(): Float

    /**
     * @return World location used for tracking and range calculations.
     */
    fun getNodeLoc(): Loc4

    fun getPowerCurrent(): Double

    fun getPowerMax(): Double

    /**
     * @param doFill False to simulate.
     * @return Amount of [amount] stored.
     */
    fun addPower(amount: Double, doFill: Boolean): Double

    fun setPower(amount: Double)

    /**
     * @param doUse False to simulate.
     * @return Amount of [amount] consumed.
     */
    fun usePower(amount: Double, doUse: Boolean): Double

    /**
     * @return ARGB color, for looks.
     */
    fun getColor(): Int
}

object PowerNodeTypes {
    const val CRYSTAL_MOUNT = "Mount"
    const val TRANSFER_NODE = "Transfer"
    const val DIFFUSION_NODE = "Diffusion"
    const val DIFFUSION_TARGET_NODE = "Diffusion_Target"
    const val DIRECT_NODE = "Direct"
    const val LONE_NODE = "Lone"
    const val DEFAULT_MAX_RADIUS = 8f
}
