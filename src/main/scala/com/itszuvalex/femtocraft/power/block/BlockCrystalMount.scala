package com.itszuvalex.femtocraft.power.block

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.femtocraft.proxy.ProxyCommon
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.core.traits.block.DroppableInventory
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.{IBlockAccess, World}

/**
  * Created by Christopher on 8/30/2015.
  */
class BlockCrystalMount extends TileContainer(Material.IRON) with DroppableInventory {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileCrystalMount

  override def isNormalCube(state: IBlockState, world: IBlockAccess, pos: BlockPos): Boolean = false

  override def isOpaqueCube(state: IBlockState): Boolean = false

  override def randomDisplayTick(stateIn: IBlockState, worldIn: World, pos: BlockPos, rand: Random): Unit = {
    worldIn.getTileEntity(pos) match {
      case mount: TileCrystalMount =>
        if (mount.getCrystalStack != null && !mount.getCrystalStack.isEmpty)
          Femtocraft.proxy.spawnParticle(worldIn, ProxyCommon.PARTICLE_POWER,
            pos.getX + .5 + (rand.nextDouble() * .2 - .1),
            pos.getY + .5 + (rand.nextDouble() * .2 - .1),
            pos.getZ + .5 + (rand.nextDouble() * .2 - .1),
            mount.getCapability(Capabilities.COLORABLE, null).toInt)
      case _ =>
    }
  }
}
