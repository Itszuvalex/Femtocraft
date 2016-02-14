package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.item.{ItemBaseSeed, ItemDumbDust}
import com.itszuvalex.femtocraft.industry.item.{ItemMultiblock, ItemFrame}
import com.itszuvalex.femtocraft.power.item.ItemPowerCrystal
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.item.Item

/**
  * Created by Christopher Harris (Itszuvalex) on 5/3/15.
  */
object FemtoItems {
  var itemPowerCrystal: Item = null

  var itemDumbDust     : Item = null
  var itemCracklingDust: Item = null


  var itemFrame   : Item = null
  var itemBaseSeed: Item = null
  var itemMultiblock: Item = null

  def preInit(): Unit = {
    itemPowerCrystal = new ItemPowerCrystal().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemPowerCrystal, "itemPowerCrystal")

    itemFrame = new ItemFrame().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemFrame, "itemFrameTest")

    itemBaseSeed = new ItemBaseSeed().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemBaseSeed, "itemBaseSeed")

    itemMultiblock = new ItemMultiblock().setCreativeTab(Femtocraft.tab)
    GameRegistry.registerItem(itemMultiblock, "itemMultiblock")

    itemDumbDust = new ItemDumbDust().setTextureName(Femtocraft.ID + ":" + "dust_dumb").setUnlocalizedName("itemDumbDust")
    GameRegistry.registerItem(itemDumbDust, "itemDumbDust")

    itemCracklingDust = new Item().setCreativeTab(Femtocraft.tab).setTextureName(Femtocraft.ID + ":" + "dust_crystal").setUnlocalizedName("itemCracklingDust")
    GameRegistry.registerItem(itemCracklingDust, "itemCracklingDust")
  }

  def init(): Unit = {

  }

  def postInit(): Unit = {

  }

}
