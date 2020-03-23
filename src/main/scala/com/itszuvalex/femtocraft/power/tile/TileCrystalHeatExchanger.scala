package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.api.power.PowerStorageNodeType
import com.itszuvalex.femtocraft.api.{Capabilities, ManagerModules}
import com.itszuvalex.femtocraft.power.tile.TileCrystalHeatExchanger.CrystalHeatExchangerModule
import com.itszuvalex.femtocraft.power.{ModuleColorableFromPowerLeafNode, ModulePowerLeafNode, ModulePowerStorage, ModulePowerStorageNodeFromPowerLeafNode}
import com.itszuvalex.femtocraft.temp.TileEntityInternalModuleTickable
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.Burnable
import com.itszuvalex.itszulib.api.core.{IModule, Module}
import com.itszuvalex.itszulib.api.storage.{IBattery, IItemStorage, ItemStorageArray, PowerBattery}
import com.itszuvalex.itszulib.api.wrappers.{IItemStack, ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.modules.{ModuleIItemHandlerConverter, ModuleIItemStorage}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}

object TileCrystalHeatExchanger {
  val MODULE: IModule[CrystalHeatExchangerModule] = Module.registerModule("CrystalHeatExchangerModule", null)
  val POWER_STORAGE                               = 10000
  val CHARGING_MULTIPLIER                         = 10d
  val BURN_TIME_MULTIPLIER                        = .5d

  val FUEL_INDEX    = 0
  val CRYSTAL_INDEX = 1

  class CrystalHeatExchangerModule(val storage: IItemStorage, val battery: IBattery) extends TileEntityInternalModuleTickable[CrystalHeatExchangerModule] {
    var burnTime = 0
    var burnMax  = 0

    override def module: IModule[CrystalHeatExchangerModule] = MODULE

    override def hasWorldNBT: Boolean = true

    override def writeWorldNBT(tag: NBTTagCompound): Unit = {
      tag.setInteger(CrystalHeatExchangerModule.BURN_TIME_KEY, burnTime)
      tag.setInteger(CrystalHeatExchangerModule.BURN_MAX_KEY, burnMax)
    }

    override def readWorldNBT(tagCompound: NBTTagCompound): Unit = {
      burnTime = tagCompound.getInteger(CrystalHeatExchangerModule.BURN_TIME_KEY)
      burnMax = tagCompound.getInteger(CrystalHeatExchangerModule.BURN_MAX_KEY)
    }

    override def serverUpdate(tile: ITileEntity): Unit = {
      if (burnTime > 0) {
        storage(TileCrystalHeatExchanger.CRYSTAL_INDEX).capabilityOption(Capabilities.ITEM_POWER_CRYSTAL, null).foreach { c =>
          val power = Math.min(c.battery.storage, c.getTransferRate())
          battery.storage = Math.min(battery.maxStorage, battery.storage + power)
          c.battery.storage = Math.max(0, c.battery.storage - power)
          val gen = powerPerTick
          battery.storage = Math.min(battery.maxStorage, battery.storage + gen)
        }

        burnTime -= 1
      }

      if (burnTime <= 0 && battery.storage < battery.maxStorage && storage(TileCrystalHeatExchanger.CRYSTAL_INDEX).hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)) {
        Burnable.getBurnTime(storage(TileCrystalHeatExchanger.FUEL_INDEX).toMinecraft).foreach { f =>
          burnTime = (f * TileCrystalHeatExchanger.BURN_TIME_MULTIPLIER).toInt
          burnMax = burnTime
          storage.split(TileCrystalHeatExchanger.FUEL_INDEX, 1)
        }
      }
    }

    def powerPerTick: Double = storage(TileCrystalHeatExchanger.CRYSTAL_INDEX).moduleOption(ManagerModules.ITEM_POWER_CRYSTAL, null).map(_.getPassiveGen() * TileCrystalHeatExchanger.CHARGING_MULTIPLIER).getOrElse(0d)

    def getBurnMax: Int = burnMax

    def setBurnMax(i: Int): Unit = burnMax = i

    def getBurnTime: Int = burnTime

    def setBurnTime(i: Int): Unit = burnTime = i
  }

  object CrystalHeatExchangerModule {
    val BURN_TIME_KEY = "Burn"
    val BURN_MAX_KEY  = "BurnMax"
  }

}

class TileCrystalHeatExchanger extends TileEntityCoreTickable {
  val storage: IItemStorage = new ItemStorageArray(2) {
    override def canInsert(i: Int, stack: IItemStack): Boolean = {
      stack.isEmpty || (i match {
        case TileCrystalHeatExchanger.CRYSTAL_INDEX => stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
        case TileCrystalHeatExchanger.FUEL_INDEX => Burnable.getBurnTime(stack.toMinecraft).nonEmpty
        case _ => false
      })
    }
  }
  val battery: IBattery     = new PowerBattery(TileCrystalHeatExchanger.POWER_STORAGE)
  val leafNode              = new ModulePowerLeafNode(this, battery, PowerStorageNodeType.PRODUCER, transRate = () => 50d)

  val internal = new CrystalHeatExchangerModule(storage, battery)

  addTileEntityModule(new ModuleIItemStorage(storage))
  addTileEntityModule(new ModuleIItemHandlerConverter)
  addTileEntityModule(new ModulePowerStorage(battery))
  addTileEntityModule(leafNode)
  addTileEntityModule(new ModulePowerStorageNodeFromPowerLeafNode(leafNode))
  addTileEntityModule(new ModuleColorableFromPowerLeafNode(leafNode))

  override def getMod: AnyRef = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileCrystalHeatExchangerID

  // TODO

  override def hasDescription: Boolean = false

  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = false
}
