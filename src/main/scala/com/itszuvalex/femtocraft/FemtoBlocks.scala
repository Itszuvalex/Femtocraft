package com.itszuvalex.femtocraft


import com.itszuvalex.femtocraft.core.Cyber.block.{BlockCyberleaf, BlockCyberweave, BlockCyberwood}
import com.itszuvalex.femtocraft.industry.block.{BlockArcFurnace, BlockCentrifuge, BlockCrystallizationChamber}
import com.itszuvalex.femtocraft.logistics.test.{BlockTaskProviderTest, BlockWorkerProviderTest}
import com.itszuvalex.femtocraft.power.test._
import com.itszuvalex.femtocraft.worldgen.block.BlockCrystalsWorldgen
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.block.Block

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

  //Tests

  var testBlock: Block = null


  var testDiffusionNode      : Block = null
  var testDiffusionTargetNode: Block = null
  var testDirectNode         : Block = null
  var testGenerationNode     : Block = null
  var testTransferNode       : Block = null

  var testTaskProvider  : Block = null
  var testWorkerProvider: Block = null


  def preInit(): Unit = {
    blockCyberweave = new BlockCyberweave().setCreativeTab(Femtocraft.tab).setBlockName("blockCyberweave")
    GameRegistry.registerBlock(blockCyberweave, "blockCyberweave")

    blockCyberwood = new BlockCyberwood().setCreativeTab(Femtocraft.tab).setBlockName("blockCyberwood")
    GameRegistry.registerBlock(blockCyberwood, "blockCyberwood")

    blockCyberleaf = new BlockCyberleaf().setCreativeTab(Femtocraft.tab).setBlockName("blockCyberleaf")
    GameRegistry.registerBlock(blockCyberleaf, "blockCyberleaf")



    blockCrystals = new BlockCrystalsWorldgen
    blockCrystals.setBlockName("crystalCluster")
    GameRegistry.registerBlock(blockCrystals, "crystalCluster")

    blockArcFurnace = new BlockArcFurnace
    blockArcFurnace.setBlockName("blockArcFurnace")
    GameRegistry.registerBlock(blockArcFurnace, "blockArcFurnace")

    blockCrystallizationChamber = new BlockCrystallizationChamber
    blockCrystallizationChamber.setBlockName("blockCrystallizationChamber")
    GameRegistry.registerBlock(blockCrystallizationChamber, "blockCrystallizationChamber")

    blockCentrifuge = new BlockCentrifuge
    blockCentrifuge.setBlockName("blockCentrifuge")
    GameRegistry.registerBlock(blockCentrifuge, "blockCentrifuge")
    //tests

    testBlock = new BlockTest
    GameRegistry.registerBlock(testBlock, "testBlock")


    testDiffusionNode = new BlockDiffusionNodeTest
    testDiffusionNode.setBlockName("testDiffusionNode")
    GameRegistry.registerBlock(testDiffusionNode, "testDiffusionNode")
    testDiffusionTargetNode = new BlockDiffusionTargetNodeTest
    testDiffusionTargetNode.setBlockName("testDiffusionTargetNode")
    GameRegistry.registerBlock(testDiffusionTargetNode, "testDiffusionTargetNode")
    testDirectNode = new BlockDirectNodeTest
    testDirectNode.setBlockName("testDirectNode")
    GameRegistry.registerBlock(testDirectNode, "testDirectNode")
    testGenerationNode = new BlockGenerationNodeTest
    testGenerationNode.setBlockName("testGenerationNode")
    GameRegistry.registerBlock(testGenerationNode, "testGenerationNode")
    testTransferNode = new BlockTransferNodeTest
    testTransferNode.setBlockName("testTransferNode")
    GameRegistry.registerBlock(testTransferNode, "testTransferNode")


    testTaskProvider = new BlockTaskProviderTest
    testTaskProvider.setBlockName("testTaskProvider")
    GameRegistry.registerBlock(testTaskProvider, "testTaskProvider")
    testWorkerProvider = new BlockWorkerProviderTest
    testWorkerProvider.setBlockName("testWorkerProvider")
    GameRegistry.registerBlock(testWorkerProvider, "testWorkerProvider")
  }

  def init(): Unit = {

  }

  def postInit(): Unit = {

  }

}
