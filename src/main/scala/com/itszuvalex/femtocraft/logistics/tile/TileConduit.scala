package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics.IConduit
import com.itszuvalex.femtocraft.logistics.tile.TileConduit.ConduitImpl
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler

/**
  * Created by Chris on 2/16/2017.
  */
object TileConduit {
  val CONDUIT_KEY = "connections"

  class ConduitImpl(val conduit: TileConduit) extends IConduit {
    val connections = new Array[Boolean](6)

    override def isConnected(facing: EnumFacing): Boolean = connections(facing.getIndex)

    override def canAddConnection(facing: EnumFacing): Boolean = true

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

    def saveConnectionInfoNBT(compound: NBTTagCompound): Unit = {
      connections.indices.foreach { i =>
        compound.setBoolean(EnumFacing.VALUES(i).getName, connections(i))
      }
    }

    def loadConnectionInfoNBT(compound: NBTTagCompound): Unit = {
      connections.indices.foreach { i =>
        connections(i) = compound.getBoolean(EnumFacing.VALUES(i).getName)
      }
      conduit.setRenderUpdate()
    }
  }
}

class TileConduit extends TileEntityBase {
  val conduit = new ConduitImpl(this)

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

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

    conduit.connections.indices.filter(conduit.connections(_)).map(EnumFacing.getFront).foreach { f =>
      val loc = getLoc
      val floc = Loc4(loc.x + f.getFrontOffsetX, loc.y + f.getFrontOffsetY, loc.z + f.getFrontOffsetZ, loc.dim)
      floc.getTileEntity(false).withFilter(_.hasCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).map(_.getCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).foreach { conduit =>
        conduit.removeConnection(f.getOpposite)
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
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_CONDUIT) true
    else super.hasCapability(capability, facing)
  }
}
