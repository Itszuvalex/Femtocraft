package com.itszuvalex.femtocraft.util

import com.itszuvalex.itszulib.api.core.NBTSerializable
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 2/1/2016.
  */
trait IItemFilterRule extends NBTSerializable {

  def itemMatches(item: ItemStack): Boolean

  def ruleType: String

}
