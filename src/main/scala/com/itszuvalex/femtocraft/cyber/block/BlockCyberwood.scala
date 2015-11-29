package com.itszuvalex.femtocraft.cyber.block

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.util.IIcon
import net.minecraft.world.IBlockAccess
import net.minecraftforge.common.util.ForgeDirection

/**
  * Created by Christopher on 8/27/2015.
  */
class BlockCyberwood extends Block(Material.wood) {
  var topIcon: IIcon = null

  override def canSustainLeaves(world: IBlockAccess, x: Int, y: Int, z: Int): Boolean = true

  override def getIcon(side: Int, meta: Int): IIcon = ForgeDirection.getOrientation(side) match {
    case ForgeDirection.UP => topIcon
    case ForgeDirection.DOWN => topIcon
    case _ => blockIcon
  }

  override def registerBlockIcons(register: IIconRegister): Unit = {
    this.blockIcon = register.registerIcon(Femtocraft.ID + ":" + "log_cyber")
    this.topIcon = register.registerIcon(Femtocraft.ID + ":" + "log_cyber_top")
  }

}
