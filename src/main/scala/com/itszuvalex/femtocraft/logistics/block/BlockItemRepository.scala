package com.itszuvalex.femtocraft.logistics.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.itszulib.core.TileContainer
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
}
