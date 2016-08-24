package com.itszuvalex.femtocraft


import com.itszuvalex.femtocraft.cyber.block._
import com.itszuvalex.femtocraft.industry.block._
import com.itszuvalex.femtocraft.logistics.block.BlockItemRepository
import com.itszuvalex.femtocraft.logistics.test.{BlockNetworkTest, BlockTaskProviderTest, BlockWorkerProviderTest}
import com.itszuvalex.femtocraft.nanite.block.{BlockNaniteExtractor, BlockNaniteHiveSmall}
import com.itszuvalex.femtocraft.power.block._
import com.itszuvalex.femtocraft.power.test._
import com.itszuvalex.femtocraft.worldgen.block.BlockCrystalsWorldgen
import net.minecraft.block.Block
import net.minecraft.item.ItemBlock
import net.minecraftforge.fml.common.registry.GameRegistry
import net.minecraftforge.oredict.OreDictionary

/**
  * Created by Christopher Harris (Itszuvalex) on 5/3/15.
  */
object FemtoBlocks {
  //Cyber
  var blockCyberweave: Block = _
  var blockCyberwood : Block = _
  var blockCyberleaf : Block = _

  var blockCrystals: Block = _


  var blockArcFurnace            : Block = _
  var blockCrystallizationChamber: Block = _
  var blockCentrifuge            : Block = _
  var blockMaterialProcessor     : Block = _
  var blockGrowthChamber         : Block = _
  var blockBioBeacon             : Block = _
  var blockCondensationArray     : Block = _
  var blockCybermatDisintegrator : Block = _
  var blockGraspingVines         : Block = _
  var blockLashingVines          : Block = _
  var blockMetabolicConverter    : Block = _
  var blockPhotosynthesisTower   : Block = _
  var blockSporeDistributor      : Block = _
  var blockItemRepository        : Block = _
  var blockNanoFurnace           : Block = _


  var blockFrame                 : Block = _
  var blockCyberBase             : Block = _
  var blockCyberMachineInProgress: Block = _

  var blockNaniteHiveSmall: Block = _
  var blockCrystalMount   : Block = _
  var blockPowerPedestal  : Block = _

  var blockNaniteExtractor: Block = _

  var blockPowerSink     : Block = _
  var blockPowerGenerator: Block = _

  var blockGlowStick: Block = _

  //Tests

  var testBlock       : Block = _
  var testNetworkBlock: Block = _


  var testDiffusionNode      : Block = _
  var testDiffusionTargetNode: Block = _
  var testDirectNode         : Block = _
  var testGenerationNode     : Block = _
  var testTransferNode       : Block = _

  var testTaskProvider  : Block = _
  var testWorkerProvider: Block = _


  def preInit(): Unit = {
    blockCyberweave = registerBlock(new BlockCyberweave(), "blockCyberweave").registerOre("cyberweave")
    blockCyberwood = registerBlock(new BlockCyberwood(), "blockCyberwood").registerOre("logWood")
    blockCyberleaf = registerBlock(new BlockCyberleaf(), "blockCyberleaf").registerOre("treeLeaves")
    blockCrystals = registerBlock(new BlockCrystalsWorldgen(), "crystalCluster")
    blockArcFurnace = registerBlock(new BlockArcFurnace(), "blockArcFurnace")
    blockCrystallizationChamber = registerBlock(new BlockCrystallizationChamber(), "blockCrystallizationChamber")
    blockCentrifuge = registerBlock(new BlockCentrifuge(), "blockCentrifuge")
    blockMaterialProcessor = registerBlock(new BlockMaterialProcessor(), "blockMaterialProcessor")
    blockGrowthChamber = registerBlock(new BlockGrowthChamber(), "blockGrowthChamber")
    blockBioBeacon = registerBlock(new BlockBioBeacon(), "blockBioBeacon")
    blockCondensationArray = registerBlock(new BlockCondensationArray(), "blockCondensationArray")
    blockCybermatDisintegrator = registerBlock(new BlockCybermatDisintegrator(), "blockCybermatDisintegrator")
    blockGraspingVines = registerBlock(new BlockGraspingVines(), "blockGraspingVines")
    blockLashingVines = registerBlock(new BlockLashingVines(), "blockLashingVines")
    blockMetabolicConverter = registerBlock(new BlockMetabolicConverter(), "blockMetabolicConverter")
    blockPhotosynthesisTower = registerBlock(new BlockPhotosynthesisTower(), "blockPhotosynthesisTower")
    blockSporeDistributor = registerBlock(new BlockSporeDistributor(), "blockSporeDistributor")
    blockNanoFurnace = registerBlock(new BlockNanoFurnace, "blockNanoFurnace")
    blockFrame = registerBlock(new BlockFrame(), "blockFrame")
    blockCyberBase = registerBlock(new BlockCyberBase(), "blockCyberBase")
    blockCyberMachineInProgress = registerBlock(new BlockCyberMachineInProgress(), "blockInProgressMachine").setBlockUnbreakable().setResistance(Float.MaxValue / 3f)
    blockNaniteHiveSmall = registerBlock(new BlockNaniteHiveSmall(), "blockNaniteHive_small")
    blockItemRepository = registerBlock(new BlockItemRepository(), "blockItemRepository")
    blockCrystalMount = registerBlock(new BlockCrystalMount(), "blockCrystalMount")
    blockPowerPedestal = registerBlock(new BlockPowerPedestal(), "blockPowerPedestal")
    blockNaniteExtractor = registerBlock(new BlockNaniteExtractor, "blockNaniteExtractor")
    blockPowerSink = registerBlock(new BlockPowerSink(), "blockPowerSink")
    blockPowerGenerator = registerBlock(new BlockPowerGenerator(), "blockPowerGenerator")
    blockGlowStick = registerBlock(new BlockGlowStick(), "blockGlowStick")

    //tests

    testBlock = registerBlock(new BlockTest, "testBlock")
    testNetworkBlock = registerBlock(new BlockNetworkTest, "testNetworkBlock")
    testDiffusionNode = registerBlock(new BlockDiffusionNodeTest, "testDiffusionNode")
    testDiffusionTargetNode = registerBlock(new BlockDiffusionTargetNodeTest, "testDiffusionTargetNode")
    testDirectNode = registerBlock(new BlockDirectNodeTest, "testDirectNode")
    testGenerationNode = registerBlock(new BlockGenerationNodeTest, "testGenerationNode")
    testTransferNode = registerBlock(new BlockTransferNodeTest, "testTransferNode")
    testTaskProvider = registerBlock(new BlockTaskProviderTest, "testTaskProvider")
    testWorkerProvider = registerBlock(new BlockWorkerProviderTest, "testWorkerProvider")
  }

  def init(): Unit = {
    blockCyberweave.registerModel()
    blockCyberleaf.registerModel()
    blockCyberwood.registerModel()
    blockCrystalMount.registerModel()
    blockPowerPedestal.registerModel()
    blockNaniteHiveSmall.registerModel()
    blockPowerSink.registerModel()
    blockCrystals.registerModel()
    blockNanoFurnace.registerModel()
    blockNaniteExtractor.registerModel()
  }

  def postInit(): Unit = {

  }

  def registerBlock[T <: Block](block: T, name: String): T = {
    block.setCreativeTab(Femtocraft.tab).setRegistryName(Femtocraft.ID.toLowerCase, name).setUnlocalizedName(name)
    GameRegistry.register(block)
    GameRegistry.register(new ItemBlock(block).setRegistryName(block.getRegistryName).setUnlocalizedName(name))
    block
  }

  implicit class BlockHelpers[T <: Block](block: T) {
    def registerOre(name: String): T = {
      OreDictionary.registerOre(name, block)
      block
    }

    def registerModel() = {
      Femtocraft.proxy.onRegisterBlock(block, block.getUnlocalizedName.substring(5))
    }
  }

}
