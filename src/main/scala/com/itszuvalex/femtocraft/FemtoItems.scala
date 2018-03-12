package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.item.ItemDumbDust
import com.itszuvalex.femtocraft.industry.item._
import com.itszuvalex.femtocraft.logistics.item.ItemLogisticsItemChip
import com.itszuvalex.femtocraft.nanite.items.ItemNanolash
import com.itszuvalex.femtocraft.power.item.ItemPowerCrystal
import net.minecraft.item.Item
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.registries.IForgeRegistry

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Christopher Harris (Itszuvalex) on 5/3/15.
  */
object FemtoItems {
  var itemPowerCrystal: Item = _

  var itemDumbDust                : Item = _
  var itemCyberleaf               : Item = _
  var itemNanoweaveThread         : Item = _
  var itemNanoweaveSheet          : Item = _
  var itemCracklingDust           : Item = _
  var itemRiftironDust            : Item = _
  var itemPhasemetalDust          : Item = _
  var itemRedstonereplacementDust : Item = _
  var itemLapisreplacementDust    : Item = _
  var itemDiamondreplacementDust  : Item = _
  var itemRiftironIngotDevoid     : Item = _
  var itemRiftironIngotActivated  : Item = _
  var itemPhasemetalIngotDevoid   : Item = _
  var itemPhasemetalIngotActivated: Item = _
  var itemBasicCircuit            : Item = _
  var itemEnergyRegulator         : Item = _
  var itemCrystalBattery          : Item = _
  var itemNaniteBeacon            : Item = _
  var itemNanoChannel             : Item = _

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
  var itemNanoLash : Item = _

  val itemCallbacks = new ArrayBuffer[() => Unit]()

  @SubscribeEvent
  def registerItems(event: RegistryEvent.Register[Item]): Unit = {
    val registry = event.getRegistry
    itemPowerCrystal = registerItem(registry, new ItemPowerCrystal, "itemPowerCrystal").registerOre("itemCrystal")
    itemFrame = registerItem(registry, new ItemFrame(), "itemFrame")
    itemMultiblock = registerItem(registry, new ItemMultiblock(), "itemMultiblock")
    itemDumbDust = registerItem(registry, new ItemDumbDust(), "itemDumbDust")
    itemCyberleaf = registerItem(registry, new Item, "itemCyberleaf")
    itemSolarPanel = registerItem(registry, new Item(), "itemSolarPanel")
    itemNanoweaveThread = registerItem(registry, new Item(), "itemNanoweaveThread")
    itemNanoweaveSheet = registerItem(registry, new Item(), "itemNanoweaveSheet")
    itemCracklingDust = registerItem(registry, new Item(), "itemCracklingDust")
    itemRiftironDust = registerItem(registry, new Item(), "itemRiftironDust").setCreativeTab(Femtocraft.tab).registerOre("dustRiftiron")
    itemPhasemetalDust = registerItem(registry, new Item(), "itemPhasemetalDust").setCreativeTab(Femtocraft.tab).registerOre("dustPhasemetal")
    itemIronDust = registerItem(registry, new Item(), "itemIronDust").setCreativeTab(Femtocraft.tab).registerOre("dustIron")
    itemGoldDust = registerItem(registry, new Item(), "itemGoldDust").setCreativeTab(Femtocraft.tab).registerOre("dustGold")
    itemDiamondDust = registerItem(registry, new Item(), "itemDiamondDust").setCreativeTab(Femtocraft.tab).registerOre("dustDiamond")
    itemRedstonereplacementDust = registerItem(registry, new Item(), "itemRedstonereplacementDust").setCreativeTab(Femtocraft.tab).registerOre("dustRedstonereplacement")
    itemLapisreplacementDust = registerItem(registry, new Item(), "itemLapisreplacementDust").setCreativeTab(Femtocraft.tab).registerOre("dustLapisreplacement")
    itemDiamondreplacementDust = registerItem(registry, new Item(), "itemDiamondreplacementDust").setCreativeTab(Femtocraft.tab).registerOre("dustDiamondreplacement")
    itemRiftironIngotDevoid = registerItem(registry, new Item(), "itemRiftironIngotDevoid").setCreativeTab(Femtocraft.tab).registerOre("ingotRiftironDevoid")
    itemRiftironIngotActivated = registerItem(registry, new Item(), "itemRiftironIngotActivated").setCreativeTab(Femtocraft.tab).registerOre("ingotRiftironActivated")
    itemPhasemetalIngotDevoid = registerItem(registry, new Item(), "itemPhasemetalIngotDevoid").setCreativeTab(Femtocraft.tab).registerOre("ingotPhasemetalDevoid")
    itemPhasemetalIngotActivated = registerItem(registry, new Item(), "itemPhasemetalIngotActivated").setCreativeTab(Femtocraft.tab).registerOre("ingotPhasemetalActivated")
    itemBasicCircuit = registerItem(registry, new Item(), "itemBasicCircuit").setCreativeTab(Femtocraft.tab)
    itemEnergyRegulator = registerItem(registry, new Item(), "itemEnergyRegulator").setCreativeTab(Femtocraft.tab)
    itemCrystalBattery = registerItem(registry, new Item(), "itemCrystalBattery").setCreativeTab(Femtocraft.tab)
    itemNaniteBeacon = registerItem(registry, new Item(), "itemNaniteBeacon").setCreativeTab(Femtocraft.tab)
    itemNanoChannel = registerItem(registry, new Item(), "itemNanoChannel").setCreativeTab(Femtocraft.tab)
    itemLogisticsItemChipBasic = registerItem(registry, new ItemLogisticsItemChip(), "itemLogisticsItemChipBasic").setCreativeTab(Femtocraft.tab)
    itemLogisticsFluidChipBasic = registerItem(registry, new Item(), "itemLogisticsFluidChipBasic").setCreativeTab(Femtocraft.tab)
    itemLogisticsNaniteChipBasic = registerItem(registry, new Item(), "itemLogisticsNaniteChipBasic").setCreativeTab(Femtocraft.tab)
    itemMultiTool = registerItem(registry, new ItemMultiTool(), "itemMultiTool")
    itemShiftTest = registerItem(registry, new ItemShiftTest(), "itemShiftTest").setCreativeTab(Femtocraft.tab)
    itemNanoLash = registerItem(registry, new ItemNanolash(), "itemNanoLash").setCreativeTab(Femtocraft.tab)

    FemtoBlocks.registerItemBlocks(registry)

    itemCallbacks.foreach(_ ())
    itemCallbacks.clear()
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
    itemRiftironIngotActivated.registerModel()
    itemPhasemetalIngotDevoid.registerModel()
    itemPhasemetalIngotActivated.registerModel()
    itemRedstonereplacementDust.registerModel()
    itemLapisreplacementDust.registerModel()
    itemDiamondreplacementDust.registerModel()
    itemBasicCircuit.registerModel()
    itemEnergyRegulator.registerModel()
    itemCrystalBattery.registerModel()
    itemNaniteBeacon.registerModel()
    itemNanoChannel.registerModel()
    itemLogisticsItemChipBasic.registerModel()
    itemLogisticsFluidChipBasic.registerModel()
    itemLogisticsNaniteChipBasic.registerModel()
    itemMultiTool.registerModel()
    itemShiftTest.registerModel()
    itemNanoLash.registerModel()
  }

  def postInit(): Unit = {

  }

  def registerItem[T <: Item](registry: IForgeRegistry[Item], item: T, name: String): T = {
    item.setCreativeTab(Femtocraft.tab).setRegistryName(Femtocraft.ID.toLowerCase(), name).setUnlocalizedName(name)
    registry.register(item)
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
