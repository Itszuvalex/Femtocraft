package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.{Femtocraft, FemtoBlocks}
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 1/30/2016.
  */
class BlockNetworkTest extends TileContainer(Material.iron){
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileNetworkTest
}
