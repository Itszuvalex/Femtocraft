package com.itszuvalex.femtocraft.logistics.block

import java.util.Random

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.itszulib.core.TileContainer
import com.itszuvalex.itszulib.util.InventoryUtils
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection

/**
  * Created by Christopher Harris (Itszuvalex) on 12/20/15.
  */
class BlockItemRepository extends TileContainer(Material.iron) {
  var sideIcon: IIcon = null

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileItemRepository

  override def getIcon(side: Int, meta: Int): IIcon = {
    ForgeDirection.getOrientation(side) match {
      case ForgeDirection.EAST | ForgeDirection.WEST | ForgeDirection.NORTH | ForgeDirection.SOUTH
      => sideIcon
      case _ =>
        blockIcon
    }
  }

  override def registerBlockIcons(p_149651_1_ : IIconRegister): Unit = {
    blockIcon = p_149651_1_.registerIcon(Femtocraft.ID + ":" + "BlockItemRepository_top")
    sideIcon = p_149651_1_.registerIcon(Femtocraft.ID + ":" + "BlockItemRepository_side")
  }

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, metadata: Int): Unit = {
    world.getTileEntity(x, y, z) match {
      case i: TileItemRepository =>
        val random = new Random()
        (0 until i.getSizeInventory).map(i.getStackInSlotOnClosing).foreach(InventoryUtils.dropItem(_, world, x, y, z, random))
      case _ =>
    }
    super.breakBlock(world, x, y, z, block, metadata)
  }
}
