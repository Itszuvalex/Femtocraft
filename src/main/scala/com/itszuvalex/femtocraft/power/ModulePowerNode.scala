package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.power.{IPowerLeafNode, IPowerNetworkNode, IPowerStorageNode}
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.TileEntityModule
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing

import scala.collection.{Set, mutable}

object ModulePowerNode {
  val LEAF_NODE_TAG   = "Leaf"
  val RENDER_LOCS_TAG = "Render"
}

class ModulePowerNode(val tile: ITileEntity, var conRad: () => Float, var tranRate: () => Double, var doRender: () => Boolean) extends TileEntityModule[IPowerNetworkNode] with IPowerNetworkNode {
  val leafNodeLocs: mutable.HashSet[Loc4]      = new mutable.HashSet[Loc4]()
  var renderLocs  : scala.collection.Set[Loc4] = Set()

  override def module: IModule[IPowerNetworkNode] = ManagerModules.TILE_POWER_NODE

  override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[IPowerNetworkNode] = _ => Some(this)

  override def connectionRadius: Float = conRad()

  override def leafNodes(force: Boolean): collection.Set[IPowerLeafNode] = {
    leafNodeLocs.flatMap(_.getITileEntity(force)).withFilter(_.hasModule(ManagerModules.TILE_POWER_LEAF_NODE, null)).map(_.getModule(ManagerModules.TILE_POWER_LEAF_NODE, null))
  }

  override def addLeafNode(node: IPowerLeafNode): Unit = {
    leafNodeLocs += node.getStorageLoc
    tile.setUpdate()
    tile.markDirtyForSave()
  }

  override def removeLeafNode(node: IPowerLeafNode): Unit = {
    leafNodeLocs -= node.getStorageLoc
    PowerManager.instance.refreshLeafsOnMain(this)
    tile.setUpdate()
    tile.markDirtyForSave()
  }

  override def storageNodes(force: Boolean): collection.Set[IPowerStorageNode] = {
    val set = if (tile.hasModule(ManagerModules.TILE_POWER_STORAGE_NODE, null))
      Set(tile.getModule(ManagerModules.TILE_POWER_STORAGE_NODE, null))
    else Set()

    set ++ leafNodeLocs.flatMap(_.getITileEntity(force)).withFilter(_.hasModule(ManagerModules.TILE_POWER_STORAGE_NODE, null)).map(_.getModule(ManagerModules.TILE_POWER_STORAGE_NODE, null))
  }

  override def rendersConnections: Boolean = doRender()

  override def setRenderLocations(set: collection.Set[Loc4]): Unit = {
    renderLocs = set
    tile.setUpdate()
  }

  override def renderLocations: collection.Set[Loc4] = renderLocs

  override def leafTransferRate: Double = tranRate()

  override def getLoc: Loc4 = new Loc4(tile)


  override def invalidate(tile: ITileEntity): Unit = {
    if (!tile.getIWorld.isRemote) PowerManager.instance.removeNode(this)

  }

  override def onChunkUnload(tile: ITileEntity): Unit = {
    if (!tile.getIWorld.isRemote) PowerManager.instance.removeNode(this)
  }

  override def onBlockBreak(core: ITileEntity, state: IBlockState): Unit = {
    PowerManager.instance.removeNode(this)
    PowerManager.instance.onNodeBroken(this)
  }


  override def onLoad(tile: ITileEntity): Unit = {
    if (network == null && !tile.isInvalid) {
      PowerManager.instance.addNode(this)
    }
  }

  override def hasWorldNBT: Boolean = true

  override def writeWorldNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(ModulePowerNode.LEAF_NODE_TAG, NBTList(leafNodeLocs.map(_.serializeNBT())))
  }

  override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
    val locs = tagCompound.NBTList(ModulePowerNode.LEAF_NODE_TAG)
    if (locs != null) {
      leafNodeLocs ++= locs.map(Loc4(_))
    }
    tile.setUpdate()
    tile.setRenderUpdate()
  }

  override def hasDescriptionNBT: Boolean = true

  override def writeDescriptionNBT(tag: NBTTagCompound): Unit = {
    tag.setTag(ModulePowerNode.LEAF_NODE_TAG, NBTList(leafNodeLocs.map(_.serializeNBT())))
    tag.setTag(ModulePowerNode.RENDER_LOCS_TAG, NBTList(renderLocs.map(NBTCompound)))
  }

  override def readDescriptionNBT(tag: NBTTagCompound): Unit = {
    renderLocs = tag.NBTList(ModulePowerNode.RENDER_LOCS_TAG).map(Loc4(_)).toSet
    leafNodeLocs.clear()
    val locs = tag.NBTList(ModulePowerNode.LEAF_NODE_TAG)
    if (locs != null) {
      leafNodeLocs ++= locs.map(Loc4(_))
    }
    tile.setRenderUpdate()
  }
}
