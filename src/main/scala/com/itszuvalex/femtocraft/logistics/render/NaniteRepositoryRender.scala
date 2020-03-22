package com.itszuvalex.femtocraft.logistics.render

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoRenderSwitches
import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.femtocraft.{Femtocraft, Resources}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.render.TileEntityRenderCube
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

object NaniteRepositoryRender {
  lazy val sideTex  = Resources.TexBlock("blocknaniterepository_side.png")
  lazy val topTex   = Resources.TexBlock("blocknaniterepository_top.png")
  lazy val frontTex = Resources.TexBlock("blocknaniterepository_front.png")
}

class NaniteRepositoryRender extends TileEntityRenderCube[TileNaniteRepository](Femtocraft.ID.toLowerCase(), NaniteRepositoryRender.sideTex) {
  override def preFaceRender(facing: EnumFacing): Unit = {
    GL11.glColor4f(1f, 1f, 1f, 1f)
    facing match {
      case EnumFacing.UP | EnumFacing.DOWN => bindTexture(NaniteRepositoryRender.topTex)
      case EnumFacing.NORTH => bindTexture(NaniteRepositoryRender.frontTex)
      case _ => super.preFaceRender(facing)
    }
  }

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    facing = EnumFacing.SOUTH
    super.renderTileEntityAsItem(x, y, z, partialTicks)
  }

  override def renderTileEntityInWorld(te: TileNaniteRepository, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    facing = Option(te).map(_.getWorld.getBlockState(te.getPos).getValue(BlockBehaviorHorizontalFacing.FACING)).getOrElse(EnumFacing.NORTH)
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage, alpha)
    if (FemtoRenderSwitches.renderNaniteConfiguration && te.hasCapability(Capabilities.NANITE_STORAGE_CONFIGURABLE, null)) {
      FemtoRenderUtils.renderNaniteConfigOverlay(te.asInstanceOf[TileEntity], x, y, z, te.getCapability(Capabilities.NANITE_STORAGE_CONFIGURABLE, null))
    }
  }
}
