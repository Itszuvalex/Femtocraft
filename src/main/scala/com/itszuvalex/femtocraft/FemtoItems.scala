package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.item.ItemDumbDust
import com.itszuvalex.femtocraft.industry.item._
import com.itszuvalex.femtocraft.logistics.item.ItemLogisticsItemChip
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
  var itemLapisreplacementDust   : Item = _
  var itemDiamondreplacementDust : Item = _
  var itemRiftironIngotDevoid    : Item = _
  var itemPhasemetalIngotDevoid  : Item = _
  var itemBasicCircuit           : Item = _
  var itemEnergyRegulator        : Item = _
  var itemCrystalBattery         : Item = _
  var itemNaniteBeacon           : Item = _

  var itemIronDust   : Item = _
  var itemGoldDust   : Item = _
  var itemDiamondDust: Item = _

  var itemSolarPanel: Item = _

  var itemLogisticsItemChipBasic  : Item = _
  var itemLogisticsFluidChipBasic : Item = _
  var itemLogisticsNaniteChipBasic: Item = _

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
    itemIronDust = registerItem(new Item(), "itemIronDust").setCreativeTab(Femtocraft.tab).registerOre("dustIron")
    itemGoldDust = registerItem(new Item(), "itemGoldDust").setCreativeTab(Femtocraft.tab).registerOre("dustGold")
    itemDiamondDust = registerItem(new Item(), "itemDiamondDust").setCreativeTab(Femtocraft.tab).registerOre("dustDiamond")
    itemRedstonereplacementDust = registerItem(new Item(), "itemRedstonereplacementDust").setCreativeTab(Femtocraft.tab).registerOre("dustRedstonereplacement")
    itemLapisreplacementDust = registerItem(new Item(), "itemLapisreplacementDust").setCreativeTab(Femtocraft.tab).registerOre("dustLapisreplacement")
    itemDiamondreplacementDust = registerItem(new Item(), "itemDiamondreplacementDust").setCreativeTab(Femtocraft.tab).registerOre("dustDiamondreplacement")
    itemRiftironIngotDevoid = registerItem(new Item(), "itemRiftironIngotDevoid").setCreativeTab(Femtocraft.tab).registerOre("ingotRiftironDevoid")
    itemPhasemetalIngotDevoid = registerItem(new Item(), "itemPhasemetalIngotDevoid").setCreativeTab(Femtocraft.tab).registerOre("ingotPhasemetalDevoid")
    itemBasicCircuit = registerItem(new Item(), "itemBasicCircuit").setCreativeTab(Femtocraft.tab)
    itemEnergyRegulator = registerItem(new Item(), "itemEnergyRegulator").setCreativeTab(Femtocraft.tab)
    itemCrystalBattery = registerItem(new Item(), "itemCrystalBattery").setCreativeTab(Femtocraft.tab)
    itemNaniteBeacon = registerItem(new Item(), "itemNaniteBeacon").setCreativeTab(Femtocraft.tab)
    itemLogisticsItemChipBasic = registerItem(new ItemLogisticsItemChip(), "itemLogisticsItemChipBasic").setCreativeTab(Femtocraft.tab)
    itemLogisticsFluidChipBasic = registerItem(new Item(), "itemLogisticsFluidChipBasic").setCreativeTab(Femtocraft.tab)
    itemLogisticsNaniteChipBasic = registerItem(new Item(), "itemLogisticsNaniteChipBasic").setCreativeTab(Femtocraft.tab)
    itemMultiTool = registerItem(new ItemMultiTool(), "itemMultiTool")
    itemShiftTest = registerItem(new ItemShiftTest(), "itemShiftTest")
  }

  def init(): Unit = {
    itemDumbDust.registerModel()
    itemFrame.registerModel()
    itemCyberleaf.registerModel()
    itemSolarPanel.registerModel()
    itemNanoweaveSheet.registerModel()
    itemNanoweaveThread.registerModel()
    itemCracklingDust.registerModel()
    itemRiftironDust.registerModel()
    itemPhasemetalDust.registerModel()
    itemIronDust.registerModel()
    itemGoldDust.registerModel()
    itemDiamondDust.registerModel()
    itemRiftironIngotDevoid.registerModel()
    itemPhasemetalIngotDevoid.registerModel()
    itemRedstonereplacementDust.registerModel()
    itemLapisreplacementDust.registerModel()
    itemDiamondreplacementDust.registerModel()
    itemBasicCircuit.registerModel()
    itemEnergyRegulator.registerModel()
    itemCrystalBattery.registerModel()
    itemNaniteBeacon.registerModel()
    itemLogisticsItemChipBasic.registerModel()
    itemLogisticsFluidChipBasic.registerModel()
    itemLogisticsNaniteChipBasic.registerModel()
    itemMultiTool.registerModel()
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
