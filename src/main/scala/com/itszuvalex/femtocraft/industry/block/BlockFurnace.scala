package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.industry.multiblock.MultiblockFurnace
import com.itszuvalex.femtocraft.industry.tile.TileFurnace
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
object BlockFurnace {
  var breaking = false
}

class BlockFurnace extends TileContainer(Material.iron) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileFurnace

  override def isOpaqueCube: Boolean = false

  override def renderAsNormalBlock(): Boolean = false

  override def getRenderBlockPass: Int = -1

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, metadata: Int): Unit = {
    if (!BlockFurnace.breaking) {
      world.getTileEntity(x, y, z) match {
        case null =>
        case furnace: TileFurnace if furnace.isController =>
          BlockFurnace.breaking = true
          FrameMultiblockRegistry.getMultiblock(MultiblockFurnace.name) match {
            case Some(multi) =>
              multi.onMultiblockBroken(world, x, y, z)
            case _ =>
          }
          BlockFurnace.breaking = false
        case furnace: TileFurnace if furnace.isValidMultiBlock =>
          world.setBlockToAir(furnace.info.x, furnace.info.y, furnace.info.z)
        case _ =>
      }
    }
    super.breakBlock(world, x, y, z, block, metadata)
  }
}
