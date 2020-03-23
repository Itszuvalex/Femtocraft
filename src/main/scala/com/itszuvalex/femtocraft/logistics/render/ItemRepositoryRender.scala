package com.itszuvalex.femtocraft.logistics.render

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoRenderSwitches
import com.itszuvalex.femtocraft.logistics.tile.TileItemRepository
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.femtocraft.{Femtocraft, Resources}
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.render.TileEntityRenderCube
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

object ItemRepositoryRender {
  lazy val sideTex  = Resources.TexBlock("blockitemrepository_side.png")
  lazy val topTex   = Resources.TexBlock("blockitemrepository_top.png")
  lazy val frontTex = Resources.TexBlock("blockitemrepository_front.png")
}

class ItemRepositoryRender extends TileEntityRenderCube[TileItemRepository](Femtocraft.ID.toLowerCase(), ItemRepositoryRender.sideTex) {
  override def preFaceRender(facing: EnumFacing): Unit = {
    GL11.glColor4f(1f, 1f, 1f, 1f)
    facing match {
      case EnumFacing.UP | EnumFacing.DOWN => bindTexture(ItemRepositoryRender.topTex)
      case EnumFacing.NORTH => bindTexture(ItemRepositoryRender.frontTex)
      case _ => super.preFaceRender(facing)
    }
  }

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    facing = EnumFacing.SOUTH
    super.renderTileEntityAsItem(x, y, z, partialTicks)
  }

  override def renderTileEntityInWorld(te: TileItemRepository, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    facing = Option(te).map(_.getWorld.getBlockState(te.getPos).getValue(BlockBehaviorHorizontalFacing.FACING)).getOrElse(EnumFacing.NORTH)
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage, alpha)
    if (FemtoRenderSwitches.renderItemConfiguration && te.hasCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null)) {
      FemtoRenderUtils.renderItemConfigOverlay(te.asInstanceOf[ITileEntity], x, y, z, te.getCapability(Capabilities.ITEM_STORAGE_CONFIGURABLE, null))
    }
  }
}
