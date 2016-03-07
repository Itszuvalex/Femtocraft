package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.femtocraft.power.node._
import com.itszuvalex.femtocraft.power.{ICrystalMount, IPowerPedestal, PowerManager}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import com.itszuvalex.itszulib.core.{BaseInventory, TileEntityBase}
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.util.ForgeDirection

import scala.collection.{Set, mutable}
import scala.util.Random

/**
  * Created by Christopher Harris (Itszuvalex) on 8/27/15.
  */
object TileCrystalMount {
  val MOUNT_COMPOUND = "Mount"
  val PEDESTALS_KEY  = "Pedestals"
  val CRYSTAL_KEY    = "Crystal"
  val PEDESTAL_RANGE = 16f
}

class TileCrystalMount extends TileEntityBase with PowerNode with ICrystalMount with TileInventory {
  private val pedestalLocs = mutable.HashSet[Loc4]()


  override def hasGUI = true

  override def getGuiID = GuiIDs.TileCrystalMountGuiID

  override def getMod: AnyRef = Femtocraft

  /**
    *
    * @return The color of this power node.  This is used for aesthetics.
    */
  override def getColor: Int = {
    getCrystalStack match {
      case null => super.getColor
      case i => i.getItem.asInstanceOf[IPowerCrystal].getColor(i)
    }
  }

  /**
    *
    * @return Set of all locations that have pedestal connections.
    */
  override def getPedestalLocations: Set[Loc4] = pedestalLocs

  /**
    *
    * @param loc Location to remove pedestal from.
    */
  override def removePedestal(loc: Loc4): Unit = {
    pedestalLocs -= loc
    setUpdate()
  }

  /* Tile Entity */
  override def validate(): Unit = {
    super.validate()
    if (getWorldObj.isRemote) return
    if (getCrystalStack != null)
      PowerManager.addNode(this)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (getWorldObj.isRemote) return
    if (getCrystalStack != null)
      PowerManager.removeNode(this)
  }

  /**
    *
    * @param child
    * @return True if child is capable of being a child of this node.
    */
  override def canAddChild(child: IPowerNode): Boolean = getType match {
    case IPowerNode.GENERATION_NODE => GenerationNode.canAddChild(child)
    case IPowerNode.TRANSFER_NODE => TransferNode.canAddChild(child)
    case IPowerNode.DIFFUSION_NODE => DiffusionNode.canAddChild(child)
    case _ => false
  }

  /**
    *
    * @param child
    * @return True if child is successfully added.
    */
  override def addChild(child: IPowerNode): Boolean = {
    val ret = super.addChild(child)
    setUpdate()
    ret
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    getCrystalStack match {
      case null =>
      case stack =>
        stack.getItem match {
          case null =>
          case crystal: IPowerCrystal =>
            crystal.onTick(stack)
            distributePower(stack, crystal)
          case _ =>
        }
    }
  }

  def distributePower(item: ItemStack, crystal: IPowerCrystal): Unit = {
    val rate = crystal.getTransferRate(item)
    var amount = Math.min(getPowerCurrent, rate)
    //TODO:  This is crap.  Needs to be replaced. Probably with Femto 1 algorithm.
    // Why?  Imagine 10 connections, only 1 has power.  This will be completely random on giving between
    // 1/10, 1/9, 1/8....1/1  * transferRate power to that one node.  It will give more power to the node the later in this random
    // list that it is found.  If found first, it gives the least power.
    val connections = Random.shuffle((childrenLocs + parentLoc).view.filter(_ != null).flatMap(_.getTileEntity(false)).collect { case node: IPowerNode => node }.toList)
    connections.zipWithIndex.reverse.foreach { case (tile, i) =>
      val p = tile.addPower(amount / (i + 1), doFill = true)
      usePower(p, doUse = true)
      amount -= p
                                          }
  }

  /**
    *
    * @return Amount of power currently stored in this node.
    */
  override def getPowerCurrent: Double = {
    getCrystalStack match {
      case null => 0
      case stack => stack.getItem match {
        case crystal: IPowerCrystal =>
          crystal.getStorageCurrent(stack)
        case _ => 0
      }
    }
  }

  /**
    *
    * @return Crystal ItemStack.  Null if no crystal.
    */
  override def getCrystalStack = getStackInSlot(0)

  /**
    *
    * @param amount Amount of power to consume.
    * @param doUse  True if actually change values, false to simulate.
    * @return Amount of power consumed out of @amount from the internal storage of this Tile.
    */
  override def usePower(amount: Double, doUse: Boolean): Double = {
    getCrystalStack match {
      case null => 0
      case stack => stack.getItem match {
        case crystal: IPowerCrystal =>
          crystal.consume(stack, amount, doUse)
        case _ => 0
      }
    }
  }

  /**
    *
    * @param parent IPowerNode that is being checked.
    * @return True if this node is capable of having that node as a parent.
    */
  override def canSetParent(parent: IPowerNode): Boolean = getType match {
    case IPowerNode.GENERATION_NODE => GenerationNode.canAddParent(parent)
    case IPowerNode.TRANSFER_NODE => TransferNode.canAddParent(parent)
    case IPowerNode.DIFFUSION_NODE => DiffusionNode.canAddParent(parent)
    case _ => false
  }

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType: String = getCrystalStack match {
    case stack if stack != null =>
      stack.getItem match {
        case item: IPowerCrystal => getNodeTypeFromCrystalType(item.getType(stack))
        case _ => null
      }
    case _ => null
  }

  def getNodeTypeFromCrystalType(crystalType: String): String = {
    crystalType match {
      case IPowerCrystal.TYPE_LARGE => IPowerNode.GENERATION_NODE
      case null => null
      case _ => IPowerNode.TRANSFER_NODE
    }
  }

  override def hasDescription: Boolean = true

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    slot == 0 && (item == null || (item.getItem != null && item.getItem.isInstanceOf[IPowerCrystal]))
  }

  override def defaultInventory: BaseInventory = new BaseInventory(1)

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    var co: NBTTagCompound = null
    if (getCrystalStack != null) {
      co = new NBTTagCompound
      getCrystalStack.writeToNBT(co)
    }
    compound(TileCrystalMount.CRYSTAL_KEY -> co)
    savePowerConnectionInfo(compound)
    savePedestalLocInfo(compound)
  }

  def savePedestalLocInfo(compound: NBTTagCompound): NBTTagCompound = {
    compound(
              TileCrystalMount.MOUNT_COMPOUND ->
              NBTCompound(
                           TileCrystalMount.PEDESTALS_KEY -> NBTList(pedestalLocs.map(NBTCompound))
                         )
            )
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    setInventorySlotContents(0, compound.NBTCompound(TileCrystalMount.CRYSTAL_KEY)(ItemStack.loadItemStackFromNBT))
    loadPowerConnectionInfo(compound)
    loadPedestalLocInfo(compound)
    setRenderUpdate()
  }

  override def setInventorySlotContents(slot: Int, item: ItemStack): Unit = {
    item match {
      case null =>
      case _ => item.getItem match {
        case i: IPowerCrystal =>
          if (getNodeTypeFromCrystalType(i.getType(item)) != getType)
            if (slot == 0 && inventory.getInventory(slot) != null) {
              inventory.getInventory(slot) = null
              markDirty()
            }
        case _ =>
      }
    }
    super.setInventorySlotContents(slot, item)
  }

  override def markDirty(): Unit = {
    super.markDirty()
    if (getWorldObj.isRemote) return
    setModified()
    setUpdate()
    getCrystalStack match {
      case null =>
        PowerManager.removeNode(this)
        getChildren.foreach(_.setParent(null))
        childrenLocs.clear()
        Option(getParent).filter(_ != this).map(_.removeChild(this))
        parentLoc = null
      case _ => PowerManager.addNode(this)
    }
  }

  def loadPedestalLocInfo(compound: NBTTagCompound): Unit = {
    compound.NBTCompound(TileCrystalMount.MOUNT_COMPOUND) { comp =>
      pedestalLocs.clear()
      pedestalLocs ++= comp.NBTList(TileCrystalMount.PEDESTALS_KEY).map(Loc4(_))
                                                          }
    setRenderUpdate()
  }

  override def loadPowerConnectionInfo(compound: NBTTagCompound): Unit = {
    super.loadPowerConnectionInfo(compound)
    setRenderUpdate()
  }

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    savePedestalLocInfo(compound)
    savePowerConnectionInfo(compound)

  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    loadPedestalLocInfo(compound)
    loadPowerConnectionInfo(compound)
  }

  override def getRenderBoundingBox: AxisAlignedBB = {
    val center = Vector3(xCoord + .5f, yCoord + .5f, zCoord + .5f)
    AxisAlignedBB.getBoundingBox(center.x - childrenConnectionRadius,
                                 center.y - childrenConnectionRadius,
                                 center.z - childrenConnectionRadius,
                                 center.x + childrenConnectionRadius,
                                 center.y + childrenConnectionRadius,
                                 center.z + childrenConnectionRadius)
  }

  /**
    *
    * @return Maximum distance children can be from this node, to connect.
    */
  override def childrenConnectionRadius: Float = TileCrystalMount.PEDESTAL_RANGE

  override def onBlockBreak(): Unit = {
    pedestalLocs.flatMap(_.getTileEntity(true)).collect { case p: IPowerPedestal => p }.foreach(_.setMount(null))
    super.onBlockBreak()
  }

  def onPostBlockPlaced(): Unit = {
    if (getWorldObj.isRemote) return
    checkAndAddPedestal(ForgeDirection.UP)
    checkAndAddPedestal(ForgeDirection.DOWN)
  }

  def checkAndAddPedestal(dir: ForgeDirection) = {
    getLoc.getOffset(dir).getTileEntity(true) match {
      case Some(i: IPowerPedestal) =>
        if (i.mountLoc == null && i.canSetMount(getLoc) && canAcceptPedestal(getLoc.getOffset(dir))) {
          addPedestal(getLoc.getOffset(dir))
          i.setMount(getLoc)
        }
      case _ =>
    }
  }

  /**
    *
    * @param loc Location to add as pedestal
    */
  override def addPedestal(loc: Loc4): Unit = {
    if (!canAcceptPedestal(loc)) return
    pedestalLocs += loc
    setUpdate()
  }

  /**
    *
    * @param loc Location of pedestal to connect with.
    * @return True if this block can accept a pedestal connection from this location.
    */
  override def canAcceptPedestal(loc: Loc4): Boolean = {
    getLoc.getOffset(ForgeDirection.UP) == loc ||
    getLoc.getOffset(ForgeDirection.DOWN) == loc
  }

  /**
    *
    * @param child
    * @return True if child was a child of this node, and was successfully removed.
    */
  override def removeChild(child: IPowerNode): Boolean = {
    val ret = super.removeChild(child)
    setUpdate()
    ret
  }

  /**
    *
    * @param amount Amount of power to add.
    * @param doFill True if actually change values, false to simulate.
    * @return Amount of power used out of @amount to fill the internal storage of this Tile.
    */
  override def addPower(amount: Double, doFill: Boolean): Double = {
    getCrystalStack match {
      case null => 0
      case stack => stack.getItem match {
        case null => 0
        case crystal: IPowerCrystal =>
          crystal.store(stack, amount, doFill)
      }
    }
  }

  /**
    *
    * @param amount Set current stored power to the given value.
    */
  override def setPower(amount: Double): Unit = {
    getCrystalStack match {
      case null =>
      case stack => stack.getItem match {
        case null =>
        case crystal: IPowerCrystal =>
          crystal.setStorageCurrent(stack, amount)
      }
    }
  }

  /**
    *
    * @return Amount of power capable of being stored in this node.
    */
  override def getPowerMax: Double = {
    getCrystalStack match {
      case null => 0
      case stack => stack.getItem match {
        case null => 0
        case crystal: IPowerCrystal =>
          crystal.getStorageMax(stack)
      }
    }
  }
}
