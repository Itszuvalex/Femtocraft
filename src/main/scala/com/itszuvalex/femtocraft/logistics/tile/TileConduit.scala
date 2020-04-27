package com.itszuvalex.femtocraft.logistics.tile

import java.util

import com.itszuvalex.femtocraft.api.ManagerModules
import com.itszuvalex.femtocraft.api.logistics._
import com.itszuvalex.femtocraft.logistics.tile.TileConduit.ModuleConduit
import com.itszuvalex.femtocraft.power.ModuleColorNeighborAverage
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.{IModule, Loc4}
import com.itszuvalex.itszulib.api.storage.{IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers._
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.modules.{ModuleGui, ModuleNetworkedWire}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Chris on 2/16/2017.
 */
object TileConduit {
  lazy val connectionModules: ArrayBuffer[IModule[_]] = mutable.ArrayBuffer[IModule[_]](
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

  class ModuleConduit(val conduit: TileConduit) extends ModuleNetworkedWire[ILogisticsNetworkNode, LogisticsNetwork](conduit, () => new LogisticsNetwork)
    with ILogisticsNetworkNode {
    val connectionStorage = Array(new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4), new ConduitStorage(4))
    var seek              = true

    override def getConnections[T](facing: EnumFacing): util.Collection[IConnection[T]] = {
      if (facing == null) return Set[IConnection[T]]()

      connectionStorage(facing.getIndex).withFilter(f => !f.isEmpty && f.hasModule(ManagerModules.ITEM_CONNECTION_PROVIDER, facing)).map(_.getModule(ManagerModules.ITEM_CONNECTION_PROVIDER, facing)).flatMap { f =>
        f.getConnections[T](getLoc, facing)
      }
    }

    override protected def shouldConnect(a: ITileEntity, f: EnumFacing): Boolean = connectionModules.exists(a.hasModule(_, f))

    override protected def getNetworkNodeFromITE(ite: ITileEntity, f: EnumFacing): Option[ILogisticsNetworkNode] = ite.moduleOption(module, f)

    override def module: IModule[ILogisticsNetworkNode] = ManagerModules.TILE_LOGISTICS_NODE

    override def faceToModuleMapper(tile: ITileEntity): EnumFacing => Option[ILogisticsNetworkNode] = _ => Some(this)

    override def getLoc: Loc4 = conduit.getLoc

    override def writeWorldNBT(tag: NBTTagCompound): Unit = {
      super.writeWorldNBT(tag)
      val storage = new NBTTagCompound
      connectionStorage.indices.foreach { i =>
        storage.setTag(EnumFacing.VALUES(i).getName, connectionStorage(i).serializeNBT())
      }
      tag.setTag(ModuleConduit.STORAGE_KEY, storage)
    }

    override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
      super.readWorldNBT(tagCompound)
      val storage = tagCompound.getCompoundTag(ModuleConduit.STORAGE_KEY)
      connectionStorage.indices.foreach { i =>
        connectionStorage(i).deserializeNBT(storage.getCompoundTag(EnumFacing.VALUES(i).getName))
      }
    }
  }

  object ModuleConduit {
    val STORAGE_KEY = "storage"
  }

}

class TileConduit extends TileEntityCoreTickable {
  val conduit = new ModuleConduit(this)
  var color   = new ModuleColorNeighborAverage(conduit.isConnected)

  addTileEntityModule(new ModuleGui(Femtocraft, GuiIDs.TileConduitID _))
  addTileEntityModule(conduit)
  addTileEntityModuleTickable(color)

  def getStorage(facing: EnumFacing): IItemStorage = conduit.connectionStorage(facing.getIndex)

  override def getRenderBoundingBox: AxisAlignedBB = new AxisAlignedBB(getPos, getPos.add(1, 1, 1))
}
