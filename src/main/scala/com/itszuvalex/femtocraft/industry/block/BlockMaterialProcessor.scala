package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.industry.FrameMultiblockRegistry
import com.itszuvalex.femtocraft.industry.multiblock.MultiblockMaterialProcessor
import com.itszuvalex.femtocraft.industry.tile.TileMaterialProcessor
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Christopher Harris (Itszuvalex) on 8/28/15.
  */
object BlockMaterialProcessor {
  var breaking = false
}

class BlockMaterialProcessor extends TileContainer(Material.IRON) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileMaterialProcessor

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def breakBlock(world: World, pos: BlockPos, state: IBlockState): Unit = {
    //Gets entered
    if (!BlockMaterialProcessor.breaking) {
      world.getTileEntity(pos) match {
        case null =>
        //If not above
        case processor: TileMaterialProcessor if processor.isController =>
          BlockMaterialProcessor.breaking = true
          FrameMultiblockRegistry.getMultiblock(MultiblockMaterialProcessor.name) match {
            case Some(multi) =>
              multi.onMultiblockBroken(new Loc4(world, pos))
            case _ =>
          }
          BlockMaterialProcessor.breaking = false
        //Break the controller block to trigger above
        case processor: TileMaterialProcessor if processor.isValidMultiBlock =>
          world.setBlockToAir(processor.info.cLoc.getPos)
        case _ =>
      }
    }
    else {
      world.getTileEntity(pos) match {
        case null =>
        case tile: TileMaterialProcessor => tile.onBlockBreak()
      }
      super.breakBlock(world, pos, state)
    }
  }
}
