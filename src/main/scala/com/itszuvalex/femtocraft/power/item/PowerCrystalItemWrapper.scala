package com.itszuvalex.femtocraft.power.item

import com.itszuvalex.femtocraft.power.item.PowerCrystalItemWrapper._
import com.itszuvalex.itszulib.api.wrappers.{IBattery, WrapperNBTBattery}
import com.itszuvalex.itszulib.implicits.ItemStackImplicits._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

import scala.collection.JavaConversions._

/**
  * Created by Chris on 1/8/2017.
  */

object PowerCrystalItemWrapper {
  val NBT_COMPOUND_KEY    = "PowerCrystal"
  val COLOR_KEY           = "Color"
  val TYPE_KEY            = "Type"
  val STORAGE_CURRENT_KEY = "Storage_Current"
  val STORAGE_MAX_KEY     = "Storage_Max"
  val STORAGE_PARTIAL_KEY = "Storage_Partial"
  val PASSIVE_GEN_KEY     = "Passive"
  val TRANSFER_KEY        = "Transfer"
  val NAME_KEY            = "Name"
}

class PowerCrystalItemWrapper(val stack: ItemStack) extends IPowerCrystal {
  if (stack.getTagCompound == null)
    stack.setTagCompound(new NBTTagCompound)
  val batWrapper = new WrapperNBTBattery(stack.getTagCompound)

  override def onTick(): Unit = {
    if (stack == null) return

    setStoragePartial(getStoragePartial() + getPassiveGen())
    if (getStoragePartial() > 0) {
      setStoragePartial(getStoragePartial() - 1)
      battery.storage = Math.min(battery.storage + 1, battery.maxStorage)
    }
  }

  override def battery: IBattery = batWrapper

  override def getPassiveGen(): Double = {
    stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
      return comp.Float(PASSIVE_GEN_KEY)
    }
  }

  override def setPassiveGen(passiveGen: Float): Unit = {
    stack.forceTag.merge(NBT_COMPOUND_KEY ->
      NBTCompound(
        PASSIVE_GEN_KEY -> passiveGen)
    )
  }

  override def getStoragePartial(): Double = {
    stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
      return comp.Double(STORAGE_PARTIAL_KEY)
    }
  }

  override def setStoragePartial(amount: Double): Unit = {
    stack.forceTag.merge(NBT_COMPOUND_KEY ->
      NBTCompound(
        STORAGE_PARTIAL_KEY -> amount
      )
    )

  }

  def addInformation(tooltipList: java.util.List[_]): Unit = {
    val tlist = tooltipList.asInstanceOf[java.util.List[String]]
    tlist += "Crystal Type:" + getType()
    tlist += "Passive Gen:" + getPassiveGen().formatted("%.2f")
    tlist += "Transfer Rate:" + getTransferRate()
    tlist += "Power:" + battery.storage.formatted("%.0f") + "/" + battery.maxStorage.formatted("%.0f")
    //        tlist += "Partial Power:" + crystal.getStoragePartial(stack)
  }

  override def getTransferRate(): Double = {
    stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
      return comp.Double(TRANSFER_KEY)
    }
  }

  override def setTransferRate(rate: Double): Unit = {
    stack.forceTag.merge(NBT_COMPOUND_KEY ->
      NBTCompound(
        TRANSFER_KEY -> rate
      )
    )
  }

  override def getType(): String = {
    stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
      return comp.String(TYPE_KEY)
    }
  }

  override def setType(ctype: String): Unit = {
    stack.forceTag.merge(NBT_COMPOUND_KEY ->
      NBTCompound(
        TYPE_KEY -> ctype
      )
    )
  }

  override def getColor(): Int = {
    stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
      return comp.Int(COLOR_KEY)
    }
    Color(0.toByte, 255.toByte, 255.toByte, 255.toByte).toInt
  }

  override def setColor(color: Int): Unit = {
    stack.forceTag.merge(NBT_COMPOUND_KEY ->
      NBTCompound(
        COLOR_KEY -> color
      )
    )
  }

  override def getName(): String = {
    stack.getTagCompound.NBTCompound(NBT_COMPOUND_KEY) { comp =>
      return comp.String(NAME_KEY)
    }
  }

  override def setName(name: String): Unit = {
    stack.forceTag.merge(NBT_COMPOUND_KEY ->
      NBTCompound(
        NAME_KEY -> name
      )
    )
  }

  def updateDamage(): Unit = {
    val max = battery.maxStorage
    if (max > 0)
      stack.setItemDamage(stack.getMaxDamage - ((battery.storage / max) * stack.getMaxDamage).toInt)
  }
}
