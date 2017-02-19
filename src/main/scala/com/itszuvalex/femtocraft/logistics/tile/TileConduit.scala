package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.logistics.IConduit
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler

/**
  * Created by Chris on 2/16/2017.
  */
object TileConduit {
  val CONNECTIONS_KEY = "connections"
}

class TileConduit extends TileEntityBase {
  val connections = new Array[Boolean](6)

  override def hasDescription: Boolean = true

  override def getMod: AnyRef = Femtocraft

  def onBlockPlaced(): Unit = {
    EnumFacing.VALUES.foreach { f =>
      val loc = getLoc
      val floc = Loc4(loc.x + f.getFrontOffsetX, loc.y + f.getFrontOffsetY, loc.z + f.getFrontOffsetZ, loc.dim)
      floc.getTileEntity(false).withFilter(_.hasCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).map(_.getCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).foreach { conduit =>
        val thisConduit = getCapability(Capabilities.TILE_CONDUIT, f)
        if (conduit.canAddConnection(f.getOpposite) && thisConduit.canAddConnection(f)) {
          conduit.addConnection(f.getOpposite)
          thisConduit.addConnection(f)
        }
      }
      floc.getTileEntity(false).withFilter(_.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite)).map(_.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite)).foreach { handler =>
        getCapability(Capabilities.TILE_CONDUIT, f).addConnection(f)
      }
    }
  }


  override def onBlockBreak(): Unit = {
    super.onBlockBreak()
    connections.indices.filter(connections(_)).map(EnumFacing.getFront).foreach { f =>
      val loc = getLoc
      val floc = Loc4(loc.x + f.getFrontOffsetX, loc.y + f.getFrontOffsetY, loc.z + f.getFrontOffsetZ, loc.dim)
      floc.getTileEntity(false).withFilter(_.hasCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).map(_.getCapability(Capabilities.TILE_CONDUIT, f.getOpposite)).foreach { conduit =>
        conduit.removeConnection(f.getOpposite)
      }
    }
  }

  def saveConnectionInfoNBT(compound: NBTTagCompound): Unit = {
    connections.indices.foreach { i =>
      compound.setBoolean(i.toString, connections(i))
    }
  }

  def loadConnectionInfoNBT(compound: NBTTagCompound): Unit = {
    connections.indices.foreach { i =>
      connections(i) = compound.getBoolean(i.toString)
    }
    setRenderUpdate()
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    val con = new NBTTagCompound
    saveConnectionInfoNBT(con)
    compound.setTag(TileConduit.CONNECTIONS_KEY, con)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadConnectionInfoNBT(compound.getCompoundTag(TileConduit.CONNECTIONS_KEY))
  }


  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    loadConnectionInfoNBT(par1nbtTagCompound.getCompoundTag(TileConduit.CONNECTIONS_KEY))
  }

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(par1nbtTagCompound)
    val con = new NBTTagCompound
    saveConnectionInfoNBT(con)
    par1nbtTagCompound.setTag(TileConduit.CONNECTIONS_KEY, con)
    par1nbtTagCompound
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.TILE_CONDUIT)
      new IConduit {
        override def canAddConnection(facing: EnumFacing): Boolean = true

        override def addConnection(facing: EnumFacing): Unit = {
          connections(facing.getIndex) = true
          setModified()
          setUpdate()
        }

        override def removeConnection(facing: EnumFacing): Unit = {
          connections(facing.getIndex) = false
          setModified()
          setUpdate()
        }

        override def isConnected(facing: EnumFacing): Boolean = connections(facing.getIndex)
      }.asInstanceOf[T]
    else super.getCapability(capability, facing)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == Capabilities.TILE_CONDUIT) true
    else super.hasCapability(capability, facing)
  }
}
