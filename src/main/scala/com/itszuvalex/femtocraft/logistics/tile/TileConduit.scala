package com.itszuvalex.femtocraft.logistics.tile

import java.util

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics._
import com.itszuvalex.femtocraft.logistics.connections.ItemConnection
import com.itszuvalex.femtocraft.logistics.tile.TileConduit.ConduitImpl
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 2/16/2017.
  */
object TileConduit {
  val CONDUIT_KEY = "conduit"

  object ConduitImpl {
    val CONNECTION_KEY = "connections"
    val BLOCKED_KEY = "blocked"
    val STORAGE_KEY = "storage"
  }

  class ConduitImpl(val conduit: TileConduit) extends IConduit with ILogisticsNetworkNode {
    val connections = new Array[Boolean](6)
    val blocked = new Array[Boolean](6)
    var seek = true
    val connectionMap: mutable.HashMap[EnumFacing, util.Map[IResource[_], util.Collection[IConnection[_]]]] = new mutable.HashMap[EnumFacing, util.Map[IResource[_], util.Collection[IConnection[_]]]]()
    val connectionStorage = Array(new ItemStorageArray(4), new ItemStorageArray(4), new ItemStorageArray(4), new ItemStorageArray(4), new ItemStorageArray(4), new ItemStorageArray(4))

    def addConnection(facing: EnumFacing, con: IConnection[_]): Boolean = {
      connectionMap.getOrElseUpdate(facing, new mutable.HashMap[IResource[_], util.Collection[IConnection[_]]]).getOrElseUpdate(con.resource, new ArrayBuffer[IConnection[_]]).add(con)
    }

    def removeConnection(facing: EnumFacing, con: IConnection[_]): Boolean = {
      val facingMap = connectionMap.getOrElse(facing, return false)
      val col = facingMap.getOrElse(con.resource, return false)
      val ret = col.remove(con)
      if (col.isEmpty)
        facingMap.remove(con.resource)
      if (facingMap.isEmpty)
        connectionMap.remove(facing)
      ret
    }

    override def getConnections(facing: EnumFacing): util.Map[IResource[_], util.Collection[IConnection[_]]] = connectionMap.getOrElse(facing, new mutable.HashMap[IResource[_], util.Collection[IConnection[_]]]())

    override def getLoc: Loc4 = conduit.getLoc

    override def canConnect(loc: Loc4): Boolean = {
      if (!super.canConnect(loc)) return false

      EnumFacing.VALUES.exists { f =>
        val l = getLoc.getOffset(f)
        (l == loc) && !blocked(f.getIndex)
      }
    }

    override def isConnected(facing: EnumFacing): Boolean = !blocked(facing.getIndex) && connections(facing.getIndex)

    override def canAddConnection(facing: EnumFacing): Boolean = !blocked(facing.getIndex)

    override def addConnection(facing: EnumFacing): Unit = {
      connections(facing.getIndex) = true
      conduit.setModified()
      conduit.setUpdate()
    }

    override def removeConnection(facing: EnumFacing): Unit = {
      connections(facing.getIndex) = false
      conduit.setModified()
      conduit.setUpdate()
    }

    def connectToNetwork(loc: Loc4, facing: EnumFacing): Unit = {
      loc.getTileEntity(false) match {
        case None =>
        case Some(i: TileEntity) if i.hasCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite) =>
          val cap = i.getCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite)
          val network = if (cap.getNetwork != null) cap.getNetwork else getNetwork
          network.addConnection(getLoc, cap.getLoc)
        case _ =>
      }
    }

    def disconnectFromNetwork(loc: Loc4, facing: EnumFacing): Unit = {
      loc.getTileEntity(false) match {
        case None =>
        case Some(i: TileEntity) if i.hasCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite) =>
          val cap = i.getCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite)
          getNetwork.removeConnection(getLoc, cap.getLoc)
        case _ =>
      }
    }

    def saveConnectionInfoNBT(compound: NBTTagCompound): Unit = {
      connections.indices.foreach { i =>
        compound.setBoolean(EnumFacing.VALUES(i).getName, connections(i))
      }

      val storage = new NBTTagCompound
      connectionStorage.indices.foreach { i =>
        storage.setTag(EnumFacing.VALUES(i).getName, connectionStorage(i).serializeNBT())
      }
      compound.setTag(ConduitImpl.STORAGE_KEY, storage)
    }

    def loadConnectionInfoNBT(compound: NBTTagCompound): Unit = {
      connections.indices.foreach { i =>
        connections(i) = compound.getBoolean(EnumFacing.VALUES(i).getName)
      }

      val storage = compound.getCompoundTag(ConduitImpl.STORAGE_KEY)
      connectionStorage.indices.foreach { i =>
        connectionStorage(i).deserializeNBT(storage.getCompoundTag(EnumFacing.VALUES(i).getName))
      }

      conduit.setRenderUpdate()
    }
  }

}

class TileConduit extends TileEntityBase {
  val conduit = new ConduitImpl(this)

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  def getStorage(facing: EnumFacing): IItemStorage = conduit.connectionStorage(facing.getIndex)

  def onNeighborChange(neighbor: BlockPos): Unit = {
    if (getWorld.isRemote) return

    val loc = getLoc
    EnumFacing.VALUES.withFilter(p =>
      p.getFrontOffsetX + loc.x == neighbor.getX && p.getFrontOffsetY + loc.y == neighbor.getY && p.getFrontOffsetZ + loc.z == neighbor.getZ
    ).foreach(checkFacingForConnection)
  }

  def onBlockPlaced(): Unit = {
    if (getWorld.isRemote) return

    EnumFacing.VALUES.foreach { f =>
      checkFacingForConnection(f)
    }
  }

  private def checkFacingForConnection(f: EnumFacing) = {
    val loc = getLoc
    val floc = Loc4(loc.x + f.getFrontOffsetX, loc.y + f.getFrontOffsetY, loc.z + f.getFrontOffsetZ, loc.dim)
    var addedConnection = false
    floc.getTileEntity(false).withFilter(_.hasCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).map(_.getCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).foreach { conduit =>
      val thisConduit = getCapability(Capabilities.TILE_CONDUIT, f)
      if (conduit.canAddConnection(f.getOpposite) && thisConduit.canAddConnection(f)) {
        conduit.addConnection(f.getOpposite)
        thisConduit.addConnection(f)
        addedConnection = true
      }
    }
    floc.getTileEntity(false).withFilter(_.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite)).map(_.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite)).foreach { handler =>
      getCapability(Capabilities.TILE_CONDUIT, f).addConnection(f)
      addedConnection = true
    }

    if (!addedConnection) {
      getCapability(Capabilities.TILE_CONDUIT, f).removeConnection(f)
    }
  }

  override def onBlockBreak(): Unit = {
    super.onBlockBreak()

    if (getWorld.isRemote) return

    conduit.connections.indices.withFilter(conduit.connections).map(EnumFacing.VALUES).foreach { f =>
      val loc = getLoc
      val floc = Loc4(loc.x + f.getFrontOffsetX, loc.y + f.getFrontOffsetY, loc.z + f.getFrontOffsetZ, loc.dim)
      floc.getTileEntity(false).withFilter(_.hasCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).map(_.getCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).foreach { c =>
        c.removeConnection(f.getOpposite)
      }
    }

    conduit.network.removeNode(conduit)
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (conduit.seek) {
      conduit.seek = false
      conduit.connections.indices.withFilter(conduit.connections).map(EnumFacing.VALUES).foreach { facing =>
        val loc = getLoc.getOffset(facing)
        loc.getTileEntity(false) match {
          case None =>
          case Some(i: TileEntity) if i.hasCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite) =>
            val cap = i.getCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite)
            if (cap.network != null)
              if (conduit.network == null)
                cap.network.addNode(conduit)
              else
                cap.network.addConnection(getLoc, cap.getLoc)
          //TODO MUCH BETTER WAY
          case Some(i: TileEntity) if i.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing.getOpposite) =>
            facing match {
              case EnumFacing.DOWN =>
                val con = new ItemConnection(getLoc, facing, new NBTTagCompound, 5000d, 1)
                con.setDirection(ConnectionDirection.OUTPUT)
                conduit.addConnection(facing, con)
              case EnumFacing.UP =>
                val con = new ItemConnection(getLoc, facing, new NBTTagCompound, 5000d, 1)
                con.setDirection(ConnectionDirection.INPUT)
                conduit.addConnection(facing, con)
              case _ =>
            }
          case _ =>
        }
      }

      if (conduit.network == null) {
        conduit.network = new LogisticsNetwork
        conduit.network.addNode(conduit)
        conduit.network.register()
      }
    }
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    val con = new NBTTagCompound
    conduit.saveConnectionInfoNBT(con)
    compound.setTag(TileConduit.CONDUIT_KEY, con)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    conduit.loadConnectionInfoNBT(compound.getCompoundTag(TileConduit.CONDUIT_KEY))
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    conduit.loadConnectionInfoNBT(par1nbtTagCompound.getCompoundTag(TileConduit.CONDUIT_KEY))
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    val con = new NBTTagCompound
    conduit.saveConnectionInfoNBT(con)
    par1nbtTagCompound.setTag(TileConduit.CONDUIT_KEY, con)
    par1nbtTagCompound
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_CONDUIT)
      conduit.asInstanceOf[T]
    else if (capability == Capabilities.TILE_LOGISTICS_NODE)
      conduit.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_CONDUIT) true
    else if (capability == Capabilities.TILE_LOGISTICS_NODE) true
    else super.hasCapability(capability, facing)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    if (getWorld.isRemote) return

    conduit.network.removeNode(conduit)
  }

  override def onChunkUnload(): Unit = {
    super.onChunkUnload()
    if (getWorld.isRemote) return

    conduit.network.removeNode(conduit)
  }
}
