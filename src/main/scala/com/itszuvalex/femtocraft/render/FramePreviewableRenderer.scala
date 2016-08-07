package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.industry.{FrameMultiblockRegistry, FrameMultiblockRendererRegistry, IFrameItem}
import com.itszuvalex.itszulib.api.IPreviewableRenderer
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.item.ItemStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * Created by Christopher on 8/26/2015.
  */
@SideOnly(Side.CLIENT)
class FramePreviewableRenderer extends IPreviewableRenderer {
  lazy val generic = new GenericFrameMultiblockRenderer

  override def renderAtLocation(stack: ItemStack, loc: Loc4, rx: Double, ry: Double, rz: Double): Unit = {
    stack.getItem match {
      case frame: IFrameItem =>
        frame.getSelectedMultiblock(stack) match {
          case multi: String =>
            FrameMultiblockRegistry.getMultiblock(multi) match {
              case Some(mb) =>
                FrameMultiblockRendererRegistry.getRenderer(mb.multiblockRenderID) match {
                  case Some(renderer) => renderer.previewRenderAtWorldLocation(stack, loc, rx, ry, rz)
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
  }
}
