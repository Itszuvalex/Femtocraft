package com.itszuvalex.femtocraft.cyber.block

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.util.AxisAlignedBB
import net.minecraft.world.World

/**
 * Created by Alex on 01.10.2015.
 */
class BlockInProgressMachine extends Block(Material.iron) {
  override def renderAsNormalBlock = false
  override def getRenderBlockPass = 2
  override def isOpaqueCube = false

  override def getSelectedBoundingBoxFromPool(world: World, x: Int, y: Int, z: Int) = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0)
}
