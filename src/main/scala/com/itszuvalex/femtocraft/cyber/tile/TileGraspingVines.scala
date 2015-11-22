package com.itszuvalex.femtocraft.cyber.tile

import java.util.UUID

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.itszulib.api.core.{Configurable, Loc4}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.{MultiBlockComponent, TileFluidTank}
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{Fluid, FluidTank}

import scala.collection.JavaConversions._
import scala.collection.mutable

@Configurable
object TileGraspingVines {
  @Configurable
  val DEFAULT_GRAB_RADIUS = 32f
  val grabbedHashSet      = new mutable.HashSet[UUID]()

  val COMPOUND_IDLIST = "IDList"
}

/**
  * Created by Christopher on 11/21/2015.
  */
@Configurable
class TileGraspingVines extends TileEntityBase with MultiBlockComponent with TileMultiblockIndexedInventory with TileFluidTank {
  var machineIndex    : Int   = -1
  var basePos         : Loc4  = null
  var velocityAddition: Float = .2f
  var grabRadius      : Float = TileGraspingVines.DEFAULT_GRAB_RADIUS
  var serverSet               = new mutable.HashSet[Entity]()
  var clientSet               = mutable.HashSet[Int]()

  def grabbedSet: mutable.HashSet[Entity] = if (getWorldObj.isRemote) {
    clientSet.flatMap(id => Option(getWorldObj.getEntityByID(id)))
  } else serverSet

  override def serverUpdate(): Unit = {
    super.serverUpdate()
  }

  override def updateEntity(): Unit = {
    super.updateEntity()
    if (findAndGrabEntities(grabRadius))
      setUpdate()
    pullEntities()
  }

  def findAndGrabEntities(radius: Float): Boolean = {
    var changed = false
    getWorldObj.getEntitiesWithinAABB(classOf[Entity], AxisAlignedBB.getBoundingBox(xCoord + .5f - radius,
                                                                                    yCoord + .5f - radius,
                                                                                    zCoord + .5f - radius,
                                                                                    xCoord + .5f + radius,
                                                                                    yCoord + .5f + radius,
                                                                                    zCoord + .5f + radius))
    .asInstanceOf[java.util.List[Entity]]
    .view
    .filter { entity => entity.getDistanceSq(xCoord + .5d, yCoord + .5d, zCoord + .5d) <= radius * radius }
    .foreach { entity =>
      changed = grabEntity(entity) || changed
             }
    changed
  }

  def pullEntities() = {
    val toRemove = new mutable.HashSet[Entity]()
    grabbedSet.foreach { entity =>
      if (entity.isDead)
        toRemove += entity
      else {
        entity match {
          case p: EntityPlayer if p.capabilities.isCreativeMode =>
          case _ =>
            val targetPos = Vector3(xCoord + .5f, yCoord + .5f, zCoord + .5f)
            val entityPos = Vector3(entity.posX, entity.posY, entity.posZ)
            val vel = (targetPos - entityPos).normalize() * velocityAddition
            entity.addVelocity(vel.x, vel.y, vel.z)
        }
      }
      toRemove.foreach(removeEntity)
                       }
  }

  def grabEntity(entity: Entity): Boolean = {
    if (TileGraspingVines.grabbedHashSet.contains(entity.getUniqueID)) false
    else {
      TileGraspingVines.grabbedHashSet += entity.getUniqueID
      grabbedSet += entity
      true
    }
  }

  def removeEntity(entity: Entity): Boolean = {
    if (grabbedSet.contains(entity.getUniqueID)) {
      grabbedSet.remove(entity.getUniqueID)
      TileGraspingVines.grabbedHashSet -= entity.getUniqueID
      true
    } else false
  }

  override def invalidate(): Unit = {
    super.invalidate()
    grabbedSet.foreach(entity => TileGraspingVines.grabbedHashSet.remove(entity.getUniqueID))
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    clientSet ++= compound.IntArray(TileGraspingVines.COMPOUND_IDLIST) match {
      case ia =>
        grabbedSet.clear()
        clientSet ++= ia
      case _ =>
    }
  }


  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound(TileGraspingVines.COMPOUND_IDLIST -> grabbedSet.map(_.getEntityId).toArray)
  }

  def onBlockBreak() = {}

  override def getMod: AnyRef = Femtocraft

  override def defaultTank: FluidTank = new FluidTank(1000)

  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def defaultInventory: IndexedInventory = new IndexedInventory(0)

  override def hasDescription: Boolean = true
}
