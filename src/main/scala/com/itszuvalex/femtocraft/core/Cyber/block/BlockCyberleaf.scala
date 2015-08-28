package com.itszuvalex.femtocraft.core.Cyber.block

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.block.BlockLeavesBase
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister

/**
 * Created by Christopher on 8/27/2015.
 */
class BlockCyberleaf extends BlockLeavesBase(Material.leaves, true) {
  override def registerBlockIcons(iconRegister: IIconRegister): Unit = {
    this.blockIcon = iconRegister.registerIcon(Femtocraft.ID + ":" + "cyberleaf")
  }
}
