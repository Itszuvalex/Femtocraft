package com.itszuvalex.femtocraft.worldgen.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
 * Created by Alex on 08.08.2015.
 */
class BlockCrystalsWorldgen extends TileContainer(Material.glass) {
  setCreativeTab(Femtocraft.tab)

  /**
   * Is this block (a) opaque and (b) a full 1m cube?  This determines whether or not to render the shared face of two
   * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
   */
  override def isOpaqueCube =
    false


  override def getRenderBlockPass: Int = 2

  /**
   * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
   */
  override def renderAsNormalBlock = false

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileCrystalsWorldgen
}
