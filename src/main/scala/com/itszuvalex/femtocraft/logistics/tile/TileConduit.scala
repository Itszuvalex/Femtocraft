package com.itszuvalex.femtocraft.logistics.tile

import java.util

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics._
import com.itszuvalex.femtocraft.logistics.tile.TileConduit.ConduitImpl
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.CapabilityFluidHandler
import net.minecraftforge.items.CapabilityItemHandler

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 2/16/2017.
  */
object TileConduit {
  lazy val connectionCapabilities: ArrayBuffer[Capability[_]] = mutable.ArrayBuffer[Capability[_]](
    Capabilities.TILE_CONDUIT,
    Capabilities.TILE_LOGISTICS_NODE,
    CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
    Capabilities.TILE_NANITE_STORAGE_TANK,
    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY
  )
  val CONDUIT_KEY = "conduit"

  def addConnectionCapability(cap: Capability[_]): Unit = {
    connectionCapabilities += cap
  }

  class ConduitStorage(size: Int) extends ItemStorageArray(size) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = stack.hasCapability(Capabilities.ITEM_CONNECTION_PROVIDER, null)

    override def maxStackSize(i: Int): Int = 1
  }

  class ConduitImpl(val conduit: TileConduit) extends IConduit with ILogisticsNetworkNode {
    val connections       = new Array[Boolean](6)
    val blocked           = new Array[Boolean](6)
    val connectionStorage = Array(new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4))
    var seek              = true

    override def getConnections[T](facing: EnumFacing): util.Collection[IConnection[T]] = {
      if (facing == null) return Set[IConnection[T]]()

      connectionStorage(facing.getIndex).withFilter(f => !f.isEmpty && f.hasCapability(Capabilities.ITEM_CONNECTION_PROVIDER, facing)).map(_.getCapability(Capabilities.ITEM_CONNECTION_PROVIDER, facing)).flatMap { f =>
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

      getLoc.getOffset(facing).getTileEntity(false).foreach { t =>
        if (!t.hasCapability(Capabilities.TILE_CONDUIT, facing.getOpposite)) return
        val cap = t.getCapability(Capabilities.TILE_CONDUIT, facing.getOpposite)
        if (!cap.canAddConnection(facing.getOpposite)) return

        cap.addConnection(facing.getOpposite)
      }
    }

    override def getLoc: Loc4 = conduit.getLoc

    override def canAddConnection(facing: EnumFacing): Boolean = !blocked(facing.getIndex)

    override def addConnection(facing: EnumFacing): Unit = {
      if (connections(facing.getIndex)) return
      if (!canAddConnection(facing)) return

      getLoc.getOffset(facing).getTileEntity(false)
        .withFilter(_.hasCapability(Capabilities.TILE_CONDUIT, facing.getOpposite))
        .map(_.getCapability(Capabilities.TILE_CONDUIT, facing.getOpposite))
        .withFilter(!_.canAddConnection(facing.getOpposite))
        .foreach(a => return)

      connections(facing.getIndex) = true
      connectToNetwork(getLoc.getOffset(facing), facing.getOpposite)
      conduit.setModified()
      conduit.setUpdate()
    }

    def connectToNetwork(loc: Loc4, facing: EnumFacing): Unit = {
      loc.getTileEntity(false) match {
        case Some(i: TileEntity) if i.hasCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite) =>
          val cap = i.getCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite)
          val network = if (cap.getNetwork != null) cap.getNetwork else getNetwork
          network.addConnection(getLoc, cap.getLoc)
        case _ =>
      }
    }

    override def removeConnection(facing: EnumFacing): Unit = {
      if (!connections(facing.getIndex)) return

      connections(facing.getIndex) = false
      disconnectFromNetwork(getLoc.getOffset(facing), facing.getOpposite)
      conduit.setModified()
      conduit.setUpdate()
    }

    def disconnectFromNetwork(loc: Loc4, facing: EnumFacing): Unit = {
      loc.getTileEntity(false) match {
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

  object ConduitImpl {
    val CONNECTION_KEY = "connections"
    val BLOCKED_KEY    = "blocked"
    val STORAGE_KEY    = "storage"
  }

}

class TileConduit extends TileEntityBase {
  val conduit = new ConduitImpl(this)
  var color   = Color(0, 0, 0, 0)

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileConduitID

  def getStorage(facing: EnumFacing): IItemStorage = conduit.connectionStorage(facing.getIndex)

  def onNeighborChange(neighbor: BlockPos): Unit = {
    if (getWorld.isRemote) return

    val loc = getLoc
    val nloc = new Loc4(getWorld, neighbor)
    EnumFacing.VALUES.withFilter(getLoc.getOffset(_) == nloc).foreach(checkFacingForConnection)
  }

  private def checkFacingForConnection(f: EnumFacing) = {
    val loc = getLoc
    val floc = getLoc.getOffset(f)
    if (conduit.isConnected(f)) {
      floc.getTileEntity(false) match {
        case Some(a: TileEntity) if TileConduit.connectionCapabilities.exists(a.hasCapability(_, f.getOpposite)) =>
        case _ => conduit.removeConnection(f)
      }
    }
    else {
      floc.getTileEntity(false) match {
        case Some(a: TileEntity) if TileConduit.connectionCapabilities.exists(a.hasCapability(_, f.getOpposite)) =>
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

  override def onBlockBreak(): Unit = {
    super.onBlockBreak()

    if (getWorld.isRemote) return

    conduit.connections.indices.withFilter(conduit.connections).map(EnumFacing.VALUES).foreach { f =>
      val loc = getLoc
      val floc = loc.getOffset(f)
      floc.getTileEntity(false).withFilter(_.hasCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).map(_.getCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).foreach { c =>
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
        loc.getTileEntity(false) match {
          case Some(i: TileEntity) if i.hasCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite) =>
            val cap = i.getCapability(Capabilities.TILE_LOGISTICS_NODE, facing.getOpposite)
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

  override def update(): Unit = {
    super.update()
    var red: Int = 0
    var green: Int = 0
    var blue: Int = 0
    var numBlocks = 0
    EnumFacing.VALUES.map(getLoc.getOffset(_)).flatMap(_.getTileEntity(false))
      .withFilter(_.hasCapability(ItszuLibCapabilities.COLORABLE, null)).map(_.getCapability(ItszuLibCapabilities.COLORABLE, null)).foreach { c =>
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

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_CONDUIT)
      conduit.asInstanceOf[T]
    else if (capability == Capabilities.TILE_LOGISTICS_NODE)
      conduit.asInstanceOf[T]
    else if (capability == ItszuLibCapabilities.COLORABLE)
      color.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    capability == Capabilities.TILE_CONDUIT ||
      capability == Capabilities.TILE_LOGISTICS_NODE ||
      capability == ItszuLibCapabilities.COLORABLE ||
      super.hasCapability(capability, facing)
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
