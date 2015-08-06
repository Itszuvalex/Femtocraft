package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.tileentity.TileEntity

import scala.collection._
import scala.util.Random

/**
 * Created by Christopher Harris (Itszuvalex) on 8/3/15.
 */
object PowerNode {
  val POWER_COMPOUND_KEY = "FemtoPower"
  val NODE_PARENT_KEY    = "Parent"
  val NODE_CHILDREN_KEY  = "Children"
  val COLOR_KEY          = "Color"
}


trait PowerNode extends TileEntity with IPowerNode {
  var parentLoc   : Loc4 = null
  val childrenLocs       = mutable.HashSet[Loc4]()
  var powerCurrent: Long = 0
  var powerMax    : Long = 0
  var color              = Color(255.toByte, (Random.nextInt(125) + 115).toByte,
                                    (Random.nextInt(125) + 115).toByte,
                                    (Random.nextInt(125) + 115).toByte  ).toInt
  var offsetColors : Array[Int] = new Array(11)
  (1 to 10).foreach( num => offsetColors{num} = Color((Random.nextInt(70) + 150).toByte,
                                                      (Random.nextInt(30) - 15).toByte,
                                                      (Random.nextInt(30) - 15).toByte,
                                                      (Random.nextInt(30) - 15).toByte).toInt )

  initializePowerSettings()

  def initializePowerSettings(): Unit


  def savePowerConnectionInfo(compound: NBTTagCompound) =  {
    val powerCompound = new NBTTagCompound
    val parentLocCompound = new NBTTagCompound
    if (parentLoc != null) {
      parentLoc.saveToNBT(parentLocCompound)
      powerCompound.setTag(PowerNode.NODE_PARENT_KEY, parentLocCompound)
    }
    val childrenLocsList = new NBTTagList
    childrenLocs.foreach { child =>
      val childCompound = new NBTTagCompound
      child.saveToNBT(childCompound)
      childrenLocsList.appendTag(childCompound)
                         }
    powerCompound.setTag(PowerNode.NODE_CHILDREN_KEY, childrenLocsList)
    powerCompound.setInteger(PowerNode.COLOR_KEY, color)
    compound.setTag(PowerNode.POWER_COMPOUND_KEY, powerCompound)
  }

  def loadPowerConnectionInfo(compound: NBTTagCompound) = {
    val powerCompound = compound.getCompoundTag(PowerNode.POWER_COMPOUND_KEY)
    if (powerCompound.hasKey(PowerNode.NODE_PARENT_KEY)) {
      parentLoc = Loc4(0, 0, 0, 0)
      parentLoc.loadFromNBT(powerCompound.getCompoundTag(PowerNode.NODE_PARENT_KEY))
    }
    else parentLoc = null
    color = powerCompound.getInteger(PowerNode.COLOR_KEY)
    childrenLocs.clear()
    val childrenList = powerCompound.getTagList(PowerNode.NODE_CHILDREN_KEY, 10)
    childrenLocs ++= (0 until childrenList.tagCount()).view.map(childrenList.getCompoundTagAt).map { compound =>
      val loc = Loc4(0, 0, 0, 0)
      loc.loadFromNBT(compound)
      loc
                                                                                                   }
  }

  def onBlockBreak() = {
    PowerManager.removeNode(this)
    val parent = getParent
    if (parent != null && parent != this) parent.removeChild(this)
    val children = getChildren
    if (children != null) children.foreach(_.setParent(null))
  }


  /* Tile Entity */
  override def validate(): Unit = {
    super.validate()
    if (!getWorldObj.isRemote) PowerManager.addNode(this)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (!getWorldObj.isRemote) PowerManager.removeNode(this)
  }

  override def writeToNBT(p_145841_1_ : NBTTagCompound): Unit = {
    super.writeToNBT(p_145841_1_)
    savePowerConnectionInfo(p_145841_1_)
  }

  override def readFromNBT(p_145839_1_ : NBTTagCompound): Unit = {
    super.readFromNBT(p_145839_1_)
    loadPowerConnectionInfo(p_145839_1_)
  }

  /* IPowerNode */
  /**
   *
   * @param child
   * @return True if child is successfully added.
   */
  override def addChild(child: IPowerNode): Boolean = {
    if(child == null) return true
    childrenLocs += child.getNodeLoc
    true
  }


  /**
   *
   * @param child
   * @return True if child was a child of this node, and was successfully removed.
   */
  override def removeChild(child: IPowerNode): Boolean = {
    if(child == null) return true
    if (childrenLocs.contains(child.getNodeLoc)) {
      childrenLocs -= child.getNodeLoc
      true
    }
    else false
  }

  /**
   *
   * @param child
   * @return True if child is capable of being a child of this node.
   */
  override def canAddChild(child: IPowerNode): Boolean = {
    child != null && child.getNodeLoc != parentLoc
  }

  /**
   *
   * @param parent IPowerNode that is being checked.
   * @return True if this node is capable of having that node as a parent.
   */
  override def canAddParent(parent: IPowerNode): Boolean = {
    parent != null && !childrenLocs.contains(parent.getNodeLoc)
  }

  /**
   *
   * @return Amount of power capable of being stored in this node.
   */
  override def getPowerMax: Long = powerMax

  /**
   *
   * @return Get world loc of this node.  This will be the location used for tracking and range calculations.
   */
  override def getNodeLoc: Loc4 = Loc4(xCoord, yCoord, zCoord, getWorldObj.provider.dimensionId)

  /**
   *
   * @param parent Parent being set.
   * @return True if parent is successfully set to input parent.
   */
  override def setParent(parent: IPowerNode): Boolean = {
    if (parent != null) {
      parentLoc = parent.getNodeLoc
    } else {
      parentLoc = null
    }
    PowerManager.refreshParentlessStatus(this)
    true
  }

  /**
   *
   * @param amount Set current stored power to the given value.
   */
  override def setPower(amount: Long): Unit = powerCurrent = amount

  /**
   *
   * @return Maximum distance to look for parents in.
   */
  override def parentConnectionRadius: Float = IPowerNode.DEFAULT_MAX_RADIUS

  /**
   *
   * @return The IPowerNode this has as its parent.  If this is of type 'Power', this will be itself.
   */
  override def getParent: IPowerNode = if (parentLoc == null) null
  else parentLoc.getTileEntity(true) match {
    case Some(i) if i.isInstanceOf[IPowerNode] => i.asInstanceOf[IPowerNode]
    case None => null
  }

  /**
   *
   * @param amount Amount of power to consume.
   * @param doUse True if actually change values, false to simulate.
   * @return Amount of power consumed out of @amount from the internal storage of this Tile.
   */
  override def usePower(amount: Long, doUse: Boolean): Long = {
    val min = Math.min(amount, powerCurrent)
    if (doUse)
      powerCurrent -= min
    min
  }

  /**
   *
   * @return Amount of power currently stored in this node.
   */
  override def getPowerCurrent: Long = powerCurrent

  /**
   *
   * @return Iterable of IPowerNodes this has as children. If this is a leaf node, returns null, otherwise, empty list.
   */
  override def getChildren: Iterable[IPowerNode] = childrenLocs.flatMap(_.getTileEntity(true)).collect { case node: IPowerNode => node }

  /**
   *
   * @return Iterable of Loc4s containing the locations of this node's children.  If this is a leaf node, returns null.
   *         This is to bypass chunk churn by using a reference to the location containing the tile entity, instead of having to load
   *         the chunk.
   */
  override def getChildrenLocs: scala.Iterable[Loc4] = childrenLocs

  /**
   *
   * @return Maximum distance children can be from this node, to connect.
   */
  override def childrenConnectionRadius: Float = IPowerNode.DEFAULT_MAX_RADIUS

  /**
   *
   * @param amount Amount of power to add.
   * @param doFill True if actually change values, false to simulate.
   * @return Amount of power used out of @amount to fill the internal storage of this Tile.
   */
  override def addPower(amount: Long, doFill: Boolean): Long = {
    val min = Math.min(amount, powerMax - powerCurrent)
    if (doFill)
      powerCurrent += min
    min
  }

  /**
   *
   * @return The color of this power node.  This is used for aesthetics.
   */
  override def getColor: Int = color

  /**
   *
   * @return The color offsets of every crystal of this node. Also for aesthetics.
   */
  override def getOffsetColors : Array[Int] = offsetColors

  /**
   *
   * @return Loc4 of this node's parent, null if it has no parent.  This is primarily to bypass chunk churn, as a node may have a parent set but the parent is in an unloaded chunk.  If that is the case, then
   *         it can return its parent location here, without having to explicitly load that chunk.
   */
  override def getParentLoc: Loc4 = parentLoc
}
