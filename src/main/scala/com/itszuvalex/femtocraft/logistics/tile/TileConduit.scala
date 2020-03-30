package com.itszuvalex.femtocraft.logistics.tile

import java.util

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.logistics._
import com.itszuvalex.femtocraft.logistics.tile.TileConduit.ConduitImpl
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers._
import com.itszuvalex.itszulib.core.modules.ModuleGui
import com.itszuvalex.itszulib.core.{TileEntityCoreTickable, TileEntityModule}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 2/16/2017.
  */
object TileConduit {
  lazy val connectionModules: ArrayBuffer[IModule[_]] = mutable.ArrayBuffer[IModule[_]](
    ManagerModules.TILE_CONDUIT,
    ManagerModules.TILE_LOGISTICS_NODE,
    ItszuLibModules.ITEM_STORAGE,
    ItszuLibModules.ITEM_MINECRAFT_INVENTORY,
    ManagerModules.TILE_NANITE_STORAGE_TANK,
    ItszuLibModules.FLUID_STORAGE,
    ItszuLibModules.FLUID_MINECRAFT_HANDLER
    )
  val CONDUIT_KEY = "conduit"

  def addConnectionModule(mod: IModule[_]): Unit = {
    connectionModules += mod
  }

  class ConduitStorage(size: Int) extends ItemStorageArray(size) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = stack.hasModule(ManagerModules.ITEM_CONNECTION_PROVIDER, null)

    override def maxStackSize(i: Int): Int = 1
  }

  class ConduitImpl(val conduit: TileConduit) extends IConduit with ILogisticsNetworkNode {
    val connections       = new Array[Boolean](6)
    val blocked           = new Array[Boolean](6)
    val connectionStorage = Array(new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4))
    var seek              = true

    override def getConnections[T](facing: EnumFacing): util.Collection[IConnection[T]] = {
      if (facing == null) return Set[IConnection[T]]()

      connectionStorage(facing.getIndex).withFilter(f => !f.isEmpty && f.hasModule(ManagerModules.ITEM_CONNECTION_PROVIDER, facing)).map(_.getModule(ManagerModules.ITEM_CONNECTION_PROVIDER, facing)).flatMap { f =>
        f.getConnections[T](getLoc, facing)
      }
    }

    override def canConnect(loc: Loc4): Boolean = {
      if (!super.canConnect(loc)) return false

      EnumFacing.VALUES.exists { f =>
        val l = getLoc.getOffset(f)
        (l == loc) && canAddConnection(f)
      }
    }

    override def isConnected(facing: EnumFacing): Boolean = !blocked(facing.getIndex) && connections(facing.getIndex)

    def addConnectionInternal(facing: EnumFacing): Unit = {
      if (connections(facing.getIndex)) return
      if (!canAddConnection(facing)) return

      addConnection(facing)

      if (!connections(facing.getIndex)) return

      getLoc.getOffset(facing).getITileEntity(false).foreach { t =>
        if (!t.hasModule(ManagerModules.TILE_CONDUIT, facing.getOpposite)) return
        val cap = t.getModule(ManagerModules.TILE_CONDUIT, facing.getOpposite)
        if (!cap.canAddConnection(facing.getOpposite)) return

        cap.addConnection(facing.getOpposite)
      }
    }

    override def getLoc: Loc4 = conduit.getLoc

    override def canAddConnection(facing: EnumFacing): Boolean = !blocked(facing.getIndex)

    override def addConnection(facing: EnumFacing): Unit = {
      if (connections(facing.getIndex)) return
      if (!canAddConnection(facing)) return

      getLoc.getOffset(facing).getITileEntity(false)
            .withFilter(_.hasModule(ManagerModules.TILE_CONDUIT, facing.getOpposite))
            .map(_.getModule(ManagerModules.TILE_CONDUIT, facing.getOpposite))
            .withFilter(!_.canAddConnection(facing.getOpposite))
            .foreach(a => return)

      connections(facing.getIndex) = true
      connectToNetwork(getLoc.getOffset(facing), facing.getOpposite)
      conduit.markDirtyForSave()
      conduit.setUpdate()
    }

    def connectToNetwork(loc: Loc4, facing: EnumFacing): Unit = {
      loc.getITileEntity(false) match {
        case Some(i: ITileEntity) if i.hasModule(ManagerModules.TILE_LOGISTICS_NODE, facing.getOpposite) =>
          val cap     = i.getModule(ManagerModules.TILE_LOGISTICS_NODE, facing.getOpposite)
          val network = if (cap.getNetwork != null) cap.getNetwork else getNetwork
          network.addConnection(getLoc, cap.getLoc)
        case _ =>
      }
    }

    override def removeConnection(facing: EnumFacing): Unit = {
      if (!connections(facing.getIndex)) return

      connections(facing.getIndex) = false
      disconnectFromNetwork(getLoc.getOffset(facing), facing.getOpposite)
      conduit.markDirtyForSave()
      conduit.setUpdate()
    }

    def disconnectFromNetwork(loc: Loc4, facing: EnumFacing): Unit = {
      loc.getITileEntity(false) match {
        case Some(i: ITileEntity) if i.hasModule(ManagerModules.TILE_LOGISTICS_NODE, facing.getOpposite) =>
          val cap = i.getModule(ManagerModules.TILE_LOGISTICS_NODE, facing.getOpposite)
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

  object ConduitImpl {
    val CONNECTION_KEY = "connections"
    val BLOCKED_KEY    = "blocked"
    val STORAGE_KEY    = "storage"
  }

}

class TileConduit extends TileEntityCoreTickable {
  val conduit = new ConduitImpl(this)
  var color   = Color(0, 0, 0, 0)

  addTileEntityModule(new ModuleGui(Femtocraft, GuiIDs.TileConduitID _))
  addTileEntityModule(new TileEntityModule[IConduit] {
    override def module: IModule[IConduit] = ManagerModules.TILE_CONDUIT

    override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IConduit] = _ => Some(conduit)
  })
  addTileEntityModule(new TileEntityModule[ILogisticsNetworkNode] {
    override def module: IModule[ILogisticsNetworkNode] = ManagerModules.TILE_LOGISTICS_NODE

    override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[ILogisticsNetworkNode] = _ => Some(conduit)
  })
  addTileEntityModule(new TileEntityModule[Color] {
    override def module: IModule[Color] = ItszuLibModules.COLORABLE

    override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[Color] = _ => Some(color)
  })

  override def hasDescription: Boolean = true

  def getStorage(facing: EnumFacing): IItemStorage = conduit.connectionStorage(facing.getIndex)

  def onNeighborChange(neighbor: BlockPos): Unit = {
    if (getWorld.isRemote) return

    val loc  = getLoc
    val nloc = new Loc4(getWorld, neighbor)
    EnumFacing.VALUES.withFilter(loc.getOffset(_) == nloc).foreach(checkFacingForConnection)
  }

  private def checkFacingForConnection(f: EnumFacing): Unit = {
    val loc  = getLoc
    val floc = loc.getOffset(f)
    if (conduit.isConnected(f)) {
      floc.getITileEntity(false) match {
        case Some(a: ITileEntity) if TileConduit.connectionModules.exists(a.hasModule(_, f.getOpposite)) =>
        case _ => conduit.removeConnection(f)
      }
    }
    else {
      floc.getITileEntity(false) match {
        case Some(a: ITileEntity) if TileConduit.connectionModules.exists(a.hasModule(_, f.getOpposite)) =>
          conduit.addConnectionInternal(f)
        case _ =>
      }
    }
  }

  def onBlockPlaced(): Unit = {
    if (getWorld.isRemote) return

    val network = new LogisticsNetwork
    network.addNode(conduit)
    network.register()

    EnumFacing.VALUES.foreach(checkFacingForConnection)
  }

  override def onBlockBreak(state: IBlockState): Unit = {
    super.onBlockBreak(state)

    if (getWorld.isRemote) return

    conduit.connections.indices.withFilter(conduit.connections).map(EnumFacing.VALUES).foreach { f =>
      val loc  = getLoc
      val floc = loc.getOffset(f)
      floc.getITileEntity(false).withFilter(_.hasModule(ManagerModules.TILE_CONDUIT, f.getOpposite)).map(_.getModule(ManagerModules.TILE_CONDUIT, f.getOpposite)).foreach { c =>
        c.removeConnection(f.getOpposite)
      }
    }

    //    conduit.network.removeNode(conduit)
  }

  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (conduit.seek) {
      conduit.seek = false
      conduit.connections.indices.withFilter(conduit.connections).map(EnumFacing.VALUES).foreach { facing =>
        val loc = getLoc.getOffset(facing)
        loc.getITileEntity(false) match {
          case Some(i: ITileEntity) if i.hasModule(ManagerModules.TILE_LOGISTICS_NODE, facing.getOpposite) =>
            val cap = i.getModule(ManagerModules.TILE_LOGISTICS_NODE, facing.getOpposite)
            if (cap.network != null)
              if (conduit.network == null)
                cap.network.addNode(conduit)
              else
                cap.network.addConnection(getLoc, cap.getLoc)
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

  override def getRenderBoundingBox: AxisAlignedBB = new AxisAlignedBB(getPos, getPos.add(1, 1, 1))

  override def clientUpdate(): Unit = {
    super.clientUpdate()
    var red  : Int = 0
    var green: Int = 0
    var blue : Int = 0
    var numBlocks  = 0
    EnumFacing.VALUES.map(getLoc.getOffset(_)).flatMap(_.getITileEntity(false))
              .withFilter(_.hasModule(ItszuLibModules.COLORABLE, null)).map(_.getModule(ItszuLibModules.COLORABLE, null)).foreach { c =>
      numBlocks += 1
      red += c.red.toInt & 255
      green += c.green.toInt & 255
      blue += c.blue.toInt & 255
    }
    color = if (numBlocks > 0) Color(255.toByte,
                                     ((red / numBlocks) & 255).toByte,
                                     ((green / numBlocks) & 255).toByte,
                                     ((blue / numBlocks) & 255).toByte)
    else Color(0, 0, 0, 0)
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
