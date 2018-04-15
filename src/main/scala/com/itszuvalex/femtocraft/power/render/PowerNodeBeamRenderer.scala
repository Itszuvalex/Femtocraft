package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.PowerNode
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.itszulib.api.ItszuLibCapabilities
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{EnumFacing, ResourceLocation}

/**
  * Created by Christopher on 8/29/2015.
  */
object PowerNodeBeamRenderer extends PowerBeamRenderer {
  val BEAM_WIDTH    = .1f
  val RENDER_RADIUS = 64
  private val beamOuterLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_outer.png")
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_colored.png")

  def renderPowerBeams(node: TileEntity with PowerNode, x: Double, y: Double, z: Double, partialTime: Float) = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamOuterLocation)
    renderBeamsToAllChildren(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, Color(180.toByte, 255.toByte, 255.toByte, 255.toByte))
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamColorLocation)
    FemtoRenderUtils.disableLightMaps()
    renderBeamsToAllChildren(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, node.getCapability(ItszuLibCapabilities.COLORABLE, EnumFacing.UP))
    FemtoRenderUtils.enableLightMap(node)
  }

  def renderPowerBeamToChild(node: TileEntity with PowerNode, x: Double, y: Double, z: Double, partialTime: Float, child: Loc4): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamOuterLocation)
    renderBeamToChild(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, Color(180.toByte, 255.toByte, 255.toByte, 255.toByte), child)
    Minecraft.getMinecraft.getTextureManager.bindTexture(PowerNodeBeamRenderer.beamColorLocation)
    FemtoRenderUtils.disableLightMaps()
    renderBeamToChild(x, y, z, partialTime, node, PowerNodeBeamRenderer.BEAM_WIDTH, node.getCapability(ItszuLibCapabilities.COLORABLE, EnumFacing.UP), child)
    FemtoRenderUtils.enableLightMap(node)
  }
}
