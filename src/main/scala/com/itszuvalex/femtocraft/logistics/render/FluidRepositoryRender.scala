package com.itszuvalex.femtocraft.logistics.render

import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.client.FemtoRenderSwitches
import com.itszuvalex.femtocraft.logistics.tile.TileFluidRepository
import com.itszuvalex.femtocraft.render.FemtoRenderUtils
import com.itszuvalex.femtocraft.{Femtocraft, Resources}
import com.itszuvalex.itszulib.core.traits.tile.BlockFacing
import com.itszuvalex.itszulib.render.{RenderUtils, TileEntityRenderCube}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import org.lwjgl.opengl.GL11

object FluidRepositoryRender {
  lazy val sideTex       = Resources.TexBlock("blockfluidrepository_side.png")
  lazy val topTex        = Resources.TexBlock("blockfluidrepository_top.png")
  lazy val frontTex      = Resources.TexBlock("blockfluidrepository_front.png")
  lazy val sideTexEmpty  = Resources.TexBlock("blockfluidrepository_side_empty.png")
  lazy val topTexEmpty   = Resources.TexBlock("blockfluidrepository_top_empty.png")
  lazy val frontTexEmpty = Resources.TexBlock("blockfluidrepository_front_empty.png")
}

class FluidRepositoryRender extends TileEntityRenderCube[TileFluidRepository](Femtocraft.ID.toLowerCase(), FluidRepositoryRender.sideTex) {
  var pass = 0

  override def preFaceRender(facing: EnumFacing): Unit = {
    GL11.glColor4f(1f, 1f, 1f, 1f)
    if (pass == 0) {
      facing match {
        case EnumFacing.UP | EnumFacing.DOWN => bindTexture(FluidRepositoryRender.topTex)
        case EnumFacing.NORTH => bindTexture(FluidRepositoryRender.frontTex)
        case _ => super.preFaceRender(facing)
      }
    }
    else {
      facing match {
        case EnumFacing.UP | EnumFacing.DOWN => bindTexture(FluidRepositoryRender.topTexEmpty)
        case EnumFacing.NORTH => bindTexture(FluidRepositoryRender.frontTexEmpty)
        case _ => bindTexture(FluidRepositoryRender.sideTexEmpty)
      }
    }
  }

  override def renderTileEntityAsItem(x: Double, y: Double, z: Double, partialTicks: Float): Unit = {
    pass = 0
    facing = EnumFacing.SOUTH
    super.renderTileEntityAsItem(x, y, z, partialTicks)
    pass = 1
    super.renderTileEntityAsItem(x, y, z, partialTicks)
  }

  override def renderTileEntityInWorld(te: TileFluidRepository, x: Double, y: Double, z: Double, partialTicks: Float, destroyStage: Int, alpha: Float): Unit = {
    GL11.glDisable(GL11.GL_BLEND)
    pass = 0
    facing = Option(te).map(_.getWorld.getBlockState(te.getPos).getValue(BlockFacing.FACING)).getOrElse(EnumFacing.NORTH)
    val fluidStack = Option(te).filter(_.hasCapability(com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE, null)).map(_.getCapability(com.itszuvalex.itszulib.api.Capabilities.FLUID_STORAGE, null).getStorageProperties.apply(0).getContents).orNull
    if (fluidStack != null) {
      RenderUtils.bindBlockTextures()
      val block = fluidStack.getFluid.getBlock
      RenderUtils.glMatrixBlock {
        val sprite = RenderUtils.getDefaultTextureForBlock(block)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        EnumFacing.VALUES.foreach { face =>
          RenderUtils.drawArbitraryFace(x.toFloat, y.toFloat, z.toFloat,
            .001f, .999f, .001f, .999f, .001f, .999f, face, sprite, sprite.getMinU, sprite.getMaxU, sprite.getMinV, sprite.getMaxV)
        }
        //        Minecraft.getMinecraft.getBlockRendererDispatcher.getBlockModelRenderer.renderModelBrightness(blockmodel, state, 1f, false)
      }
    }
    super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage, alpha)
    if (fluidStack == null) {
      pass = 1
      super.renderTileEntityInWorld(te, x, y, z, partialTicks, destroyStage, alpha)
    }
    if (FemtoRenderSwitches.renderFluidConfiguration && te.hasCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null)) {
      FemtoRenderUtils.renderFluidConfigOverlay(te.asInstanceOf[TileEntity], x, y, z, te.getCapability(Capabilities.FLUID_STORAGE_CONFIGURABLE, null))
    }
  }
}
