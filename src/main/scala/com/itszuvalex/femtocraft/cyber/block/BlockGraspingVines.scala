package com.itszuvalex.femtocraft.cyber.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.tile.TileGraspingVines
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Alex on 30.09.2015.
  */
class BlockGraspingVines extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileGraspingVines

  override def renderAsNormalBlock = false

  override def getRenderBlockPass = 2

  override def isOpaqueCube = false

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, par6: Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case te: TileGraspingVines =>
        te.onBlockBreak()
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, par6)
  }
}
