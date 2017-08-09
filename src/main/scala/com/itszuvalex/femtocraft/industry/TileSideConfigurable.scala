package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.render.RenderUtils
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{EnumFacing, ResourceLocation}

//TODO: move to ItszuLib and adjust TileEntityRenderCube
trait TileSideConfigurable extends TileEntityBase {
  private var _states = new Array[Int](6)
  def faceStates = _states
  val maxFaceState: Int

  def frontFace: EnumFacing
  def frontConfigurable: Boolean = false

  /**
    * @param face Face relative to front (front = north)
    */
  def renderFace(face: EnumFacing, x: Int, y: Int, partialTicks: Float, _3d: Boolean = false): Unit

  protected def drawFace(_3d: Boolean, face: EnumFacing, x: Int, y: Int, width: Int = 16, height: Int = 16) = {
    if (!_3d) RenderUtils.drawBlock(DefaultVertexFormats.POSITION_TEX) {
      RenderUtils.addVertexUV(x, y + height, 0, 0, 1f)
      RenderUtils.addVertexUV(x + width, y + height, 0, 1f, 1f)
      RenderUtils.addVertexUV(x + width, y, 0, 1f, 0)
      RenderUtils.addVertexUV(x, y, 0, 0, 0)
    }
    else RenderUtils.drawArbitraryFace(0, 0, 0, 0, 1, 0, 1, 0, 1, face, null, 0, 1, 0, 1)
  }

  override def hasDescription: Boolean = true

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound.setIntArray("SideConfig", _states)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    _states = compound.getIntArray("SideConfig")
  }
}
