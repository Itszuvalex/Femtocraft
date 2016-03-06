package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.item.{ItemBaseSeed, ItemDumbDust}
import com.itszuvalex.femtocraft.industry.item.{ItemFrame, ItemFurnaceAssembly, ItemGrinderAssembly, ItemMultiblock}
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

  var itemFurnaceAssembly: Item = null
  var itemGrinderAssembly: Item = null

  var itemFrame     : Item = null
  var itemBaseSeed  : Item = null
  var itemMultiblock: Item = null

  def preInit(): Unit = {
    itemPowerCrystal = new ItemPowerCrystal().setCreativeTab(Femtocraft.tab).setUnlocalizedName("item.PowerCrystal")
    GameRegistry.registerItem(itemPowerCrystal, "itemPowerCrystal")

    itemFrame = new ItemFrame().setCreativeTab(Femtocraft.tab).setUnlocalizedName("item.Frame")
    GameRegistry.registerItem(itemFrame, "itemFrameTest")

    itemBaseSeed = new ItemBaseSeed().setCreativeTab(Femtocraft.tab).setUnlocalizedName("item.seed")
    GameRegistry.registerItem(itemBaseSeed, "itemBaseSeed")

    itemMultiblock = new ItemMultiblock().setCreativeTab(Femtocraft.tab).setUnlocalizedName("item.multiblock")
    GameRegistry.registerItem(itemMultiblock, "itemMultiblock")

    itemDumbDust = new ItemDumbDust().setTextureName(Femtocraft.ID + ":" + "dust_dumb").setUnlocalizedName("item.dumbDust")
    GameRegistry.registerItem(itemDumbDust, "itemDumbDust")

    itemCracklingDust = new Item().setCreativeTab(Femtocraft.tab).setTextureName(Femtocraft.ID + ":" + "dust_crystal").setUnlocalizedName("item.cracklingDust")
    GameRegistry.registerItem(itemCracklingDust, "itemCracklingDust")

    itemFurnaceAssembly = new ItemFurnaceAssembly().setCreativeTab(Femtocraft.tab).setUnlocalizedName("item.FurnaceAssembly")
    GameRegistry.registerItem(itemFurnaceAssembly, "itemFurnaceAssembly")

    itemGrinderAssembly = new ItemGrinderAssembly().setCreativeTab(Femtocraft.tab).setUnlocalizedName("item.GrinderAssembly")
    GameRegistry.registerItem(itemGrinderAssembly, "itemGrinderAssembly")
  }

  def init(): Unit = {

  }

  def postInit(): Unit = {

  }

}
