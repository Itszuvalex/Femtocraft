package com.itszuvalex.femtocraft.api.power

import com.itszuvalex.femtocraft.api.{Capabilities, ManagerModules}
import com.itszuvalex.femtocraft.power.PowerManager
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityCore
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

import scala.collection.{Set, mutable}

/**
  * Created by Chris on 1/1/2017.
  */
object PowerNetworkNodeDelegate {
  val LEAF_NODE_TAG = "Leaf"
}

class PowerNetworkNodeDelegate(tileEntity: TileEntityCore,
                               radius: => Float,
                               transfer: => Double,
                               renders: => Boolean
                              ) extends IPowerNetworkNode with INBTSerializable[NBTTagCompound] {
  val leafNodeLocs: mutable.HashSet[Loc4]      = new mutable.HashSet[Loc4]()
  var renderLocs  : scala.collection.Set[Loc4] = Set()

  override def addLeafNode(node: IPowerLeafNode): Unit = {
    leafNodeLocs += node.getStorageLoc
    tileEntity.setUpdate()
    tileEntity.markDirtyForSave()
  }

  override def removeLeafNode(node: IPowerLeafNode): Unit = {
    leafNodeLocs -= node.getStorageLoc
    tileEntity.setUpdate()
    tileEntity.markDirtyForSave()
    PowerManager.instance.refreshLeafsOnMain(this)
  }

  override def leafTransferRate: Double = transfer

  override def leafNodes(force: Boolean): Set[IPowerLeafNode] = leafNodeLocs.flatMap(_.getITileEntity(force)).withFilter(_.hasModule(ManagerModules.TILE_POWER_LEAF_NODE, null)).map(_.getModule(ManagerModules.TILE_POWER_LEAF_NODE, null))

  override def storageNodes(force: Boolean): Set[IPowerStorageNode] = {
    val set = if (tileEntity.hasCapability(Capabilities.TILE_POWER_STORAGE_NODE, null))
      Set(tileEntity.getCapability(Capabilities.TILE_POWER_STORAGE_NODE, null))
    else Set()

    set ++ leafNodeLocs.flatMap(_.getITileEntity(force)).withFilter(_.hasModule(ManagerModules.TILE_POWER_STORAGE_NODE, null)).map(_.getModule(ManagerModules.TILE_POWER_STORAGE_NODE, null))
  }

  override def connectionRadius: Float = radius

  override def rendersConnections: Boolean = renders

  override def setRenderLocations(set: scala.collection.Set[Loc4]): Unit = {
    renderLocs = set
    tileEntity.setUpdate()
  }

  override def renderLocations: scala.collection.Set[Loc4] = renderLocs

  override def getLoc: Loc4 = tileEntity.getLoc

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    leafNodeLocs.clear()
    val locs = nbt.NBTList(PowerNetworkNodeDelegate.LEAF_NODE_TAG)
    if (locs != null) {
      leafNodeLocs ++= locs.map(Loc4(_))
    }
    tileEntity.setRenderUpdate()
  }

  override def serializeNBT(): NBTTagCompound = {
    NBTCompound(
      PowerNetworkNodeDelegate.LEAF_NODE_TAG -> NBTList(leafNodeLocs.map(_.serializeNBT()))
      )
  }
}
