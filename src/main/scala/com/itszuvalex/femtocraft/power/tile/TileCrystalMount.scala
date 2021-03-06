package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.power.item.IPowerCrystal
import com.itszuvalex.femtocraft.power.node._
import com.itszuvalex.femtocraft.power.{ICrystalMount, PowerManager}
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

/**
  * Created by Christopher Harris (Itszuvalex) on 8/27/15.
  */
object TileCrystalMount {
  val MOUNT_COMPOUND = "Mount"
  val PEDESTALS_KEY  = "Pedestals"
  val CRYSTAL_KEY    = "Crystal"
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
  override def removePedestal(loc: Loc4): Unit = pedestalLocs -= loc

  /**
    *
    * @param loc Location of pedestal to connect with.
    * @return True if this block can accept a pedestal connection from this location.
    */
  override def canAcceptPedestal(loc: Loc4): Boolean = getLoc.getOffset(ForgeDirection.UP) == loc

  /**
    *
    * @param loc Location to add as pedestal
    */
  override def addPedestal(loc: Loc4): Unit = pedestalLocs += loc

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
    setUpdate()
    super.addChild(child)
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
          case _ =>
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

  /**
    *
    * @return Crystal ItemStack.  Null if no crystal.
    */
  override def getCrystalStack = getStackInSlot(0)

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
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    setInventorySlotContents(0, compound.NBTCompound(TileCrystalMount.CRYSTAL_KEY)(ItemStack.loadItemStackFromNBT))
    loadPowerConnectionInfo(compound)
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

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound(
              TileCrystalMount.MOUNT_COMPOUND ->
              NBTCompound(
                           TileCrystalMount.PEDESTALS_KEY -> NBTList(pedestalLocs.map(NBTCompound))
                         )
            )
    savePowerConnectionInfo(compound)

  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    compound.NBTCompound(TileCrystalMount.MOUNT_COMPOUND) { comp =>
      pedestalLocs.clear()
      pedestalLocs ++= comp.NBTList(TileCrystalMount.PEDESTALS_KEY).map(Loc4(_))
                                                          }
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
}
