package com.itszuvalex.femtocraft.logistics.render

import java.util

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.logistics.tile.TileConduit
import com.itszuvalex.femtocraft.render.OBJDynamicRenderer._
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.render.TileEntityCombinedRenderer
import com.itszuvalex.itszulib.util.StringUtils
import net.minecraft.client.Minecraft
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

import scala.collection.JavaConversions._

/**
  * Created by Christopher Harris (Itszuvalex) on 8/5/15.
  */
object ConduitRenderer {
  val conduitModelLocation = Resources.CustomModelBlock("conduit/conduit.obj")
  val conduitTexLocation   = Resources.CustomModelBlockTex("conduit/conduit.png")
}

class ConduitRenderer extends TileEntityCombinedRenderer[TileConduit] {
  val conduitModel = LoadObj(ConduitRenderer.conduitModelLocation)


  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    super.renderTileEntityAsItem(x, y, z, partialTicks)
    renderConduitAt(x, y, z, Minecraft.getMinecraft.getRenderPartialTicks, Option(Minecraft.getMinecraft.world).map(_.getTotalWorldTime.toFloat).getOrElse(0f), util.EnumSet.noneOf(classOf[EnumFacing]))
  }

  override def renderTileEntityInWorld(te: TileConduit, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int): Unit = {
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage)
    val cap = te.getCapability(Capabilities.TILE_CONDUIT, null)
    val facings = EnumFacing.VALUES.filter(cap.isConnected)
    val enumSet = if (facings.isEmpty) util.EnumSet.noneOf(classOf[EnumFacing]) else util.EnumSet.copyOf(facings.toSet)
    renderConduitAt(x, y, z, partialTicks, te.getWorld.getTotalWorldTime.toFloat, enumSet)
  }

  def renderConduitAt(x: Double, y: Double, z: Double, partialTicks: Float, time: Float, connections: util.EnumSet[EnumFacing]): Unit = {
    this.bindTexture(ConduitRenderer.conduitTexLocation)
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_LIGHTING)
    GL11.glPushMatrix()

    translationBlock(x + .5, y + .5, z + .5) {
      conduitModel.renderGroups(Set("Core_Cube"), bindTextures = false)

      val set = connections.map(f => StringUtils.capitalize(f.getName) + "_Cube").toSet
      conduitModel.renderGroups(set, bindTextures = false)
    }
    GL11.glPopMatrix()
    GL11.glColor4f(1f, 1f, 1f, 1f)
    GL11.glEnable(GL11.GL_LIGHTING)
    GL11.glEnable(GL11.GL_CULL_FACE)
  }
}
