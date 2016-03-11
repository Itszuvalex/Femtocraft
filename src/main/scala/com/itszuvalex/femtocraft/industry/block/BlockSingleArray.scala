package com.itszuvalex.femtocraft.industry.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.industry.tile.TileSingleArray
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.IIcon
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 3/9/16.
  */
class BlockSingleArray extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)

  var iconSideBase  : IIcon = null
  var iconSideColor : IIcon = null
  var iconFrontBase : IIcon = null
  var iconFrontColor: IIcon = null

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TileSingleArray

  override def registerBlockIcons(register: IIconRegister): Unit = {
    iconSideBase = register.registerIcon(Femtocraft.ID + ":" + "BlockMachineBlock_side_base.png")
    iconSideColor = register.registerIcon(Femtocraft.ID + ":" + "BlockMachineBlock_side_base.png")
    iconFrontBase = register.registerIcon(Femtocraft.ID + ":" + "BlockMachineBlock_front_base.png")
    iconFrontColor = register.registerIcon(Femtocraft.ID + ":" + "BlockMachineBlock_front_base.png")
    blockIcon = iconFrontBase
  }
}
