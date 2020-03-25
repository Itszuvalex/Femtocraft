package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import net.minecraft.tileentity.TileEntity

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */

object PowerNodeRenderer {
  val RENDER_RADIUS = PowerNodeBeamRenderer.RENDER_RADIUS
}

class PowerNodeRenderer[T <: TileEntity with ITileEntity] extends NodeCrystalRenderer[T] {

  override def render(te: T, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float) = {
    renderNode(te, x, y, z, partialTicks)
    PowerNodeBeamRenderer.renderPowerBeams(te, x, y, z, partialTicks)
  }
}
