package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.industry.multiblock.MultiblockMaterialProcessor
import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
object BlockMaterialProcessor {
  var breaking = false
}

class BlockMaterialProcessor extends TileContainer(Material.iron) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileMaterialProcessor

  override def isOpaqueCube: Boolean = false

  override def renderAsNormalBlock(): Boolean = false

  override def getRenderBlockPass: Int = -1

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, metadata: Int): Unit = {
    if (!BlockMaterialProcessor.breaking) {
      world.getTileEntity(x, y, z) match {
        case null =>
        case processor: TileMaterialProcessor if processor.isController =>
          BlockMaterialProcessor.breaking = true
          FrameMultiblockRegistry.getMultiblock(MultiblockMaterialProcessor.name) match {
            case Some(multi) =>
              multi.onMultiblockBroken(world, x, y, z)
            case _ =>
          }
          BlockMaterialProcessor.breaking = false
        case processor: TileMaterialProcessor if processor.isValidMultiBlock =>
          world.setBlockToAir(processor.info.x, processor.info.y, processor.info.z)
        case _ =>
      }
    }
    super.breakBlock(world, x, y, z, block, metadata)
  }
}
