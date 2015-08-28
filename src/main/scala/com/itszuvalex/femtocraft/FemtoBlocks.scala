package com.itszuvalex.femtocraft


import com.itszuvalex.femtocraft.core.Cyber.block.{BlockCyberleaf, BlockCyberweave, BlockCyberwood}
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


  var testBlock: Block = null

  var crystalsWorldgen: Block = null

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

    testBlock = new BlockTest
    GameRegistry.registerBlock(testBlock, "testBlock")

    crystalsWorldgen = new BlockCrystalsWorldgen
    crystalsWorldgen.setBlockName("crystalCluster")
    GameRegistry.registerBlock(crystalsWorldgen, "crystalCluster")

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
