package com.itszuvalex.femtocraft


import com.itszuvalex.femtocraft.cyber.block._
import com.itszuvalex.femtocraft.industry.block._
import com.itszuvalex.femtocraft.logistics.block.BlockItemRepository
import com.itszuvalex.femtocraft.logistics.test.{BlockNetworkTest, BlockTaskProviderTest, BlockWorkerProviderTest}
import com.itszuvalex.femtocraft.nanite.block.BlockNaniteHiveSmall
import com.itszuvalex.femtocraft.power.block._
import com.itszuvalex.femtocraft.power.test._
import com.itszuvalex.femtocraft.worldgen.block.BlockCrystalsWorldgen
import net.minecraft.block.Block
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary

/**
  * Created by Christopher Harris (Itszuvalex) on 5/3/15.
  */
object FemtoBlocks {
  //Cyber
  var blockCyberweave: Block = null
  var blockCyberwood : Block = null
  var blockCyberleaf : Block = null

  var blockCrystals: Block = null


  var blockArcFurnace            : Block = null
  var blockCrystallizationChamber: Block = null
  var blockCentrifuge            : Block = null
  var blockMaterialProcessor     : Block = null
  var blockGrowthChamber         : Block = null
  var blockBioBeacon             : Block = null
  var blockCondensationArray     : Block = null
  var blockCybermatDisintegrator : Block = null
  var blockGraspingVines         : Block = null
  var blockLashingVines          : Block = null
  var blockMetabolicConverter    : Block = null
  var blockPhotosynthesisTower   : Block = null
  var blockSporeDistributor      : Block = null
  var blockItemRepository        : Block = null


  var blockFrame                 : Block = null
  var blockCyberBase             : Block = null
  var blockCyberMachineInProgress: Block = null

  var blockNaniteHiveSmall: Block = null
  var blockCrystalMount   : Block = null
  var blockPowerPedestal  : Block = null

  var blockPowerSink     : Block = null
  var blockPowerGenerator: Block = null

  var blockGlowStick: Block = null

  //Tests

  var testBlock       : Block = null
  var testNetworkBlock: Block = null


  var testDiffusionNode      : Block = null
  var testDiffusionTargetNode: Block = null
  var testDirectNode         : Block = null
  var testGenerationNode     : Block = null
  var testTransferNode       : Block = null

  var testTaskProvider  : Block = null
  var testWorkerProvider: Block = null


  def preInit(): Unit = {
    blockCyberweave = new BlockCyberweave().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockCyberweave")
    GameRegistry.registerBlock(blockCyberweave, "blockCyberweave")
    OreDictionary.registerOre("cyberweave", blockCyberweave)

    blockCyberwood = new BlockCyberwood().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockCyberwood")
    GameRegistry.registerBlock(blockCyberwood, "blockCyberwood")
    OreDictionary.registerOre("logWood", blockCyberwood)

    blockCyberleaf = new BlockCyberleaf().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockCyberleaf")
    GameRegistry.registerBlock(blockCyberleaf, "blockCyberleaf")
    OreDictionary.registerOre("treeLeaves", blockCyberleaf)

    blockCrystals = new BlockCrystalsWorldgen().setCreativeTab(Femtocraft.tab).setUnlocalizedName("crystalCluster")
    GameRegistry.registerBlock(blockCrystals, "crystalCluster")

    blockArcFurnace = new BlockArcFurnace().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockArcFurnace")
    GameRegistry.registerBlock(blockArcFurnace, "blockArcFurnace")

    blockCrystallizationChamber = new BlockCrystallizationChamber().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockCrystallizationChamber")
    GameRegistry.registerBlock(blockCrystallizationChamber, "blockCrystallizationChamber")

    blockCentrifuge = new BlockCentrifuge().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockCentrifuge")
    GameRegistry.registerBlock(blockCentrifuge, "blockCentrifuge")

    blockMaterialProcessor = new BlockMaterialProcessor().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockMaterialProcessor")
    GameRegistry.registerBlock(blockMaterialProcessor, "blockMaterialProcessor")

    blockGrowthChamber = new BlockGrowthChamber().setUnlocalizedName("blockGrowthChamber")
    GameRegistry.registerBlock(blockGrowthChamber, "blockGrowthChamber")

    blockBioBeacon = new BlockBioBeacon().setUnlocalizedName("blockBioBeacon")
    GameRegistry.registerBlock(blockBioBeacon, "BlockBioBeacon")

    blockCondensationArray = new BlockCondensationArray().setUnlocalizedName("blockCondensationArray")
    GameRegistry.registerBlock(blockCondensationArray, "blockCondensationArray")

    blockCybermatDisintegrator = new BlockCybermatDisintegrator().setUnlocalizedName("blockCybermatDisintegrator")
    GameRegistry.registerBlock(blockCybermatDisintegrator, "blockCybermatDisintegrator")

    blockGraspingVines = new BlockGraspingVines().setUnlocalizedName("blockGraspingVines")
    GameRegistry.registerBlock(blockGraspingVines, "blockGraspingVines")

    blockLashingVines = new BlockLashingVines().setUnlocalizedName("blockLashingVines")
    GameRegistry.registerBlock(blockLashingVines, "blockLashingVines")

    blockMetabolicConverter = new BlockMetabolicConverter().setUnlocalizedName("blockMetabolicConverter")
    GameRegistry.registerBlock(blockMetabolicConverter, "blockMetabolicConverter")

    blockPhotosynthesisTower = new BlockPhotosynthesisTower().setUnlocalizedName("blockPhotosynthesisTower")
    GameRegistry.registerBlock(blockPhotosynthesisTower, "blockPhotosynthesisTower")

    blockSporeDistributor = new BlockSporeDistributor().setUnlocalizedName("blockSporeDistributor")
    GameRegistry.registerBlock(blockSporeDistributor, "blockSporeDistributor")

    blockFrame = new BlockFrame().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockFrame")
    GameRegistry.registerBlock(blockFrame, "blockFrame")

    blockCyberBase = new BlockCyberBase().setUnlocalizedName("blockCyberBase")
    GameRegistry.registerBlock(blockCyberBase, "blockCyberBase")

    blockCyberMachineInProgress = new BlockCyberMachineInProgress().setUnlocalizedName("blockInProgressMachine").setBlockUnbreakable().setResistance(Float.MaxValue / 3f)
    GameRegistry.registerBlock(blockCyberMachineInProgress, "blockInProgressMachine")

    blockNaniteHiveSmall = new BlockNaniteHiveSmall().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockNaniteHive_small")
    GameRegistry.registerBlock(blockNaniteHiveSmall, "blockNaniteHive_small")

    blockItemRepository = new BlockItemRepository().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockItemRepository")
    GameRegistry.registerBlock(blockItemRepository, "blockItemRepository")

    blockCrystalMount = new BlockCrystalMount().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockCrystalMount")
    GameRegistry.registerBlock(blockCrystalMount, "blockCrystalMount")

    blockPowerPedestal = new BlockPowerPedestal().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockPowerPedestal")
    GameRegistry.registerBlock(blockPowerPedestal, "blockPowerPedestal")

    blockPowerSink = new BlockPowerSink().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockPowerSink")
    GameRegistry.registerBlock(blockPowerSink, "blockPowerSink")

    blockPowerGenerator = new BlockPowerGenerator().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockPowerGenerator")
    GameRegistry.registerBlock(blockPowerGenerator, "blockPowerGenerator")


    blockGlowStick = new BlockGlowStick().setCreativeTab(Femtocraft.tab).setUnlocalizedName("blockGlowStick")
    GameRegistry.registerBlock(blockGlowStick, "blockGlowStick")

    //tests

    testBlock = new BlockTest
    GameRegistry.registerBlock(testBlock, "testBlock")

    testNetworkBlock = new BlockNetworkTest
    GameRegistry.registerBlock(testNetworkBlock, "testNetworkBlock")

    testDiffusionNode = new BlockDiffusionNodeTest
    testDiffusionNode.setUnlocalizedName("testDiffusionNode")
    GameRegistry.registerBlock(testDiffusionNode, "testDiffusionNode")
    testDiffusionTargetNode = new BlockDiffusionTargetNodeTest
    testDiffusionTargetNode.setUnlocalizedName("testDiffusionTargetNode")
    GameRegistry.registerBlock(testDiffusionTargetNode, "testDiffusionTargetNode")
    testDirectNode = new BlockDirectNodeTest
    testDirectNode.setUnlocalizedName("testDirectNode")
    GameRegistry.registerBlock(testDirectNode, "testDirectNode")
    testGenerationNode = new BlockGenerationNodeTest
    testGenerationNode.setUnlocalizedName("testGenerationNode")
    GameRegistry.registerBlock(testGenerationNode, "testGenerationNode")
    testTransferNode = new BlockTransferNodeTest
    testTransferNode.setUnlocalizedName("testTransferNode")
    GameRegistry.registerBlock(testTransferNode, "testTransferNode")


    testTaskProvider = new BlockTaskProviderTest
    testTaskProvider.setUnlocalizedName("testTaskProvider")
    GameRegistry.registerBlock(testTaskProvider, "testTaskProvider")
    testWorkerProvider = new BlockWorkerProviderTest
    testWorkerProvider.setUnlocalizedName("testWorkerProvider")
    GameRegistry.registerBlock(testWorkerProvider, "testWorkerProvider")
  }

  def init(): Unit = {

  }

  def postInit(): Unit = {

  }

}
