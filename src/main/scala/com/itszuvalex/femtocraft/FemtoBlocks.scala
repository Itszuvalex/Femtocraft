package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.Initializable
import com.itszuvalex.femtocraft.power.test._
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.block.Block

/**
 * Created by Christopher Harris (Itszuvalex) on 5/3/15.
 */
object FemtoBlocks extends Initializable {
  var testBlock: Block = null

  var testDiffusionNode      : Block = null
  var testDiffusionTargetNode: Block = null
  var testDirectNode         : Block = null
  var testGenerationNode     : Block = null
  var testTransferNode       : Block = null

  override def preInit(): Unit = {
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
  }

  override def init(): Unit = {

  }

  override def postInit(): Unit = {

  }

}
