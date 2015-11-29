package com.itszuvalex.femtocraft.render

import com.itszuvalex.itszulib.render.Vector3
import net.minecraft.client.renderer.Tessellator
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */
object FemtoRenderUtils {

  def drawBeam(start: Vector3,
               end: Vector3,
               width: Float,
               uMin: Float = 0, uMax: Float = 1, vMin: Float = 0, vMax: Float = 1, red: Int = 255, green: Int = 255, blue: Int = 255, alpha: Int = 0): Unit = {
    val rightVector = (end - start).normalize()
    val center = ((end - start) / 2) + start
    val upVector = (center - Vector3(0, 0, 0)).cross(rightVector).normalize()
    val pos1 = start + (upVector * width)
    val pos2 = start - (upVector * width)
    val pos3 = end - (upVector * width)
    val pos4 = end + (upVector * width)

    val tes = Tessellator.instance
    tes.startDrawingQuads()
    GL11.glColor4ub(red.toByte, green.toByte, blue.toByte, alpha.toByte)
    tes.addVertexWithUV(pos2.x, pos2.y, pos2.z, uMin, vMin)
    tes.addVertexWithUV(pos3.x, pos3.y, pos3.z, uMin, vMax)
    tes.addVertexWithUV(pos4.x, pos4.y, pos4.z, uMax, vMax)
    tes.addVertexWithUV(pos1.x, pos1.y, pos1.z, uMax, vMin)
    //    tes.addVertexWithUV(pos4.x, pos4.y, pos4.z, uMin, vMin)
    //    tes.addVertexWithUV(pos3.x, pos3.y, pos3.z, uMin, vMax)
    //    tes.addVertexWithUV(pos2.x, pos2.y, pos2.z, uMax, vMax)
    //    tes.addVertexWithUV(pos1.x, pos1.y, pos1.z, uMax, vMin)
    tes.draw()
  }

}
