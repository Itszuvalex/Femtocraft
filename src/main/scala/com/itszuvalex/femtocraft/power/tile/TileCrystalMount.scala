package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.{IPowerCrystal, ICrystalMount, PowerManager}
import com.itszuvalex.femtocraft.power.node._
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.traits.tile.TileInventory
import com.itszuvalex.itszulib.core.{BaseInventory, TileEntityBase}
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
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

  override def getMod: AnyRef = Femtocraft

  /**
    *
    * @return Crystal ItemStack.  Null if no crystal.
    */
  override def getCrystalStack = getStackInSlot(0)

  /**
    *
    * @return Set of all locations that have pedestal connections.
    */
  override def getPedestalDirections: Set[Loc4] = pedestalLocs

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
    * @param parent IPowerNode that is being checked.
    * @return True if this node is capable of having that node as a parent.
    */
  override def canAddParent(parent: IPowerNode): Boolean = getType match {
    case IPowerNode.GENERATION_NODE => GenerationNode.canAddParent(parent)
    case IPowerNode.TRANSFER_NODE => TransferNode.canAddParent(parent)
    case IPowerNode.DIFFUSION_NODE => DiffusionNode.canAddParent(parent)
    case _ => false
  }

  override def hasDescription: Boolean = true

  override def isItemValidForSlot(slot: Int, item: ItemStack): Boolean = {
    slot == 0 && (item == null || (item.getItem != null && item.getItem.isInstanceOf[IPowerCrystal]))
  }


  override def markDirty(): Unit = {
    super.markDirty()
    setModified()
    setUpdate()
    getCrystalStack match {
      case null => PowerManager.removeNode(this)
      case _ => PowerManager.addNode(this)
    }
  }

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType: String = getCrystalStack match {
    case stack if stack != null =>
      stack.getItem match {
        case item: IPowerCrystal =>
          item.getType(stack) match {
            case IPowerCrystal.TYPE_LARGE => IPowerNode.GENERATION_NODE
            case null => null
            case _ => IPowerNode.TRANSFER_NODE
          }
        case _ => null
      }
    case _ => null
  }

  override def defaultInventory: BaseInventory = new BaseInventory(1)

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    var co: NBTTagCompound = null
    if (getCrystalStack != null) {
      co = new NBTTagCompound
      getCrystalStack.writeToNBT(compound)
    }
    compound(TileCrystalMount.CRYSTAL_KEY -> co)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    setInventorySlotContents(0, compound.NBTCompound(TileCrystalMount.CRYSTAL_KEY)(ItemStack.loadItemStackFromNBT))
    setRenderUpdate()
  }

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound.NBTCompound(TileCrystalMount.MOUNT_COMPOUND) { comp =>
      pedestalLocs.clear()
      pedestalLocs ++= comp.NBTList(TileCrystalMount.PEDESTALS_KEY).map(Loc4(_))
                                                          }
    if (getType != null) PowerManager.addNode(this)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    compound(
              TileCrystalMount.MOUNT_COMPOUND ->
              NBTCompound(
                           TileCrystalMount.PEDESTALS_KEY -> NBTList(pedestalLocs.map(NBTCompound))
                         )
            )
    if (getType != null) PowerManager.removeNode(this)
  }
}
