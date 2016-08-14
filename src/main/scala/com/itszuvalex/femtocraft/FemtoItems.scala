package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.item.{ItemBaseSeed, ItemDumbDust}
import com.itszuvalex.femtocraft.industry.item.{ItemFrame, ItemFurnaceAssembly, ItemGrinderAssembly, ItemMultiblock}
import com.itszuvalex.femtocraft.power.item.ItemPowerCrystal
import net.minecraft.item.Item
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary

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
    itemPowerCrystal = registerItem(new ItemPowerCrystal, "itemPowerCrystal").registerOre("itemCrystal")
    itemFrame = registerItem(new ItemFrame(), "itemFrame")
    itemBaseSeed = registerItem(new ItemBaseSeed(), "itemBaseSeed")
    itemMultiblock = registerItem(new ItemMultiblock(), "itemMultiblock")
    itemDumbDust = registerItem(new ItemDumbDust(), "itemDumbDust")
    itemCracklingDust = registerItem(new Item(), "itemCracklingDust")
    itemFurnaceAssembly = registerItem(new ItemFurnaceAssembly(), "itemFurnaceAssembly").registerOre("assemblyFurnace")
    itemGrinderAssembly = registerItem(new ItemGrinderAssembly(), "itemGrinderAssembly").registerOre("assemblyGrinder")
  }

  def init(): Unit = {
    itemPowerCrystal.registerModel()
    itemDumbDust.registerModel()
    itemCracklingDust.registerModel()
  }

  def postInit(): Unit = {

  }


  def registerItem[T <: Item](item: T, name: String): T = {
    item.setCreativeTab(Femtocraft.tab).setRegistryName(Femtocraft.ID.toLowerCase(), name).setUnlocalizedName(name)
    GameRegistry.register(item)
    item
  }

  implicit class ItemHelpers[T <: Item](item: T) {
    def registerOre(name: String): T = {
      OreDictionary.registerOre(name, item)
      item
    }

    def registerModel() = {
      Femtocraft.proxy.onRegisterItem(item, item.getUnlocalizedName.substring(5))
    }
  }

}
