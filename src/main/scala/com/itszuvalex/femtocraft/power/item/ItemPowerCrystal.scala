package com.itszuvalex.femtocraft.power.item

import java.util

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.itszulib.api.wrappers.WrapperNBTBattery
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.{Capability, ICapabilityProvider}

import scala.collection.JavaConversions._

/**
  * Created by Christopher on 8/26/2015.
  */
object ItemPowerCrystal {

  val NBT_COMPOUND_KEY    = "PowerCrystal"
  val COLOR_KEY           = "Color"
  val TYPE_KEY            = "Type"
  val STORAGE_CURRENT_KEY = "Storage_Current"
  val STORAGE_MAX_KEY     = "Storage_Max"
  val STORAGE_PARTIAL_KEY = "Storage_Partial"
  val PASSIVE_GEN_KEY     = "Passive"
  val TRANSFER_KEY        = "Transfer"
  val NAME_KEY            = "Name"

  def addInformation(stack: ItemStack, tooltipList: util.List[_]): Unit = {
    val tlist = tooltipList.asInstanceOf[util.List[String]]
    if (stack == null) return
    if (!stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)) return
    val cap = stack.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    tlist += "Crystal Type:" + cap.getType()
    tlist += "Passive Gen:" + cap.getPassiveGen().formatted("%.2f")
    tlist += "Transfer Rate:" + cap.getTransferRate()
    tlist += "Power:" + cap.battery.storage.formatted("%.0f") + "/" + cap.battery.maxStorage.formatted("%.0f")
    //        tlist += "Partial Power:" + crystal.getStoragePartial(stack)
  }

  def getFullName(stack: ItemStack): String = {
    if (stack == null) return ""
    if (!stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)) return ""
    val cap = stack.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    cap.getType() + " " + cap.getName()
  }

  def initialize(stack: ItemStack,
    name: String,
    rtype: String,
    color: Int,
    storage: Double,
    passiveGen: Float,
    transfer: Int): ItemStack = {
    if (stack == null) return stack
    if (!stack.hasCapability(Capabilities.ITEM_POWER_CRYSTAL, null)) return stack

    val cap = stack.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    cap.setName(name)
    cap.setType(rtype)
    cap.setColor(color)
    cap.battery.maxStorage = storage
    cap.battery.storage = storage / 2
    cap.setPassiveGen(passiveGen)
    cap.setTransferRate(transfer)
    cap.setStoragePartial(0)
    stack
  }
}

class ItemPowerCrystal extends Item {
  setNoRepair()
  setMaxDamage(100)

  override def initCapabilities(stack: ItemStack, nbt: NBTTagCompound): ICapabilityProvider = {
    new ICapabilityProvider {
      override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
        if (capability == Capabilities.ITEM_POWER_CRYSTAL) new PowerCrystalItemWrapper(stack).asInstanceOf[T]
        else if (capability == Capabilities.POWER_STORAGE) new WrapperNBTBattery(if (stack.hasTagCompound) stack.getTagCompound
        else {val tag = new NBTTagCompound; stack.setTagCompound(tag); tag}).asInstanceOf[T]
        else null.asInstanceOf[T]
      }

      override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
        if (capability == Capabilities.ITEM_POWER_CRYSTAL) true
        else if (capability == Capabilities.POWER_STORAGE) true
        else false
      }
    }
  }


  override def isDamaged(stack: ItemStack): Boolean = {
    val cap = stack.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    cap.battery.storage != cap.battery.maxStorage
  }

  override def getDamage(stack: ItemStack): Int = {
    val cap = stack.getCapability(Capabilities.ITEM_POWER_CRYSTAL, null)
    (getMaxDamage(stack) - ((cap.battery.storage * getMaxDamage(stack)) / cap.battery.maxStorage)).toInt
  }

  override def addInformation(stack: ItemStack, playerIn: EntityPlayer, tooltip: util.List[String], advanced: Boolean): Unit = {
    super.addInformation(stack, playerIn, tooltip, advanced)
    ItemPowerCrystal.addInformation(stack, tooltip)
  }
}
