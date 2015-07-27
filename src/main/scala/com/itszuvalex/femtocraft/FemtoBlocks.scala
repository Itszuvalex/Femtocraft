package com.itszuvalex.femtocraft

import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.block.Block

/**
 * Created by Christopher Harris (Itszuvalex) on 5/3/15.
 */
object FemtoBlocks {
  var testBlock: Block = null

  def preInit(): Unit = {
    testBlock = new BlockTest
    GameRegistry.registerBlock(testBlock, "testBlock")
  }

  def init(): Unit = {

  }

  def postInit(): Unit = {

  }

}
