package com.itszuvalex.femtocraft.power.render

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.util.{EnumFacing, ResourceLocation}

/**
  * Created by Christopher on 8/29/2015.
  */
object WirelessPowerNodeBeamRenderer extends WirelessPowerBeamRenderer {
  val BEAM_WIDTH    = .1f
  val RENDER_RADIUS = 64
  private val beamOuterLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_outer.png")
  private val beamColorLocation = new ResourceLocation(Femtocraft.ID + ":" + "textures/power_beam_colored.png")

  def renderPowerBeams(node: ITileEntity, x: Double, y: Double, z: Double, partialTime: Float) = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(WirelessPowerNodeBeamRenderer.beamOuterLocation)
    renderBeamsToAllChildren(x, y, z, partialTime, node, WirelessPowerNodeBeamRenderer.BEAM_WIDTH, Color(180.toByte, 255.toByte, 255.toByte, 255.toByte))
    Minecraft.getMinecraft.getTextureManager.bindTexture(WirelessPowerNodeBeamRenderer.beamColorLocation)
    FemtoRenderUtils.disableLightMaps()
    renderBeamsToAllChildren(x, y, z, partialTime, node, WirelessPowerNodeBeamRenderer.BEAM_WIDTH, node.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP))
    FemtoRenderUtils.enableLightMap(node)
  }

  def renderPowerBeamToChild(node: ITileEntity, x: Double, y: Double, z: Double, partialTime: Float, child: Loc4): Unit = {
    Minecraft.getMinecraft.getTextureManager.bindTexture(WirelessPowerNodeBeamRenderer.beamOuterLocation)
    renderBeamToChild(x, y, z, partialTime, node, WirelessPowerNodeBeamRenderer.BEAM_WIDTH, Color(180.toByte, 255.toByte, 255.toByte, 255.toByte), child)
    Minecraft.getMinecraft.getTextureManager.bindTexture(WirelessPowerNodeBeamRenderer.beamColorLocation)
    FemtoRenderUtils.disableLightMaps()
    renderBeamToChild(x, y, z, partialTime, node, WirelessPowerNodeBeamRenderer.BEAM_WIDTH, node.getModule(ItszuLibModules.COLORABLE, EnumFacing.UP), child)
    FemtoRenderUtils.enableLightMap(node)
  }
}
