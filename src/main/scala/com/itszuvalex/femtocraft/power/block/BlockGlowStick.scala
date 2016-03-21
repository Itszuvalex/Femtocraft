package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.tile.TileGlowStick
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.util.IIcon
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 1/29/2016.
  */
class BlockGlowStick extends TileContainer(Material.circuits) {
  var coloredIcon: IIcon = null
  var sideIcon   : IIcon = null

  setCreativeTab(Femtocraft.tab)
  setLightLevel(1f)
  setBlockBounds(6f / 16f, 0, 6f / 16f, 10f / 16f, 1, 10f / 16f)

  override def renderAsNormalBlock = false

  override def isOpaqueCube = false

  override def getRenderType = RenderIDs.glowStickID

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int) = new TileGlowStick

  override def getCollisionBoundingBoxFromPool(p_149668_1_ : World, p_149668_2_ : Int, p_149668_3_ : Int, p_149668_4_ : Int) = null

  override def registerBlockIcons(p_149651_1_ : IIconRegister): Unit = {
    blockIcon = p_149651_1_.registerIcon(Femtocraft.ID + ":" + "BlockGlowStick")
    sideIcon = blockIcon
    coloredIcon = p_149651_1_.registerIcon(Femtocraft.ID + ":" + "BlockGlowStick_colored")
  }
}
