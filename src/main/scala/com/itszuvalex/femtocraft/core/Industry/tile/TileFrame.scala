package com.itszuvalex.femtocraft.core.Industry.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound

/**
 * Created by Christopher on 8/29/2015.
 */
object TileFrame {
  val RENDER_SETTINGS_KEY = "RenderSettings"

  def fullRender(bool: Boolean) = setRenderMarks(bool, 0, 0 until 20: _*)

  def getRenderMark(i: Int, j: Int, k: Int, renderInt: Int) = (renderInt & (1 << TileFrame.getSaveableIndentation(i, j, k))) > 0

  def setRenderMark(bool: Boolean, i: Int, j: Int, k: Int, marker: Int): Int = {
    val num = marker
    val i1 = 1 << getSaveableIndentation(i, j, k)
    if (bool) num | i1
    else num & ~i1
  }

  def setRenderMarks(bool: Boolean, marker: Int, markers: Int*): Int = {
    var num = marker
    markers.foreach(mark => num = setRenderMark(bool, mark / 12, (mark / 4) % 3, mark % 4, num))
    num
  }

  def renderPieces(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int) = {
    val MaxX = sizeX - 1
    val MaxY = sizeY - 1
    val MaxZ = sizeZ - 1
    //Also, stable identifiers in match are backticked for syntactic redundancy.
    // This will match against MaxX's actual value, not assigning the name of MaxX to whatever is in
    // the pattern.
    (locX, locY, locZ) match {
      case (0, 0, 0) => setRenderMarks(true, 0, 4, 8, 11, 12, 16, 17, 19)
      //        Array(
      //               Array(Array(false, false, false, false), Array(true, false, false, false), Array(true, false, false, true)),
      //               Array(Array(true, false, false, false), Array(true, true, false, true)))
      case (`MaxX`, 0, 0) => setRenderMarks(true, 0, 5, 8, 9, 13, 16, 17, 18)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, true, false, false), Array(true, true, false, false)),
      //               Array(Array(false, true, false, false), Array(true, true, true, false)))
      case (0, `MaxY`, 0) => setRenderMarks(true, 0, 0, 3, 4, 12, 13, 15, 16)
      //        Array(
      //               Array(Array(true, false, false, true), Array(true, false, false, false), Array(false, false, false, false)),
      //               Array(Array(true, true, false, true), Array(true, false, false, false)))
      case (0, 0, `MaxZ`) => setRenderMarks(true, 0, 7, 10, 11, 15, 16, 18, 19)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, false, false, true), Array(false, false, true, true)),
      //               Array(Array(false, false, false, true), Array(true, false, true, true)))
      case (`MaxX`, `MaxY`, 0) => setRenderMarks(true, 0, 0, 1, 5, 12, 13, 14, 17)
      //        Array(
      //               Array(Array(true, true, false, false), Array(false, true, false, false), Array(false, false, false, false)),
      //               Array(Array(true, true, true, false), Array(false, true, false, false)))
      case (0, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 2, 3, 7, 12, 14, 15, 19)
      //        Array(
      //               Array(Array(false, false, true, true), Array(false, false, false, true), Array(false, false, false, false)),
      //               Array(Array(true, false, true, true), Array(false, false, false, true)))
      case (`MaxX`, 0, `MaxZ`) => setRenderMarks(true, 0, 6, 9, 10, 14, 17, 18, 19)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, false, true, false), Array(false, true, true, false)),
      //               Array(Array(false, false, true, false), Array(false, true, true, true)))
      case (`MaxX`, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 1, 2, 6, 13, 14, 15, 18)
      //        Array(
      //               Array(Array(false, true, true, false), Array(false, false, true, false), Array(false, false, false, false)),
      //               Array(Array(false, true, true, true), Array(false, false, true, false)))
      case (0, 0, _) => setRenderMarks(true, 0, 11, 16, 19)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, false, false, false), Array(false, false, false, true)),
      //               Array(Array(false, false, false, false), Array(true, false, false, true)))
      case (0, `MaxY`, _) => setRenderMarks(true, 0, 3, 12, 15)
      //        Array(
      //               Array(Array(false, false, false, true), Array(false, false, false, false), Array(false, false, false, false)),
      //               Array(Array(true, false, false, true), Array(false, false, false, false)))
      case (`MaxX`, 0, _) => setRenderMarks(true, 0, 9, 17, 18)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, false, false, false), Array(false, true, false, false)),
      //               Array(Array(false, false, false, false), Array(false, true, true, false)))
      case (`MaxX`, `MaxY`, _) => setRenderMarks(true, 0, 1, 13, 14)
      //        Array(
      //               Array(Array(false, true, false, false), Array(false, false, false, false), Array(false, false, false, false)),
      //               Array(Array(false, true, true, false), Array(false, false, false, false)))
      case (0, _, 0) => setRenderMarks(true, 0, 4, 12, 16)
      //        Array(
      //               Array(Array(false, false, false, false), Array(true, false, false, false), Array(false, false, false, false)),
      //               Array(Array(true, false, false, false), Array(true, false, false, false)))
      case (0, _, `MaxZ`) => setRenderMarks(true, 0, 7, 15, 19)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, false, false, true), Array(false, false, false, false)),
      //               Array(Array(false, false, false, true), Array(false, false, false, true)))
      case (`MaxX`, _, 0) => setRenderMarks(true, 0, 5, 13, 17)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, true, false, false), Array(false, false, false, false)),
      //               Array(Array(false, true, false, false), Array(false, true, false, false)))
      case (`MaxX`, _, `MaxZ`) => setRenderMarks(true, 0, 6, 14, 18)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, false, true, false), Array(false, false, false, false)),
      //               Array(Array(false, false, true, false), Array(false, false, true, false)))
      case (_, 0, 0) => setRenderMarks(true, 0, 8, 16, 17)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, false, false, false), Array(true, false, false, false)),
      //               Array(Array(false, false, false, false), Array(true, true, false, false)))
      case (_, 0, `MaxZ`) => setRenderMarks(true, 0, 10, 18, 19)
      //        Array(
      //               Array(Array(false, false, false, false), Array(false, false, false, false), Array(false, false, true, false)),
      //               Array(Array(false, false, false, false), Array(false, false, true, true)))
      case (_, `MaxY`, 0) => setRenderMarks(true, 0, 0, 12, 13)
      //        Array(
      //               Array(Array(true, false, false, false), Array(false, false, false, false), Array(false, false, false, false)),
      //               Array(Array(true, true, false, false), Array(false, false, false, false)))
      case (_, `MaxY`, `MaxZ`) => setRenderMarks(true, 0, 2, 14, 15)
      //        Array(
      //               Array(Array(false, false, true, false), Array(false, false, false, false), Array(false, false, false, false)),
      //               Array(Array(false, false, true, true), Array(false, false, false, false)))
      case _ => fullRender(false)
    }
  }

  def getSaveableIndentation(i: Int, j: Int, k: Int) = {
    19 - (12 * i) - (4 * j) - k
  }

}

// "values" are all objects in that category, clockwise, starting from north/northwest (Boolean: render or not)
// Parameter:[Edges:[Top[values], Middle:[values], Bottom:[values]], Corners:[Top:[values], Bottom:[values]]]
class TileFrame() extends TileEntityBase {
  var renderInt = TileFrame.fullRender(true)

  def calculateRendering(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int) = {
    renderInt = TileFrame.renderPieces(sizeX, sizeY, sizeZ, locX, locY, locZ)
  }

  def getRenderMark(i: Int, j: Int, k: Int) = TileFrame.getRenderMark(i, j, k, renderInt)

  def setRenderMark(bool: Boolean, i: Int, j: Int, k: Int): Unit = {
    renderInt = TileFrame.setRenderMark(bool, i, j, k, renderInt)
  }

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound.setInteger(TileFrame.RENDER_SETTINGS_KEY, renderInt)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    renderInt = compound.getInteger(TileFrame.RENDER_SETTINGS_KEY)
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound.setInteger(TileFrame.RENDER_SETTINGS_KEY, renderInt)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    renderInt = compound.getInteger(TileFrame.RENDER_SETTINGS_KEY)
  }

}
