package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.tile.TilePowerPedestal
import com.itszuvalex.itszulib.core.TileContainer
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

  override def getRenderBlockPass: Int = 2

  override def isOpaqueCube: Boolean = false
}
