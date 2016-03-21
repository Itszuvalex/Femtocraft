package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.graph.{IManyChildNode, ISingleParentNode}
import com.itszuvalex.itszulib.api.core.Loc4

/**
  * Created by Christopher Harris (Itszuvalex) on 8/3/15.
  */
object IPowerNode {
  val CRYSTAL_MOUNT         = "Mount"
  val TRANSFER_NODE         = "Transfer"
  val DIFFUSION_NODE        = "Diffusion"
  val DIFFUSION_TARGET_NODE = "Diffusion_Target"
  val DIRECT_NODE           = "Direct"
  val LONE_NODE             = "Lone"
  val DEFAULT_MAX_RADIUS    = 8f
}

trait IPowerNode extends ISingleParentNode[IPowerNode] with IManyChildNode[IPowerNode] {

  /**
    *
    * @return The type of PowerNode this is.
    */
  def getType: String

  /**
    *
    * @return The IPowerNode this has as its parent.  If this is of type 'Generation', this will be itself.
    */
  override def getParent: IPowerNode

  /**
    *
    * @return Loc4 of this node's parent, null if it has no parent.  This is primarily to bypass chunk churn, as a node may have a parent set but the parent is in an unloaded chunk.  If that is the case, then
    *         it can return its parent location here, without having to explicitly load that chunk.
    */
  def getParentLoc: Loc4

  /**
    *
    * @param parent IPowerNode that is being checked.
    * @return True if this node is capable of having that node as a parent.
    */
  override def canSetParent(parent: IPowerNode): Boolean

  /**
    *
    * @param parent Parent being set.
    * @return True if parent is successfully set to input parent.
    */
  override def setParent(parent: IPowerNode): Boolean

  /**
    *
    * @return Maximum distance to look for parents in.
    */
  def parentConnectionRadius: Float

  /**
    *
    * @return Iterable of IPowerNodes this has as children. If this is a leaf node, returns null, otherwise, empty list.
    */
  override def getChildren: scala.collection.Set[IPowerNode]

  /**
    *
    * @return Iterable of Loc4s containing the locations of this node's children.  If this is a leaf node, returns null.
    *         This is to bypass chunk churn by using a reference to the location containing the tile entity, instead of having to load
    *         the chunk.
    */
  def getChildrenLocs: scala.collection.Set[Loc4]

  /**
    *
    * @param child
    * @return True if child is capable of being a child of this node.
    */
  override def canAddChild(child: IPowerNode): Boolean

  /**
    *
    * @param child
    * @return True if child is successfully added.
    */
  override def addChild(child: IPowerNode): Boolean

  /**
    *
    * @param child
    * @return True if child was a child of this node, and was successfully removed.
    */
  override def removeChild(child: IPowerNode): Boolean

  /**
    *
    * @return Maximum distance children can be from this node, to connect.
    */
  def childrenConnectionRadius: Float

  /**
    *
    * @return Get world loc of this node.  This will be the location used for tracking and range calculations.
    */
  def getNodeLoc: Loc4

  /**
    *
    * @return Amount of power currently stored in this node.
    */
  def getPowerCurrent: Double

  /**
    *
    * @return Amount of power capable of being stored in this node.
    */
  def getPowerMax: Double

  /**
    *
    * @param amount Amount of power to add.
    * @param doFill True if actually change values, false to simulate.
    * @return Amount of power used out of @amount to fill the internal storage of this Tile.
    */
  def addPower(amount: Double, doFill: Boolean): Double

  /**
    *
    * @param amount Set current stored power to the given value.
    */
  def setPower(amount: Double)

  /**
    *
    * @param amount Amount of power to consume.
    * @param doUse  True if actually change values, false to simulate.
    * @return Amount of power consumed out of @amount from the internal storage of this Tile.
    */
  def usePower(amount: Double, doUse: Boolean): Double

  /**
    *
    * @return The color of this power node.  This is used for aesthetics.
    */
  def getColor: Int

}
