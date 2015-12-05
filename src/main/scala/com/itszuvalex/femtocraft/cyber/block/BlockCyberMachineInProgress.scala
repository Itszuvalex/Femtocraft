package com.itszuvalex.femtocraft.cyber.block

import com.itszuvalex.femtocraft.cyber.tile.TileCyberMachineInProgress
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World

/**
  * Created by Alex on 01.10.2015.
  */
class BlockCyberMachineInProgress extends TileContainer(Material.iron) {
  //  override def renderAsNormalBlock = true
  //
  //  override def getRenderBlockPass = 2
  //
  //  override def isOpaqueCube = true
  //
  //  override def getSelectedBoundingBoxFromPool(world: World, x: Int, y: Int, z: Int) = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0)

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, par6: Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case te: TileCyberMachineInProgress =>
        te.onBlockBreak()
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, par6)
  }

  override def createNewTileEntity(world: World, metadata: Int): TileEntity = new TileCyberMachineInProgress
}
