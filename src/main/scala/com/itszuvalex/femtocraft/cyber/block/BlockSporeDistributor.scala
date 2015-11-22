package com.itszuvalex.femtocraft.cyber.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.cyber.tile.TileSporeDistributor
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Alex on 30.09.2015.
  */
class BlockSporeDistributor extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileSporeDistributor

  override def renderAsNormalBlock = true

  override def getRenderBlockPass = 2

  override def isOpaqueCube = true

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, par6: Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case te: TileSporeDistributor =>
        te.onBlockBreak()
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, par6)
  }
}
