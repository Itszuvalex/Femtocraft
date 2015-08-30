package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.Power.ItemPowerCrystal
import com.itszuvalex.femtocraft.render.ItemFrameTest
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.Item

/**
 * Created by Christopher Harris (Itszuvalex) on 5/3/15.
 */
object FemtoItems {
  var itemPowerCrystal: Item = null


  var itemFrameTest: Item = null

  def preInit(): Unit = {
    itemPowerCrystal = new ItemPowerCrystal().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemPowerCrystal, "itemPowerCrystal")

    itemFrameTest = new ItemFrameTest().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemFrameTest, "itemFrameTest")
  }

  def init(): Unit = {

  }

  def postInit(): Unit = {

  }

}
