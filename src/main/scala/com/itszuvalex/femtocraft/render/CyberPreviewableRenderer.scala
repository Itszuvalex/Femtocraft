package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.core.Cyber.item.ItemBaseSeed
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.itszulib.api.IPreviewableRenderer
import com.itszuvalex.itszulib.render.RenderUtils
import net.minecraft.client.renderer.Tessellator
import net.minecraft.init.Blocks
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import org.lwjgl.opengl.GL11

/**
 * Created by Alex on 26.09.2015.
 */
class CyberPreviewableRenderer extends IPreviewableRenderer {

  override def renderAtLocation(stack: ItemStack, world: World, _x: Int, y: Int, _z: Int, _rx: Double, ry: Double, _rz: Double): Unit = {
    if (!stack.getItem.isInstanceOf[ItemBaseSeed]) return
    var x = 0
    var z = 0
    var rx = 0d
    var rz = 0d
    ItemBaseSeed.getSize(stack) match {
      case 3 =>
        x = _x - 1
        z = _z - 1
        rx = _rx - 1
        rz = _rz - 1
      case _ =>
        x = _x
        z = _z
        rx = _rx
        rz = _rz
    }
    val baseLocations = ItemBaseSeed.getBaseLocations(stack, x, y, z, world.provider.dimensionId)
    val slotLocations = ItemBaseSeed.getSlotLocations(stack, x, y, z, world.provider.dimensionId)
    Tessellator.instance.startDrawingQuads()
    GL11.glDisable(GL11.GL_CULL_FACE)
    GL11.glEnable(GL11.GL_BLEND)
    if (TileCyberBase.areAllPlaceable(baseLocations) &&
        TileCyberBase.arePartsAtYPlaceable(slotLocations, y + TileCyberBase.baseHeightMap(ItemBaseSeed.getSize(stack)))
       ) Tessellator.instance.setColorRGBA_F(0, 1, 0, .5f) else Tessellator.instance.setColorRGBA_F(1, 0, 0, .5f)
    baseLocations.toList.sortWith { case (a1, a2) =>
      a1.distSqr((rx + x).toInt,
        (ry + y).toInt,
        (rz + z).toInt) <
        a2.distSqr((rx + x).toInt,
          (ry + y).toInt,
          (rz + z).toInt)
    }
      .foreach { loc =>
      RenderUtils.renderCube(rx.toFloat + (loc.x - x), ry.toFloat + (loc.y - y), rz.toFloat + (loc.z - z), 0, 0, 0, 1, 1, 1, Blocks.iron_block.getIcon(0, 0))
    }
    if (TileCyberBase.areAllPlaceable(slotLocations)) Tessellator.instance.setColorRGBA_F(0, .75f, 1, .5f) else Tessellator.instance.setColorRGBA_F(.75f, 0, 1, .5f)
    slotLocations.toList.sortWith { case (a1, a2) =>
      a1.distSqr((rx + x).toInt,
        (ry + y).toInt,
        (rz + z).toInt) <
        a2.distSqr((rx + x).toInt,
          (ry + y).toInt,
          (rz + z).toInt)
    }
      .foreach { loc =>
      RenderUtils.renderCube(rx.toFloat + (loc.x - x), ry.toFloat + (loc.y - y), rz.toFloat + (loc.z - z), 0, 0, 0, 1, 1, 1, Blocks.iron_block.getIcon(0, 0))
    }
    Tessellator.instance.draw()
    GL11.glEnable(GL11.GL_CULL_FACE)
    GL11.glDisable(GL11.GL_BLEND)
  }

}
