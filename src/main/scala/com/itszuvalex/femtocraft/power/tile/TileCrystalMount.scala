package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.api.{Capabilities, ManagerModules}
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount.CrystalMountModule
import com.itszuvalex.femtocraft.power.{ModuleColorableFromICrystal, ModuleWirelessPowerNode, ModulePowerStorage, ModuleWirelessPowerStorageNode}
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage.{DynamicIBattery, IBattery, IItemStorage, ItemStorageArray}
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, ITileEntity}
import com.itszuvalex.itszulib.core.modules.{ModuleGui, ModuleIItemHandlerConverter, ModuleIItemStorage}
import com.itszuvalex.itszulib.core.{TileEntityCoreTickable, TileEntityInternalModuleTickable}
import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.AxisAlignedBB

/**
  * Created by Christopher Harris (Itszuvalex) on 8/27/15.
  */
object TileCrystalMount {
  val MODULE: IModule[CrystalMountModule] = Module.registerModule("CrystalMountModule", null)
  val CRYSTAL_KEY                         = "Crystal"
  val PEDESTAL_RANGE                      = 8f

  class CrystalMountModule(val tile: TileCrystalMount, val storage: IItemStorage) extends TileEntityInternalModuleTickable[CrystalMountModule] {
    private var lastCrystal: IItemStack = IItemStack.Empty

    override def module: IModule[CrystalMountModule] = MODULE

    override def serverUpdate(tile: ITileEntity): Unit = {
      storage.head.capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).foreach(_.onTick())
      val stack = storage.head
      if (stack != lastCrystal) {
        tile.setUpdate()
      }
      lastCrystal = stack
    }

    override def hasDescriptionNBT: Boolean = true

    override def writeDescriptionNBT(tag: NBTTagCompound): Unit = {
      tag.setTag(CRYSTAL_KEY, storage.serializeNBT())
    }

    override def readDescriptionNBT(tag: NBTTagCompound): Unit = {
      storage.deserializeNBT(tag.getCompoundTag(CRYSTAL_KEY))
      tile.setUpdate()
    }
  }

}

class TileCrystalMount extends TileEntityCoreTickable {
  val storage: IItemStorage = new ItemStorageArray(1) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack == null || stack.isEmpty || stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    }
  }
  val battery: IBattery     = new DynamicIBattery(() => storage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null).map(_.battery).getOrElse(IBattery.Empty))
  val powerNetworkNode      = new ModuleWirelessPowerNode(this, () => TileCrystalMount.PEDESTAL_RANGE, () => powerStorageTransferRate, () => true)
  val internal              = new CrystalMountModule(this, storage)

  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModulePowerStorage(battery) {
    override def hasWorldNBT: Boolean = false // We don't need to save the battery since it's part of the itemstack and saved with ItemStorage
  })
  addTileEntityModule(new ModuleWirelessPowerStorageNode(this, battery, PowerStorageNodeType.STORAGE, () => powerStorageTransferRate))
  addTileEntityModule(powerNetworkNode)
  addTileEntityModule(new ModuleColorableFromICrystal(() => storage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null)))
  addTileEntityModule(new ModuleGui(Femtocraft, GuiIDs.TileCrystalMountGuiID _))
  addTileEntityModuleTickable(internal)

  def powerStorageTransferRate: Double = storage.head.moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null).map(_.getTransferRate())
                                                .getOrElse(0d)

  override def shouldRenderInPass(pass: Int): Boolean = pass == 0 || pass == 1

  override def getRenderBoundingBox: AxisAlignedBB = {
    val center = Vector3(getPos.getX + .5f, getPos.getY + .5f, getPos.getZ + .5f)
    new AxisAlignedBB(center.x - powerNetworkNode.connectionRadius,
                      center.y - powerNetworkNode.connectionRadius,
                      center.z - powerNetworkNode.connectionRadius,
                      center.x + powerNetworkNode.connectionRadius,
                      center.y + powerNetworkNode.connectionRadius,
                      center.z + powerNetworkNode.connectionRadius)
  }
}
