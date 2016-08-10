package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.cyber.item.ItemBaseSeed
import com.itszuvalex.itszulib.api.IPreviewableRenderer
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.render.RenderUtils
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import org.lwjgl.opengl.GL11

/**
  * Created by Alex on 26.09.2015.
  */
class CyberPreviewableRenderer extends IPreviewableRenderer {

  override def renderAtLocation(stack: ItemStack, loc4: Loc4, _rx: Double, _ry: Double, _rz: Double): Unit = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) return
    var x = 0
    var z = 0
    var rx = 0d
    var rz = 0d
    ItemBaseSeed.getSize(stack) match {
      case 3 =>
        x = loc4.x - 1
        z = loc4.z - 1
        rx = _rx - 1
        rz = _rz - 1
      case _ =>
        x = loc4.x
        z = loc4.z
        rx = _rx
        rz = _rz
    }
    val baseLocations = ItemBaseSeed.getBaseLocations(stack, x, loc4.y, z, loc4.getWorld.get.provider.getDimension)
    val slotLocations = ItemBaseSeed.getSlotLocations(stack, x, loc4.y, z, loc4.getWorld.get.provider.getDimension)
    GL11.glPushMatrix()
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glEnable(GL11.GL_BLEND)
    //                              if (TileCyberBase.areAllPlaceable(baseLocations) &&
    //                                  TileCyberBase.arePartsAtYPlaceable(slotLocations, loc.y + TileCyberBase.baseHeightMap(ItemBaseSeed.getSize(stack)))
    //                              )
    //                                Tessellator.instance.setColorRGBA_F(0, 1, 0, .5f)
    //                              else Tessellator.instance.setColorRGBA_F(1, 0, 0, .5f)
    baseLocations.toList.sortWith { case (a1, a2) =>
      a1.distSqr((rx + x).toInt,
        (_ry + loc4.y).toInt,
        (rz + z).toInt) <
        a2.distSqr((rx + x).toInt,
          (_ry + loc4.y).toInt,
          (rz + z).toInt)
    }
      .foreach { loc =>
        RenderUtils.renderCube(rx.toFloat + (loc.x - x), _ry.toFloat + (loc.y - loc4.y), rz.toFloat + (loc.z - z), 0, 0, 0, 1, 1, 1, RenderUtils.getDefaultTextureForBlock(Blocks.IRON_BLOCK))
      }
    //                              if (TileCyberBase.areAllPlaceable(slotLocations)) Tessellator.instance.setColorRGBA_F(0, .75f, 1, .5f) else Tessellator.instance.setColorRGBA_F(.75f, 0, 1, .5f)
    slotLocations.toList.sortWith { case (a1, a2) =>
      a1.distSqr((rx + x).toInt,
        (_ry + loc4.y).toInt,
        (rz + z).toInt) <
        a2.distSqr((rx + x).toInt,
          (_ry + loc4.y).toInt,
          (rz + z).toInt)
    }
      .foreach { loc =>
        RenderUtils.renderCube(rx.toFloat + (loc.x - x), _ry.toFloat + (loc.y - loc4.y), rz.toFloat + (loc.z - z), 0, 0, 0, 1, 1, 1, RenderUtils.getDefaultTextureForBlock(Blocks.IRON_BLOCK))
      }
    GL11.glPopMatrix()
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
  }

}
