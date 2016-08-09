package com.itszuvalex.femtocraft.power.block

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.ICrystalMount
import com.itszuvalex.femtocraft.power.tile.TileCrystalMount
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.core.traits.block.DroppableInventory
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemStack
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
      case mount: ICrystalMount =>
      //        if (mount.getCrystalStack != null)
      //          Femtocraft.proxy.spawnParticle(p_149734_1_, "power",
      //            p_149734_2_ + .5 + (p_149734_5_.nextDouble() * .2 - .1),
      //            p_149734_3_ + .5 + (p_149734_5_.nextDouble() * .2 - .1),
      //            p_149734_4_ + .5 + (p_149734_5_.nextDouble() * .2 - .1),
      //            mount.getColor)
      case _ =>
    }
  }

  override def onBlockPlacedBy(worldIn: World, pos: BlockPos, state: IBlockState, placer: EntityLivingBase, stack: ItemStack): Unit = {
    worldIn.getTileEntity(pos) match {
      case i: TileCrystalMount =>
        i.onPostBlockPlaced()
      case _ =>
    }
    super.onBlockPlacedBy(worldIn, pos, state, placer, stack)
  }
}
