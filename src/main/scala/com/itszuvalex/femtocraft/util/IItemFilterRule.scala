package com.itszuvalex.femtocraft.util

import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.INBTSerializable

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
trait IItemFilterRule extends INBTSerializable[NBTTagCompound] {

  def itemMatches(item: ItemStack): Boolean

  def ruleType: String

}
