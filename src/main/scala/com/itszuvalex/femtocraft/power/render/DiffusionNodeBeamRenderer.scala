package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import net.minecraft.client.Minecraft
import net.minecraft.util.{EnumFacing, ResourceLocation}

/**
  * Created by Christopher on 8/29/2015.
  */
object DiffusionNodeBeamRenderer extends WirelessPowerBeamRenderer {
  val BEAM_WIDTH    = .1f
  val RENDER_RADIUS = 64
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/diffusion_particles_colored.png")

  def renderDiffuseBeams(node: ITileEntity, x: Double, y: Double, z: Double, partialTime: Float) = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(DiffusionNodeBeamRenderer.beamColorLocation)
    renderBeamsToAllChildren(x, y, z, partialTime, node, DiffusionNodeBeamRenderer.BEAM_WIDTH, node.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP).setAlpha(64.toByte))
  }

  def renderBeamToChild(node: ITileEntity, x: Double, y: Double, z: Double, partialTime: Float, child: Loc4): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(DiffusionNodeBeamRenderer.beamColorLocation)
    renderBeamToChild(x, y, z, partialTime, node, DiffusionNodeBeamRenderer.BEAM_WIDTH, node.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP).setAlpha(64.toByte), child)
  }
}
