package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.tile.TileSingleArray
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 3/9/16.
  */
class BlockSingleArray extends TileContainer(Material.IRON) {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileSingleArray
}
