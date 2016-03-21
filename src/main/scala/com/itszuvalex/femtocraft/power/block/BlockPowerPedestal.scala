package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.tile.TilePowerPedestal
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher on 8/30/2015.
  */
class BlockPowerPedestal extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TilePowerPedestal

  override def renderAsNormalBlock(): Boolean = false

  override def isOpaqueCube: Boolean = false

  override def getRenderType: Int = RenderIDs.powerPedestalID

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, metadata: Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case i: TilePowerPedestal =>
        i.onBlockBreak()
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, metadata)
  }

  override def onPostBlockPlaced(p_149714_1_ : World, p_149714_2_ : Int, p_149714_3_ : Int, p_149714_4_ : Int, p_149714_5_ : Int): Unit = {
    p_149714_1_.getTileEntity(p_149714_2_, p_149714_3_, p_149714_4_) match {
      case i: TilePowerPedestal =>
        i.onPostBlockPlaced()
      case _ =>
    }
    super.onPostBlockPlaced(p_149714_1_, p_149714_2_, p_149714_3_, p_149714_4_, p_149714_5_)
  }
}
