package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.itszulib.api.wrappers.{Converter, IItemStack}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
  * Created by Chris on 12/11/2016.
  */
object ItemMultiTool {
  val TOOL_PICKAXE = "pickaxe"
  val TOOL_AXE     = "axe"
  val TOOL_SHOVEL  = "shovel"

  val UPGRADE_NBT_KEY    = "Upgrades"
  val PRIMARY_TOOL_KEY   = "Primary"
  val SECONDARY_TOOL_KEY = "Secondary"

  val upgradeMap: mutable.HashMap[String, IMultiToolUpgrade] = mutable.HashMap[String, IMultiToolUpgrade]()

  def setActivePrimaryTool(item: IItemStack, tool: String): Unit = {
    if (item.nbt == null)
      item.nbt = new NBTTagCompound
    item.nbt.setString(PRIMARY_TOOL_KEY, tool)
  }

  def setActivePrimaryTool(item: IItemStack, tool: IMultiToolUpgrade): Unit = setActivePrimaryTool(item, tool.name)

  def setActiveSecondaryTool(item: IItemStack, tool: String): Unit = {
    if (item.nbt == null)
      item.nbt = new NBTTagCompound
    item.nbt.setString(SECONDARY_TOOL_KEY, tool)
  }

  def setActiveSecondaryTool(item: IItemStack, tool: IMultiToolUpgrade): Unit = setActiveSecondaryTool(item, tool.name)

  def activePrimaryTool(item: IItemStack): Option[IMultiToolUpgrade] = {
    if (item.nbt == null) null
    if (!item.nbt.hasKey(PRIMARY_TOOL_KEY)) null
    else upgradeMap.get(item.nbt.getString(PRIMARY_TOOL_KEY))
  }

  def activeSecondaryTool(item: IItemStack): Option[IMultiToolUpgrade] = {
    if (item.nbt == null) null
    if (!item.nbt.hasKey(SECONDARY_TOOL_KEY)) null
    else upgradeMap.get(item.nbt.getString(SECONDARY_TOOL_KEY))
  }

  def primaryUpgrades(item: IItemStack): Set[IMultiToolUpgrade] = upgrades(item).filter(_.isPrimary(item))

  def secondaryUpgrades(item: IItemStack): Set[IMultiToolUpgrade] = upgrades(item).filterNot(_.isPrimary(item))

  def addUpgrade(item: IItemStack, upgrade: IMultiToolUpgrade): Unit = addUpgrade(item, upgrade.name)

  def addUpgrade(item: IItemStack, upgrade: String): Unit = {
    val upgrades = upgradesNBT(item)
    if (!upgrades.hasKey(upgrade))
      upgrades.setTag(upgrade, new NBTTagCompound)
  }

  def removeUpgrade(item: IItemStack, upgrade: IMultiToolUpgrade): Unit = addUpgrade(item, upgrade.name)

  def removeUpgrade(item: IItemStack, upgrade: String): Unit = {
    val upgrades = upgradesNBT(item, createIfEmpty = false)
    if (upgrades != null && upgrades.hasKey(upgrade))
      upgrades.removeTag(upgrade)
  }

  def upgrades(item: IItemStack): Set[IMultiToolUpgrade] = {
    if (item.nbt == null) Set()

    val upgradeNBT = upgradesNBT(item)
    upgradeNBT.getKeySet.flatMap(upgradeMap.get).toSet
  }

  def upgradesNBT(item: IItemStack, createIfEmpty: Boolean = true): NBTTagCompound = {
    if (item.nbt == null) {
      if (createIfEmpty)
        item.nbt = new NBTTagCompound
    }

    item.nbt
  }

  def upgradeNBT(upgrade: String, item: IItemStack, createIfEmpty: Boolean): NBTTagCompound = {
    val upgradeNBT = upgradesNBT(item, createIfEmpty)
    if (!upgradeNBT.hasKey(UPGRADE_NBT_KEY)) {
      if (createIfEmpty)
        upgradeNBT.setTag(UPGRADE_NBT_KEY, new NBTTagCompound)
    }
    upgradeNBT.getCompoundTag(UPGRADE_NBT_KEY)
  }

  def upgradeNBT(upgrade: IMultiToolUpgrade, item: IItemStack, createIfEmpty: Boolean = true): NBTTagCompound = upgradeNBT(upgrade.name, item, createIfEmpty)

}

class ItemMultiTool extends Item {
  override def getItemEnchantability: Int = 0

  override def getIsRepairable(toRepair: ItemStack, repair: ItemStack): Boolean = false

  @SideOnly(Side.CLIENT)
  override def isFull3D: Boolean = true

  def upgrades(item: IItemStack): Set[IMultiToolUpgrade] = ItemMultiTool.upgrades(item)

  def primaryUpgrades(item: IItemStack): Set[IMultiToolUpgrade] = ItemMultiTool.primaryUpgrades(item)

  def secondaryUpgrades(item: IItemStack): Set[IMultiToolUpgrade] = ItemMultiTool.secondaryUpgrades(item)

  def addUpgrade(item: IItemStack, upgrade: IMultiToolUpgrade): Unit = ItemMultiTool.addUpgrade(item, upgrade)

  def addUpgrade(item: IItemStack, upgrade: String): Unit = ItemMultiTool.addUpgrade(item, upgrade)

  def removeUpgrade(item: IItemStack, upgrade: IMultiToolUpgrade): Unit = ItemMultiTool.removeUpgrade(item, upgrade)

  def removeUpgrade(item: IItemStack, upgrade: String): Unit = ItemMultiTool.removeUpgrade(item, upgrade)

  def upgradeNBT(upgrade: String, item: IItemStack): NBTTagCompound = ItemMultiTool.upgradeNBT(upgrade, item, createIfEmpty = false)

  def upgradeNBT(upgrade: IMultiToolUpgrade, item: IItemStack): NBTTagCompound = ItemMultiTool.upgradeNBT(upgrade, item)

  def activePrimaryTool(item: IItemStack): Option[IMultiToolUpgrade] = ItemMultiTool.activePrimaryTool(item)

  override def getHarvestLevel(stack: ItemStack, toolClass: String, @javax.annotation.Nullable player: EntityPlayer, @javax.annotation.Nullable blockState: IBlockState): Int = {
    val level: Int = super.getHarvestLevel(stack, toolClass, player, blockState)
    if (level == -1) {
      val iitem = Converter.IItemStackFromItemStack(stack)
      upgrades(iitem).filter { upgrade => activePrimaryTool(iitem).contains(upgrade) || upgrade.isAutoSwitchable(iitem) }.map(_.getHarvestLevel(iitem, toolClass, player, blockState)).max
    }
    else level
  }

  override def getToolClasses(stack: ItemStack): java.util.Set[String] = {
    val iitem = Converter.IItemStackFromItemStack(stack)
    upgrades(iitem).filter { upgrade => activePrimaryTool(iitem).contains(upgrade) || upgrade.isAutoSwitchable(iitem) }.flatMap(_.toolTypes(iitem)).asJava
  }
}
