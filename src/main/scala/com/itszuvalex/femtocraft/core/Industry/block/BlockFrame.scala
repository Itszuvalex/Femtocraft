package com.itszuvalex.femtocraft.core.Industry.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.Industry.tile.TileFrame
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
 * Created by Christopher on 8/27/2015.
 */
class BlockFrame extends TileContainer(Material.iron) {
  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileFrame()

  override def getRenderBlockPass: Int = 2

  override def renderAsNormalBlock: Boolean = false

  override def isOpaqueCube: Boolean = false
}
