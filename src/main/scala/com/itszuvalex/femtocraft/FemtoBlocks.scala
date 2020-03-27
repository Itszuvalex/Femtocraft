package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.cyber.block._
import com.itszuvalex.femtocraft.industry.block._
import com.itszuvalex.femtocraft.logistics.block.{BlockConduit, BlockFluidRepository, BlockItemRepository, BlockNaniteRepository}
import com.itszuvalex.femtocraft.logistics.test.{BlockNetworkTest, BlockTaskProviderTest, BlockWorkerProviderTest}
import com.itszuvalex.femtocraft.nanite.block.BlockNaniteHiveSmall
import com.itszuvalex.femtocraft.power.block._
import com.itszuvalex.femtocraft.worldgen.block.BlockCrystalsWorldgen
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.item.{Item, ItemBlock}
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.oredict.OreDictionary
import net.minecraftforge.registries.IForgeRegistry

import scala.collection.mutable.ArrayBuffer

/**
  * Created by Christopher Harris (Itszuvalex) on 5/3/15.
  */
object FemtoBlocks {

  val blockCallbacks = new ArrayBuffer[() => Unit]()
  private val itemBlocksToRegister = new ArrayBuffer[(Block, String)]
  private val oresToRegister       = new ArrayBuffer[(Block, String)]
  //Cyber
  var blockSubstrate             : Block = _
  var blockRefinedSubstrate      : Block = _
  var blockCyberwood             : Block = _
  var blockCyberleaf             : Block = _
  var blockNanoweave             : Block = _
  var blockRiftiron              : Block = _
  var blockPhasemetal            : Block = _
  var blockRedstonereplacement   : Block = _
  var blockLapisreplacement      : Block = _
  var blockDiamondreplacement    : Block = _
  var blockCrystals              : Block = _
  var blockNaniteRepository      : Block = _
  var blockItemRepository        : Block = _
  var blockFluidRepository       : Block = _
  var blockNanoFurnace           : Block = _
  var blockNaniteInfuser         : Block = _
  var blockFrame                 : Block = _
  var blockCyberBase             : Block = _
  var blockCyberMachineInProgress: Block = _
  var blockNaniteHiveSmall       : Block = _
  var blockCrystalMount          : Block = _
  var blockPowerPedestal         : Block = _
  var blockCrystalChargingArray  : Block = _
  var blockCrystalStorageArray   : Block = _
  var blockCrystalHeatExchanger  : Block = _
  var blockGerminationChamber    : Block = _
  var blockNaniteExtractor       : Block = _

  //Tests
  var blockDemolisher        : Block = _
  var blockConduit           : Block = _
  var blockGlowStick         : Block = _
  var testBlock              : Block = _
  var testNetworkBlock       : Block = _
  var testDiffusionNode      : Block = _
  var testDiffusionTargetNode: Block = _
  var testDirectNode         : Block = _
  var testGenerationNode     : Block = _
  var testTransferNode       : Block = _
  var testTaskProvider       : Block = _
  var testWorkerProvider     : Block = _

  @SubscribeEvent
  def registerBlocks(event: RegistryEvent.Register[Block]) {
    val registry = event.getRegistry
    blockSubstrate = registerBlock(registry, new BlockSubstrate(), "blockSubstrate").registerOre("substrate")
    blockRefinedSubstrate = registerBlock(registry, new Block(Material.IRON).setHardness(1.2f), "blockRefinedSubstrate")
    blockCyberwood = registerBlock(registry, new BlockCyberwood(), "blockCyberwood").registerOre("logWood")
    blockCyberleaf = registerBlock(registry, new BlockCyberleaf(), "blockCyberleaf").registerOre("treeLeaves")

    blockNanoweave = registerBlock(registry, new BlockNanoweave(), "blockNanoweave").registerOre("oreNanoweave")
    blockRiftiron = registerBlock(registry, new BlockRiftiron(), "blockRiftiron").registerOre("oreRiftiron")
    blockPhasemetal = registerBlock(registry, new BlockPhasemetal(), "blockPhasemetal").registerOre("orePhasemetal")
    blockRedstonereplacement = registerBlock(registry, new BlockRedstonereplacement(), "blockRedstonereplacement").registerOre("oreRedstonereplacement")
    blockLapisreplacement = registerBlock(registry, new BlockLapisreplacement(), "blockLapisreplacement").registerOre("oreLapisreplacement")
    blockDiamondreplacement = registerBlock(registry, new BlockDiamondreplacement(), "blockDiamondreplacement").registerOre("oreDiamondreplacement")

    blockCrystals = registerBlock(registry, new BlockCrystalsWorldgen(), "crystalCluster")
    blockNanoFurnace = registerBlock(registry, new BlockNanoFurnace, "blockNanoFurnace")
    blockNaniteInfuser = registerBlock(registry, new BlockNaniteInfuser, "blockNaniteInfuser")
    blockFrame = registerBlock(registry, new BlockFrame(), "blockFrame")
    blockNaniteHiveSmall = registerBlock(registry, new BlockNaniteHiveSmall(), "blockNaniteHive_small")
    blockNaniteRepository = registerBlock(registry, new BlockNaniteRepository(), "blockNaniteRepository")
    blockItemRepository = registerBlock(registry, new BlockItemRepository(), "blockItemRepository")
    blockFluidRepository = registerBlock(registry, new BlockFluidRepository(), "blockFluidRepository")
    blockCrystalMount = registerBlock(registry, new BlockCrystalMount(), "blockCrystalMount")
    blockPowerPedestal = registerBlock(registry, new BlockPowerPedestal(), "blockPowerPedestal")
    blockCrystalChargingArray = registerBlock(registry, new BlockCrystalChargingArray(), "blockCrystalChargingArray")
    blockCrystalStorageArray = registerBlock(registry, new BlockCrystalStorageArray(), "blockCrystalStorageArray")
    blockCrystalHeatExchanger = registerBlock(registry, new BlockCrystalHeatExchanger(), "blockCrystalHeatExchanger")
    blockNaniteExtractor = registerBlock(registry, new BlockNaniteExtractor(), "blockNaniteExtractor")
    blockDemolisher = registerBlock(registry, new BlockDemolisher(), "blockDemolisher")
    blockConduit = registerBlock(registry, new BlockConduit(), "blockConduit")
    //blockGerminationChamber = registerBlock(registry, new BlockGerminationChamber(), "blockGerminationChamber")
    blockGlowStick = registerBlock(registry, new BlockGlowStick(), "blockGlowStick")

    //tests

    testBlock = registerBlock(registry, new BlockTest, "testBlock")
    testNetworkBlock = registerBlock(registry, new BlockNetworkTest, "testNetworkBlock")
    testTaskProvider = registerBlock(registry, new BlockTaskProviderTest, "testTaskProvider")
    testWorkerProvider = registerBlock(registry, new BlockWorkerProviderTest, "testWorkerProvider")

    blockCallbacks.foreach(_ ())
    blockCallbacks.clear()
  }

  def registerBlock[T <: Block](registry: IForgeRegistry[Block], block: T, name: String): T = {
    block.setCreativeTab(Femtocraft.tab).setRegistryName(Femtocraft.ID.toLowerCase, name).setUnlocalizedName(name)
    registry.register(block)
    itemBlocksToRegister += ((block, name))
    block
  }

  def init(): Unit = {
    blockSubstrate.registerModel()
    blockRefinedSubstrate.registerModel()
    blockFrame.registerModel()
    blockCyberleaf.registerModel()
    blockCyberwood.registerModel()
    blockNanoweave.registerModel()
    blockRiftiron.registerModel()
    blockPhasemetal.registerModel()
    blockRedstonereplacement.registerModel()
    blockLapisreplacement.registerModel()
    blockDiamondreplacement.registerModel()
    blockCrystalMount.registerModel()
    blockPowerPedestal.registerModel()
    blockNaniteHiveSmall.registerModel()
    blockCrystals.registerModel()
    blockNaniteRepository.registerModel()
    blockItemRepository.registerModel()
    blockFluidRepository.registerModel()
    blockNanoFurnace.registerModel()
    blockNaniteExtractor.registerModel()
    blockNaniteInfuser.registerModel()
    blockDemolisher.registerModel()
    blockCrystalChargingArray.registerModel()
    blockCrystalStorageArray.registerModel()
    blockCrystalHeatExchanger.registerModel()
    blockConduit.registerModel()
    //blockGerminationChamber.registerModel()
  }

  def postInit(): Unit = {

  }

  def registerItemBlocks(registry: IForgeRegistry[Item]): Unit = {
    itemBlocksToRegister.foreach { blockname =>
      registry.register(new ItemBlock(blockname._1).setRegistryName(blockname._1.getRegistryName).setUnlocalizedName(blockname._2))
    }
    itemBlocksToRegister.clear()

    oresToRegister.foreach { blockname =>
      OreDictionary.registerOre(blockname._2, blockname._1)
    }
    oresToRegister.clear()
  }

  implicit class BlockHelpers[T <: Block](block: T) {
    def registerOre(name: String): T = {
      oresToRegister += ((block, name))
      block
    }

    def registerModel(): Unit = {
      Femtocraft.proxy.onRegisterBlock(block, block.getUnlocalizedName.substring(5))
    }
  }

}
