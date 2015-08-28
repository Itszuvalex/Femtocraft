package com.itszuvalex.femtocraft

import java.util.Random

import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.world.World

/**
 * Created by Christopher Harris (Itszuvalex) on 5/18/15.
 */

class BlockTest() extends Block(Material.iron) {

  setCreativeTab(Femtocraft.tab)
  setBlockName("blockTest")
  setHardness(1.0f)
  setStepSound(Block.soundTypeMetal)
  setTickRandomly(true)

  override def randomDisplayTick(par1World: World, x: Int, y: Int, z: Int, par5Random: Random) {
    val spawnX = x + getBlockBoundsMinX - 1 + 2 * par5Random.nextFloat * (getBlockBoundsMaxX - getBlockBoundsMinX)
    val spawnY = y + getBlockBoundsMinY - 1 + 2 * par5Random.nextFloat * (getBlockBoundsMaxY - getBlockBoundsMinY)
    val spawnZ = z + getBlockBoundsMinZ - 1 + 2 * par5Random.nextFloat * (getBlockBoundsMaxZ - getBlockBoundsMinZ)
//    Femtocraft.proxy.spawnParticle(par1World, "nanitesBlue", spawnX, spawnY, spawnZ,)
  }
}

