package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.tile.TileArcFurnace
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
class BlockArcFurnace extends TileContainer(Material.iron) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileArcFurnace

  override def isOpaqueCube: Boolean = false

  override def renderAsNormalBlock(): Boolean = false

  override def getRenderBlockPass: Int = -1
}
