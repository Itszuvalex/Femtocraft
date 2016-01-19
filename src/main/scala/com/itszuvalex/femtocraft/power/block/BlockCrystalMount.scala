package com.itszuvalex.femtocraft.power.block

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.ICrystalMount
import com.itszuvalex.femtocraft.power.tile.{TilePowerPedestal, TileCrystalMount}
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher on 8/30/2015.
  */
class BlockCrystalMount extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileCrystalMount

  override def renderAsNormalBlock(): Boolean = false

  override def getRenderBlockPass: Int = 2

  override def isOpaqueCube: Boolean = false

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, metadata: Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case i: TileCrystalMount =>
        val random = new Random()
        (0 until i.getSizeInventory).map(i.getStackInSlotOnClosing).foreach(InventoryUtils.dropItem(_, world, x, y, z, random))
        i.onBlockBreak()
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, metadata)
  }

  override def randomDisplayTick(p_149734_1_ : World, p_149734_2_ : Int, p_149734_3_ : Int, p_149734_4_ : Int, p_149734_5_ : Random): Unit = {
    p_149734_1_.getTileEntity(p_149734_2_, p_149734_3_, p_149734_4_) match {
      case mount: ICrystalMount =>
        if (mount.getCrystalStack != null)
          Femtocraft.proxy.spawnParticle(p_149734_1_, "power",
                                         p_149734_2_ + .5 + (p_149734_5_.nextDouble() * .2 - .1),
                                         p_149734_3_ + .5 + (p_149734_5_.nextDouble() * .2 - .1),
                                         p_149734_4_ + .5 + (p_149734_5_.nextDouble() * .2 - .1),
                                         mount.getColor)
      case _ =>
    }
  }
  override def onPostBlockPlaced(p_149714_1_ : World, p_149714_2_ : Int, p_149714_3_ : Int, p_149714_4_ : Int, p_149714_5_ : Int): Unit = {
    p_149714_1_.getTileEntity(p_149714_2_, p_149714_3_, p_149714_4_) match {
      case i: TileCrystalMount =>
          i.onPostBlockPlaced()
      case _ =>
    }
    super.onPostBlockPlaced(p_149714_1_, p_149714_2_, p_149714_3_, p_149714_4_, p_149714_5_)
  }
}
