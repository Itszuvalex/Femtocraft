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
import net.minecraft.item.ItemBlock
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
    blockFrame = registerBlock(new BlockFrame(), "blockFrame")
    blockCyberBase = registerBlock(new BlockCyberBase(), "blockCyberBase")
    blockCyberMachineInProgress = registerBlock(new BlockCyberMachineInProgress(), "blockInProgressMachine").setBlockUnbreakable().setResistance(Float.MaxValue / 3f)
    blockNaniteHiveSmall = registerBlock(new BlockNaniteHiveSmall(), "blockNaniteHive_small")
    blockItemRepository = registerBlock(new BlockItemRepository(), "blockItemRepository")
    blockCrystalMount = registerBlock(new BlockCrystalMount(), "blockCrystalMount")
    blockPowerPedestal = registerBlock(new BlockPowerPedestal(), "blockPowerPedestal")
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
