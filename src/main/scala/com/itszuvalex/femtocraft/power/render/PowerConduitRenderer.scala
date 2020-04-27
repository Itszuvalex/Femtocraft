package com.itszuvalex.femtocraft.power.render

import java.util

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.power.tile.TilePowerConduitCrystal
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.api.ItszuLibModules
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.render.TileEntityCombinedRenderer
import com.itszuvalex.itszulib.util.{Color, StringUtils}
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

import scala.collection.JavaConversions._

/**
 * Created by Christopher Harris (Itszuvalex) on 8/5/15.
 */
object PowerConduitRenderer {
  val conduitModelLocation    = Resources.CustomModelBlock("wire/wire_thin.obj")
  val conduitTexLocation      = Resources.CustomModelBlockTex("wire/wire_thin.png")
  val conduitColorTexLocation = Resources.CustomModelBlockTex("wire/wire_thin_color.png")
}

class PowerConduitRenderer extends TileEntityCombinedRenderer[TilePowerConduitCrystal] {
  val conduitModel  = LoadObj(PowerConduitRenderer.conduitModelLocation)
  val slowingFactor = 1.3f

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    super.renderTileEntityAsItem(x, y, z, partialTicks)
    renderConduitAt(x, y, z, Minecraft.getMinecraft.getRenderPartialTicks, Color(0, 0, 0, 0), util.EnumSet.noneOf(classOf[EnumFacing]))
  }

  def renderConduitAt(x: Double, y: Double, z: Double, partialTicks: Float, color: Color, connections: util.EnumSet[EnumFacing]): Unit = {
    GL11.glPushMatrix()

    translationBlock(x + .5, y + .5, z + .5) {
      this.bindTexture(PowerConduitRenderer.conduitTexLocation)

      conduitModel.renderGroups(Set("Core_Cube"))

      val set = connections.map(f => StringUtils.capitalize(f.getName) + "_Cube").toSet
      conduitModel.renderGroups(set)

      GL11.glDisable(GL11.GL_CULL_FACE)
      GL11.glDisable(GL11.GL_LIGHTING)

      val time = Option(Minecraft.getMinecraft.world).map(_.getTotalWorldTime.toFloat).getOrElse(0f)

      GL11.glColor4ub(color.red, color.green, color.blue, 255.toByte)
      this.bindTexture(PowerConduitRenderer.conduitColorTexLocation)

      FemtoRenderUtils.disableLightMaps()
      conduitModel.renderGroups(Set("Core_Cube"))

      conduitModel.renderGroups(set)
      FemtoRenderUtils.enableLightMap(null)
    }
    GL11.glPopMatrix()
    GL11.glColor4f(1f, 1f, 1f, 1f)
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_CULL_FACE)
  }

  override def renderTileEntityInWorld(te: TilePowerConduitCrystal, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage, alpha)
    val color   = te.getModule(ItszuLibModules.COLORABLE, null)
    val facings = EnumFacing.VALUES.filter(te.conduit.isConnected)
    val enumSet = if (facings.isEmpty) util.EnumSet.noneOf(classOf[EnumFacing]) else util.EnumSet.copyOf(facings.toSet)
    renderConduitAt(x, y, z, partialTicks, color, enumSet)
  }
}
