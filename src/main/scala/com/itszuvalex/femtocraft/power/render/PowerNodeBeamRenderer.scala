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
object PowerNodeBeamRenderer extends PowerBeamRenderer {
  val BEAM_WIDTH    = .1f
  val RENDER_RADIUS = 64
  private val beamOuterLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_outer.png")
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_colored.png")

  def renderPowerBeams(node: TileEntity with IPowerNode, x: Double, y: Double, z: Double, partialTime: Float) = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamOuterLocation)
    renderBeamsToAllChildren(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, Color(180.toByte, 255.toByte, 255.toByte, 255.toByte))
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamColorLocation)
    renderBeamsToAllChildren(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, new Color(node.getColor))
  }

  def renderPowerBeamToChild(node: TileEntity with IPowerNode, x: Double, y: Double, z: Double, partialTime: Float, child: Loc4): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamOuterLocation)
    renderBeamToChild(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, Color(180.toByte, 255.toByte, 255.toByte, 255.toByte), child)
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamColorLocation)
    renderBeamToChild(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, new Color(node.getColor), child)

  }
}
