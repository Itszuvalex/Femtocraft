package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation

/**
  * Created by Christopher on 8/29/2015.
  */
object DiffusionNodeBeamRenderer extends PowerBeamRenderer {
  val BEAM_WIDTH    = .1f
  val RENDER_RADIUS = 64
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/diffusion_particles_colored.png")

  def renderDiffuseBeams(node: TileEntity with IPowerNode, x: Double, y: Double, z: Double, partialTime: Float) = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(DiffusionNodeBeamRenderer.beamColorLocation)
    renderBeamsToAllChildren(x, y, z, partialTime, node, DiffusionNodeBeamRenderer.BEAM_WIDTH, new Color(node.getColor).setAlpha(64.toByte))
  }

  def renderBeamToChild(node: TileEntity with IPowerNode, x: Double, y: Double, z: Double, partialTime: Float, child: Loc4): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(DiffusionNodeBeamRenderer.beamColorLocation)
    renderBeamToChild(x, y, z, partialTime, node, DiffusionNodeBeamRenderer.BEAM_WIDTH, new Color(node.getColor).setAlpha(64.toByte), child)
  }
}
