package com.itszuvalex.femtocraft.power.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.tile.TilePowerSink
import com.itszuvalex.femtocraft.render.RenderIDs
import com.itszuvalex.itszulib.core.TileContainer
import net.minecraft.block.material.Material
import net.minecraft.tileentity.TileEntity
import net.minecraft.world.World

/**
  * Created by Christopher Harris (Itszuvalex) on 1/28/2016.
  */
class BlockPowerSink extends TileContainer(Material.iron) {
  setCreativeTab(Femtocraft.tab)

  override def renderAsNormalBlock(): Boolean = false

  override def isOpaqueCube: Boolean = false

  override def getRenderType: Int = RenderIDs.powerSinkID

  override def createNewTileEntity(p_149915_1_ : World, p_149915_2_ : Int): TileEntity = new TilePowerSink
}
