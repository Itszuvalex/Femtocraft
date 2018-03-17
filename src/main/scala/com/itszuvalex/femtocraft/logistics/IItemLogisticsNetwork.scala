package com.itszuvalex.femtocraft.logistics

import com.itszuvalex.femtocraft.logistics.storage.item.IIndexedInventory
import net.minecraft.item.ItemStack

/**
  * Created by Christopher Harris (Itszuvalex) on 2/25/2016.
  */
trait IItemLogisticsNetwork {

  def getKeys: Set[String]

  def getInventories(key: String): Set[IIndexedInventory]

  def containsItem(item: ItemStack, amt: Int, key: String): Boolean

  def withdrawItem(item: ItemStack, amt: Int, key: String): Boolean

  def insertItem(item: ItemStack, amt: Int, key: String): Boolean

}
