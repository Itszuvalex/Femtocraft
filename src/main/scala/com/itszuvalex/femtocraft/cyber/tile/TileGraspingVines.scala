package com.itszuvalex.femtocraft.cyber.tile

import java.util.UUID

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.machine.MachineGraspingVines
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.itszulib.api.core.Configurable
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileFluidTank
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraftforge.fluids.{Fluid, FluidTank}

import scala.collection.JavaConversions._
import scala.collection.mutable

@Configurable
object TileGraspingVines {
  @Configurable
  val DEFAULT_GRAB_RADIUS = 8f
  val grabbedHashSet      = new mutable.HashSet[UUID]()

  val COMPOUND_IDLIST_KEY = "IDList"
}

/**
  * Created by Christopher on 11/21/2015.
  */
@Configurable
class TileGraspingVines extends TileEntityBase with CyberMachineMultiblock with TileMultiblockIndexedInventory with TileFluidTank {
  var velocityAddition: Float = .2f
  var grabRadius      : Float = TileGraspingVines.DEFAULT_GRAB_RADIUS
  var entitySet               = new mutable.HashSet[Entity]()
  var clientSet               = mutable.HashSet[Int]()

  override def serverUpdate(): Unit = {
    findAndGrabEntities(grabRadius)
    super.serverUpdate()
  }

  def findAndGrabEntities(radius: Float): Unit = {
    getWorld.getEntitiesWithinAABB(classOf[Entity], new AxisAlignedBB(getPos.getX + .5f - radius,
      getPos.getY + 1f - radius,
      getPos.getZ + .5f - radius,
      getPos.getX + .5f + radius,
      getPos.getY + 1f + radius,
      getPos.getZ + .5f + radius))
      .asInstanceOf[java.util.List[Entity]]
      .view
      .filter { entity => entity.getDistanceSq(getPos.getX + .5d, getPos.getY + 1d, getPos.getZ + .5d) <= radius * radius }
      .foreach(grabEntity)
  }

  def grabEntity(entity: Entity): Boolean = {
    if (TileGraspingVines.grabbedHashSet.contains(entity.getUniqueID)) false
    else {
      TileGraspingVines.grabbedHashSet += entity.getUniqueID
      grabbedSet += entity
      setUpdate()
      true
    }
  }

  override def update(): Unit = {
    super.update()
    pullEntities()
  }

  def pullEntities() = {
    val toRemove = new mutable.HashSet[Entity]()
    grabbedSet.foreach { entity =>
      if (entity.isDead)
        toRemove += entity
      else {
        entity match {
          case p if p.getDistanceSq(getPos.getX + .5d, getPos.getY + .5d, getPos.getZ + .5d) > (grabRadius * grabRadius) =>
            toRemove += p
          case p: EntityPlayer if p.capabilities.isCreativeMode =>
          case _ =>
            val targetPos = Vector3(getPos.getX + .5f, getPos.getY + .5f, getPos.getZ + .5f)
            val entityPos = Vector3(entity.posX, entity.posY, entity.posZ)
            val vel = (targetPos - entityPos).normalize() * velocityAddition
            entity.addVelocity(vel.x, vel.y, vel.z)
        }
      }
      toRemove.foreach(removeEntity)
    }
  }

  def removeEntity(entity: Entity): Boolean = {
    if (grabbedSet.contains(entity)) {
      grabbedSet.remove(entity)
      TileGraspingVines.grabbedHashSet -= entity.getUniqueID
      setUpdate()
      true
    } else false
  }

  def grabbedSet: mutable.HashSet[Entity] = {
    if (getWorld.isRemote) {
      //Find entities based on ids passed by server.  Cache the found entities so we don't keep looking them up from the worldObj every call.
      val entities = clientSet.flatMap(id => Option(getWorld.getEntityByID(id)))
      clientSet --= entities.map(_.getEntityId)
      entitySet ++= entities
    }
    entitySet
  }

  override def invalidate(): Unit = {
    super.invalidate()
    grabbedSet.foreach(removeEntity)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    compound.IntArray(TileGraspingVines.COMPOUND_IDLIST_KEY) match {
      case ia =>
        clientSet.clear()
        entitySet.clear()
        clientSet ++= ia
      case _ =>
    }
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound(TileGraspingVines.COMPOUND_IDLIST_KEY -> grabbedSet.map(_.getEntityId).toArray)
  }

  override def onBlockBreak(): Unit = {
    if (!isController) {
      worldObj.setBlockToAir(info.cLoc.getPos)
      return
    }
    if (worldObj.isRemote) return
    basePos.getTileEntity() match {
      case Some(te: TileCyberBase) =>
        te.breakMachinesUpwardsFromSlot(machineIndex)
      case _ =>
    }
  }

  override def getMod: AnyRef = Femtocraft

  override def defaultTank: FluidTank = new FluidTank(1000)

  override def canFill(from: EnumFacing, fluid: Fluid): Boolean = false

  override def canDrain(from: EnumFacing, fluid: Fluid): Boolean = false

  override def defaultInventory: IndexedInventory = new IndexedInventory(9)

  override def hasDescription: Boolean = true

  override def getRenderBoundingBox: AxisAlignedBB = {
    val center = Vector3(getPos.getX + .5f, getPos.getY + 1f, getPos.getZ + .5f)
    new AxisAlignedBB(center.x - TileGraspingVines.DEFAULT_GRAB_RADIUS,
      center.y - TileGraspingVines.DEFAULT_GRAB_RADIUS,
      center.z - TileGraspingVines.DEFAULT_GRAB_RADIUS,
      center.x + TileGraspingVines.DEFAULT_GRAB_RADIUS,
      center.y + TileGraspingVines.DEFAULT_GRAB_RADIUS,
      center.z + TileGraspingVines.DEFAULT_GRAB_RADIUS)
  }

  override def getCyberMachine = MachineGraspingVines.NAME
}
