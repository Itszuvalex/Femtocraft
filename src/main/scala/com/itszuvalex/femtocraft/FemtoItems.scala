package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.item.ItemBaseSeed
import com.itszuvalex.femtocraft.industry.item.ItemFrame
import com.itszuvalex.femtocraft.power.ItemPowerCrystal
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.Item

/**
  * Created by Christopher Harris (Itszuvalex) on 5/3/15.
  */
object FemtoItems {
  var itemPowerCrystal: Item = null


  var itemFrame   : Item = null
  var itemBaseSeed: Item = null

  def preInit(): Unit = {
    itemPowerCrystal = new ItemPowerCrystal().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemPowerCrystal, "itemPowerCrystal")

    itemFrame = new ItemFrame().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemFrame, "itemFrameTest")

    itemBaseSeed = new ItemBaseSeed().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemBaseSeed, "itemBaseSeed")
  }

  def init(): Unit = {

  }

  def postInit(): Unit = {

  }

}
