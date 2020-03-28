package com.itszuvalex.femtocraft.render

import com.itszuvalex.femtocraft.Resources
import com.itszuvalex.femtocraft.industry.gui.{GuiSidedFluidConfig, GuiSidedInventoryConfig, GuiSidedNaniteConfig}
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.{EnumAutomaticIO, SidedFluidStorageConfiguration, SidedItemStorageConfiguration}
import com.itszuvalex.itszulib.render.RenderUtils._
import com.itszuvalex.itszulib.render.{RenderUtils, Vector3}
import com.itszuvalex.itszulib.util.Color
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.{OpenGlHelper, RenderHelper}
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumFacing.AxisDirection
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */
object FemtoRenderUtils {
  val CONFIG_STORAGE_TEXTURE   = () => GuiSidedInventoryConfig.SIDE_TEX_COLOR
  val CONFIG_IO_TEXTURE_INPUT  = () => GuiSidedInventoryConfig.SIDE_TEX_INPUT
  val CONFIG_IO_TEXTURE_OUTPUT = () => GuiSidedInventoryConfig.SIDE_TEX_OUTPUT
  val CONFIG_OUTLINE           = Resources.TexBlock("blockoutline.png")

  def drawBeam(start: Vector3,
               end: Vector3,
               width: Float,
               uMin: Float = 0, uMax: Float = 1, vMin: Float = 0, vMax: Float = 1, red: Int = 255, green: Int = 255, blue: Int = 255, alpha: Int = 0): Unit = {
    val rightVector = (end - start).normalize()
    val center      = ((end - start) / 2) + start
    //    val cameraVec = Minecraft.getMinecraft.getRenderViewEntity.getPositionVector
    val eyes        = Minecraft.getMinecraft.getRenderViewEntity.getEyeHeight
    val upVector    = (center - Vector3(0, eyes, 0)).cross(rightVector).normalize()
    val pos1        = start + (upVector * width)
    val pos2        = start - (upVector * width)
    val pos3        = end - (upVector * width)
    val pos4        = end + (upVector * width)

    drawBlock(DefaultVertexFormats.POSITION_TEX) {
      GL11.glColor4ub(red.toByte, green.toByte, blue.toByte, alpha.toByte)
      addVertexUV(pos2.x, pos2.y, pos2.z, uMin, vMin)
      addVertexUV(pos3.x, pos3.y, pos3.z, uMin, vMax)
      addVertexUV(pos4.x, pos4.y, pos4.z, uMax, vMax)
      addVertexUV(pos1.x, pos1.y, pos1.z, uMax, vMin)
      //    tes.addVertexWithUV(pos4.x, pos4.y, pos4.z, uMin, vMin)
      //    tes.addVertexWithUV(pos3.x, pos3.y, pos3.z, uMin, vMax)
      //    tes.addVertexWithUV(pos2.x, pos2.y, pos2.z, uMax, vMax)
      //    tes.addVertexWithUV(pos1.x, pos1.y, pos1.z, uMax, vMin)
    }
  }

  def enableLightMap(te: ITileEntity): Unit = {
    GL11.glEnable(GL11.GL_LIGHTING)
    RenderHelper.enableStandardItemLighting()
    val i = Option(te).map(_.getIWorld.toMinecraft.getCombinedLight(te.getPos, 0)).getOrElse(15 << 20 | 15 << 4)
    setLightmapTexCoords(i)
  }

  def renderItemConfigOverlay(te: ITileEntity, x: Double, y: Double, z: Double, sidedConfig: SidedItemStorageConfiguration): Unit = {
    renderConfigOverlay(te, x, y, z,
                        (facing) =>
                          GuiSidedInventoryConfig.colors(sidedConfig.storages.keys.toArray.indexOf(sidedConfig.getStorageNameForAbsoluteFacing(facing)) % GuiSidedInventoryConfig.colors.length),
                        (facing) => sidedConfig.getIOForAbsoluteFacing(facing))
  }

  private def renderConfigOverlay(te: ITileEntity, x: Double, y: Double, z: Double, colorForStorage: (EnumFacing) => Color, ioForFacing: (EnumFacing) => EnumAutomaticIO): Unit = {
    disableLightMaps()
    RenderUtils.glMatrixBlock { // Matrixblock even though translate because we can also scale
      GL11.glTranslated(x, y, z)
      //      GL11.glDisable(GL11.GL_DEPTH_TEST)
      GL11.glScaled(1.01, 1.01, 1.01)
      GL11.glTranslated(-.005, -.005, -.005)
      EnumFacing.VALUES.foreach { facing =>
        GL11.glColor4f(1, 1, 1, 1)
        Minecraft.getMinecraft.getTextureManager.bindTexture(CONFIG_OUTLINE)
        RenderUtils.drawArbitraryFace(0, 0, 0, 0, 1, 0, 1, 0, 1, facing, null, 0, 1, 0, 1)
        val color = colorForStorage(facing)
        GL11.glColor4ub(color.red, color.green, color.blue, color.alpha)
        Minecraft.getMinecraft.getTextureManager.bindTexture(CONFIG_STORAGE_TEXTURE())
        RenderUtils.drawArbitraryFace(0, 0, 0, 0, 1, 0, 1, 0, 1, facing, null, 0, 1, 0, 1)

        GL11.glColor4f(1f, 1f, 1f, 1f)
        (ioForFacing(facing) match {
          case EnumAutomaticIO.NONE => None
          case EnumAutomaticIO.INPUT => Some(CONFIG_IO_TEXTURE_INPUT)
          case EnumAutomaticIO.OUTPUT => Some(CONFIG_IO_TEXTURE_OUTPUT)
        }).foreach { loc =>
          Minecraft.getMinecraft.getTextureManager.bindTexture(loc())
          val min = 4 / 16f
          val max = 12 / 16f
          (facing match {
            case EnumFacing.UP => RenderUtils.drawTopFace _
            case EnumFacing.DOWN => RenderUtils.drawBottomFace _
            case EnumFacing.NORTH => RenderUtils.drawNorthFace _
            case EnumFacing.EAST => RenderUtils.drawEastFace _
            case EnumFacing.SOUTH => RenderUtils.drawSouthFace _
            case EnumFacing.WEST => RenderUtils.drawWestFace _
          }) (0, 0, 0, min, max, min, max, if (facing.getAxisDirection == AxisDirection.POSITIVE) 1f else 0, null, 0, 1, 0, 1)
        }
      }
      //      GL11.glEnable(GL11.GL_DEPTH_TEST)
    }
    enableLightMap(te.getIWorld, te.getPos)
  }

  def disableLightMaps(): Unit = {
    GL11.glDisable(GL11.GL_LIGHTING)
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f)
  }

  def enableLightMap(world: IWorld, pos: BlockPos): Unit = {
    GL11.glEnable(GL11.GL_LIGHTING)
    RenderHelper.enableStandardItemLighting()
    val i = world.toMinecraft.getCombinedLight(pos, 0) // TODO
    setLightmapTexCoords(i)
  }

  private def setLightmapTexCoords(i: Int): Unit = {
    val j = i % 65536
    val k = i / 65536
    OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, j, k)
  }

  def renderNaniteConfigOverlay(te: ITileEntity, x: Double, y: Double, z: Double, sidedConfig: SidedNaniteStorageConfiguration): Unit = {
    renderConfigOverlay(te, x, y, z,
                        (facing) =>
                          GuiSidedNaniteConfig.colors(sidedConfig.storages.keys.toArray.indexOf(sidedConfig.getStorageNameForAbsoluteFacing(facing)) % GuiSidedNaniteConfig.colors.length),
                        (facing) => sidedConfig.getIOForAbsoluteFacing(facing))
  }

  def renderFluidConfigOverlay(te: ITileEntity, x: Double, y: Double, z: Double, sidedConfig: SidedFluidStorageConfiguration): Unit = {
    renderConfigOverlay(te, x, y, z,
                        (facing) =>
                          GuiSidedFluidConfig.colors(sidedConfig.storages.keys.toArray.indexOf(sidedConfig.getStorageNameForAbsoluteFacing(facing)) % GuiSidedFluidConfig.colors.length),
                        (facing) => sidedConfig.getIOForAbsoluteFacing(facing))
  }
}
