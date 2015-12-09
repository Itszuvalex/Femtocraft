package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation

/**
  * Created by Christopher on 8/29/2015.
  */
object PowerNodeBeamRenderer {
  private val beamOuterLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_outer.png")
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_colored.png")
  val BEAM_WIDTH    = .2f
  val RENDER_RADIUS = 64
}

trait PowerNodeBeamRenderer extends TileEntitySpecialRenderer with PowerBeamRenderer {
  def renderPowerBeams(node: TileEntity with IPowerNode, x: Double, y: Double, z: Double, partialTime: Float) = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamOuterLocation)
    renderBeamsToAllChildren(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, Color(180.toByte, 255.toByte, 255.toByte, 255.toByte))
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamColorLocation)
    renderBeamsToAllChildren(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, new Color(node.getColor))
  }
}
