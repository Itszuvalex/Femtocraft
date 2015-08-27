package com.itszuvalex.femtocraft.core.Cyber.block

import com.itszuvalex.femtocraft.Femtocraft
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister

/**
 * Created by Christopher on 8/27/2015.
 */
class BlockCyberweave extends Block(Material.iron) {
  setStepSound(Block.soundTypeMetal)

  override def registerBlockIcons(register: IIconRegister): Unit = {
    this.blockIcon = register.registerIcon(Femtocraft.ID + ":" + "cyberweave")
  }
}
