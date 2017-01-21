package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.item.ItemDumbDust
import com.itszuvalex.femtocraft.industry.item._
import com.itszuvalex.femtocraft.power.item.ItemPowerCrystal
import net.minecraft.item.Item
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary

/**
  * Created by Christopher Harris (Itszuvalex) on 5/3/15.
  */
object FemtoItems {
  var itemPowerCrystal: Item = _

  var itemDumbDust               : Item = _
  var itemCyberleaf              : Item = _
  var itemNanoweaveThread        : Item = _
  var itemNanoweaveSheet         : Item = _
  var itemCracklingDust          : Item = _
  var itemRiftironDust           : Item = _
  var itemPhasemetalDust         : Item = _
  var itemRedstonereplacementDust: Item = _
  var itemDiamondreplacementDust : Item = _

  var itemSolarPanel              : Item = _

  var itemFrame     : Item = _
  var itemMultiblock: Item = _

  var itemMultiTool: Item = _
  var itemShiftTest: Item = _

  def preInit(): Unit = {
    itemPowerCrystal = registerItem(new ItemPowerCrystal, "itemPowerCrystal").registerOre("itemCrystal")
    itemFrame = registerItem(new ItemFrame(), "itemFrame")
    itemMultiblock = registerItem(new ItemMultiblock(), "itemMultiblock")
    itemDumbDust = registerItem(new ItemDumbDust(), "itemDumbDust")
    itemCyberleaf = registerItem(new Item, "itemCyberleaf")
    itemSolarPanel = registerItem(new Item(), "itemSolarPanel")
    itemNanoweaveThread = registerItem(new Item(), "itemNanoweaveThread")
    itemNanoweaveSheet = registerItem(new Item(), "itemNanoweaveSheet")
    itemCracklingDust = registerItem(new Item(), "itemCracklingDust")
    itemRiftironDust = registerItem(new Item(), "itemRiftironDust").setCreativeTab(Femtocraft.tab).registerOre("dustRiftiron")
    itemPhasemetalDust = registerItem(new Item(), "itemPhasemetalDust").setCreativeTab(Femtocraft.tab).registerOre("dustPhasemetal")
    itemRedstonereplacementDust = registerItem(new Item(), "itemRedstonereplacementDust").setCreativeTab(Femtocraft.tab).registerOre("dustRedstonereplacement")
    itemDiamondreplacementDust = registerItem(new Item(), "itemDiamondreplacementDust").setCreativeTab(Femtocraft.tab).registerOre("dustDiamondreplacement")
    itemMultiTool = registerItem(new ItemMultiTool(), "itemMultiTool")
    itemShiftTest = registerItem(new ItemShiftTest(), "itemShiftTest")
  }

  def init(): Unit = {
    itemDumbDust.registerModel()
    itemCyberleaf.registerModel()
    itemSolarPanel.registerModel()
    itemNanoweaveSheet.registerModel()
    itemNanoweaveThread.registerModel()
    itemCracklingDust.registerModel()
    itemRiftironDust.registerModel()
    itemPhasemetalDust.registerModel()
    itemRedstonereplacementDust.registerModel()
    itemDiamondreplacementDust.registerModel()
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

    def registerModel(): Unit = {
      Femtocraft.proxy.onRegisterItem(item, item.getUnlocalizedName.substring(5).toLowerCase())
    }
  }

}
