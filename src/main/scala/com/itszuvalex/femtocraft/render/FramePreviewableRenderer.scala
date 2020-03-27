package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, FrameMultiblockRendererRegistry, IFrameItem}
import com.itszuvalex.itszulib.api.client.IPreviewableRenderer
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher on 8/26/2015.
  */
@SideOnly(Side.CLIENT)
class FramePreviewableRenderer extends IPreviewableRenderer {
  lazy val generic = new GenericFrameMultiblockRenderer

  override def render(stack: IItemStack, player: EntityPlayer): Unit = {}

  override def renderAtLocation(stack: IItemStack, player: EntityPlayer, loc: Loc4, rx: Double, ry: Double, rz: Double): Unit = {
    stack.toMinecraft.getItem match {
      case frame: IFrameItem =>
        frame.getSelectedMultiblock(stack) match {
          case multi: String =>
            FrameMultiblockRegistry.getMultiblock(multi) match {
              case Some(mb) =>
                if (mb.canPlaceAtLocation(loc))
                  GL11.glColor3f(0, 1, 0)
                else
                  GL11.glColor3f(1, 0, 0)
                FrameMultiblockRendererRegistry.getRenderer(mb.multiblockRenderID) match {
                  case Some(renderer) =>
                    renderer.previewRenderAtWorldLocation(stack, loc, rx, ry, rz)
                  case None =>
                    generic.multi = mb
                    generic.previewRenderAtWorldLocation(stack, loc, rx, ry, rz)
                }
              case None =>
            }
          case _ =>
        }
      case _ =>
    }
    GL11.glColor3f(1, 1, 1)
  }
}

