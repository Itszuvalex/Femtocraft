package com.itszuvalex.femtocraft.nanite.block

import com.itszuvalex.femtocraft.nanite.tile.TileNaniteHiveSmall
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
 * Created by Christopher on 8/29/2015.
 */
class BlockNaniteHiveSmall extends TileContainer(Material.iron) {
  override def isOpaqueCube = false

  override def renderAsNormalBlock() = false

  override def getRenderType = RenderIDs.naniteHiveSmallID

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, p_149749_6_ : Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case hive: TileNaniteHiveSmall => hive.onBlockBreak()
      case _                         =>
    }
    super.breakBlock(world, x, y, z, block, p_149749_6_)
  }

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileNaniteHiveSmall
}

