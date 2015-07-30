package com.itszuvalex.femtocraft

import com.itszuvalex.femtocraft.core.Initializable
import cpw.mods.fml.common.registry.GameRegistry
import net.minecraft.block.Block

/**
 * Created by Christopher Harris (Itszuvalex) on 5/3/15.
 */
object FemtoBlocks extends Initializable {
  var testBlock: Block = null

  override def preInit(): Unit = {
    testBlock = new BlockTest
    GameRegistry.registerBlock(testBlock, "testBlock")
  }

  override def init(): Unit = {

  }

  override def postInit(): Unit = {

  }

}
