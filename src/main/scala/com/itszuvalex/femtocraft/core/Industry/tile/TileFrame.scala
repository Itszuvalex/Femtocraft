package com.itszuvalex.femtocraft.core.Industry.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileEntityBase
import net.minecraft.nbt.NBTTagCompound

/**
 * Created by Christopher on 8/29/2015.
 */
object TileFrame {
  val RENDER_SETTINGS_KEY = "RenderSettings"

  /**
   * Returns a settings array full with either true or false.
   * @param bool What to fill it with
   * @return Settings array (3-dim Boolean array)
   */
  def fullArray(bool: Boolean) = Array(
                                        Array(
                                               Array(bool, bool, bool, bool),
                                               Array(bool, bool, bool, bool),
                                               Array(bool, bool, bool, bool)
                                             ),
                                        Array(
                                               Array(bool, bool, bool, bool),
                                               Array(bool, bool, bool, bool)
                                             )
                                      )

  /**
   * Returns a 3-dim array of settings arrays representing a box. Minimum size 2x2x2.
   * @param sizeX X size, not less than 2
   * @param sizeY Y size, not less than 2
   * @param sizeZ Z size, not less than 2
   * @return 3-dim array of settings arrays (6-dim Boolean array)
   */
  def getBox(sizeX: Int, sizeY: Int, sizeZ: Int): Array[Array[Array[Array[Array[Array[Boolean]]]]]] = {
    val arr = Array.ofDim[Array[Array[Array[Boolean]]]](sizeX, sizeY, sizeZ)

    (0 until sizeX).foreach { case locX =>
      (0 until sizeY).foreach { case locY =>
        (0 until sizeZ).foreach { case locZ =>
          arr(locX)(locY)(locZ) = renderPieces(sizeX, sizeY, sizeZ, locX, locY, locZ)
                                }
                              }
                            }
    arr
  }

  def renderPieces(sizeX: Int, sizeY: Int, sizeZ: Int, locX: Int, locY: Int, locZ: Int) = {
    val MaxX = sizeX - 1
    val MaxY = sizeY - 1
    val MaxZ = sizeZ - 1
    //Commented if statements are not required :
    //  Match statements are ordered, and the if statements refer to previous
    //   case statements, so they will always be true regardless.
    //   The only difference in this change is the case where those variables
    //   are <0 or > maxZ.  Following this change, they will render as if they were in-bounds.
    //
    //Also, stable identifiers in match are backticked for syntactic redundancy.
    // This will match against MaxX's actual value, not assigning the name of MaxX to whatever is in
    // the pattern.
    (locX, locY, locZ) match {
      case (0, 0, 0) => Array(Array(Array(false, false, false, false), Array(true, false, false, false), Array(true, false, false, true)), Array(Array(true, false, false, false), Array(true, true, false, true)))
      case (`MaxX`, 0, 0) => Array(Array(Array(false, false, false, false), Array(false, true, false, false), Array(true, true, false, false)), Array(Array(false, true, false, false), Array(true, true, true, false)))
      case (0, `MaxY`, 0) => Array(Array(Array(true, false, false, true), Array(true, false, false, false), Array(false, false, false, false)), Array(Array(true, true, false, true), Array(true, false, false, false)))
      case (0, 0, `MaxZ`) => Array(Array(Array(false, false, false, false), Array(false, false, false, true), Array(false, false, true, true)), Array(Array(false, false, false, true), Array(true, false, true, true)))
      case (`MaxX`, `MaxY`, 0) => Array(Array(Array(true, true, false, false), Array(false, true, false, false), Array(false, false, false, false)), Array(Array(true, true, true, false), Array(false, true, false, false)))
      case (0, `MaxY`, `MaxZ`) => Array(Array(Array(false, false, true, true), Array(false, false, false, true), Array(false, false, false, false)), Array(Array(true, false, true, true), Array(false, false, false, true)))
      case (`MaxX`, 0, `MaxZ`) => Array(Array(Array(false, false, false, false), Array(false, false, true, false), Array(false, true, true, false)), Array(Array(false, false, true, false), Array(false, true, true, true)))
      case (`MaxX`, `MaxY`, `MaxZ`) => Array(Array(Array(false, true, true, false), Array(false, false, true, false), Array(false, false, false, false)), Array(Array(false, true, true, true), Array(false, false, true, false)))
      case (0, 0, `MaxZ`) => Array(Array(Array(false, false, false, false), Array(false, false, false, false), Array(false, false, false, true)), Array(Array(false, false, false, false), Array(true, false, false, true)))
      case (0, `MaxY`, _) /*if z > 0 && z < MaxZ*/ => Array(Array(Array(false, false, false, true), Array(false, false, false, false), Array(false, false, false, false)), Array(Array(true, false, false, true), Array(false, false, false, false)))
      case (`MaxX`, 0, _) /*if z > 0 && z < MaxZ*/ => Array(Array(Array(false, false, false, false), Array(false, false, false, false), Array(false, true, false, false)), Array(Array(false, false, false, false), Array(false, true, true, false)))
      case (`MaxX`, `MaxY`, _) /*if z > 0 && z < MaxZ*/ => Array(Array(Array(false, true, false, false), Array(false, false, false, false), Array(false, false, false, false)), Array(Array(false, true, true, false), Array(false, false, false, false)))
      case (0, _, 0) /*if y > 0 && y < MaxY*/ => Array(Array(Array(false, false, false, false), Array(true, false, false, false), Array(false, false, false, false)), Array(Array(true, false, false, false), Array(true, false, false, false)))
      case (0, _, `MaxZ`) /*if y > 0 && y < MaxY*/ => Array(Array(Array(false, false, false, false), Array(false, false, false, true), Array(false, false, false, false)), Array(Array(false, false, false, true), Array(false, false, false, true)))
      case (`MaxX`, _, 0) /*if y > 0 && y < MaxY*/ => Array(Array(Array(false, false, false, false), Array(false, true, false, false), Array(false, false, false, false)), Array(Array(false, true, false, false), Array(false, true, false, false)))
      case (`MaxX`, _, `MaxZ`) /*if y > 0 && y < MaxY*/ => Array(Array(Array(false, false, false, false), Array(false, false, true, false), Array(false, false, false, false)), Array(Array(false, false, true, false), Array(false, false, true, false)))
      case (_, 0, 0) /*if x > 0 && x < MaxX*/ => Array(Array(Array(false, false, false, false), Array(false, false, false, false), Array(true, false, false, false)), Array(Array(false, false, false, false), Array(true, true, false, false)))
      case (_, 0, `MaxZ`) /*if x > 0 && x < MaxX*/ => Array(Array(Array(false, false, false, false), Array(false, false, false, false), Array(false, false, true, false)), Array(Array(false, false, false, false), Array(false, false, true, true)))
      case (_, `MaxY`, 0) /*if x > 0 && x < MaxX*/ => Array(Array(Array(true, false, false, false), Array(false, false, false, false), Array(false, false, false, false)), Array(Array(true, true, false, false), Array(false, false, false, false)))
      case (_, `MaxY`, `MaxZ`) /*if x > 0 && x < MaxX*/ => Array(Array(Array(false, false, true, false), Array(false, false, false, false), Array(false, false, false, false)), Array(Array(false, false, true, true), Array(false, false, false, false)))
      case _ => fullArray(false)
    }
  }

  def bToI(boolean: Boolean): Int = {
    if (boolean) 1 else 0
  }

  def iToB(int: Int): Boolean = int != 0

  def getSaveableInt(arr: Array[Array[Array[Boolean]]]): Int = {
    var ret = 0
    ret += bToI(arr(0)(0)(0)) << 19
    ret += bToI(arr(0)(0)(1)) << 18
    ret += bToI(arr(0)(0)(2)) << 17
    ret += bToI(arr(0)(0)(3)) << 16
    ret += bToI(arr(0)(1)(0)) << 15
    ret += bToI(arr(0)(1)(1)) << 14
    ret += bToI(arr(0)(1)(2)) << 13
    ret += bToI(arr(0)(1)(3)) << 12
    ret += bToI(arr(0)(2)(0)) << 11
    ret += bToI(arr(0)(2)(1)) << 10
    ret += bToI(arr(0)(2)(2)) << 9
    ret += bToI(arr(0)(2)(3)) << 8
    ret += bToI(arr(1)(0)(0)) << 7
    ret += bToI(arr(1)(0)(1)) << 6
    ret += bToI(arr(1)(0)(2)) << 5
    ret += bToI(arr(1)(0)(3)) << 4
    ret += bToI(arr(1)(1)(0)) << 3
    ret += bToI(arr(1)(1)(1)) << 2
    ret += bToI(arr(1)(1)(2)) << 1
    ret += bToI(arr(1)(1)(3))
    ret
  }

  def getFromSaveableInt(int: Int): Array[Array[Array[Boolean]]] = {
    val ret = Array(
                     Array(
                            Array(iToB((int & 524288) >> 19), iToB((int & 262144) >> 18), iToB((int & 131072) >> 17), iToB((int & 65536) >> 16)),
                            Array(iToB((int & 32768) >> 15), iToB((int & 16384) >> 14), iToB((int & 8192) >> 13), iToB((int & 4096) >> 12)),
                            Array(iToB((int & 2048) >> 11), iToB((int & 1024) >> 10), iToB((int & 512) >> 9), iToB((int & 256) >> 8))
                          ),
                     Array(
                            Array(iToB((int & 128) >> 7), iToB((int & 64) >> 6), iToB((int & 32) >> 5), iToB((int & 16) >> 4)),
                            Array(iToB((int & 8) >> 3), iToB((int & 4) >> 2), iToB((int & 2) >> 1), iToB(int & 1))
                          )
                   )
    ret
  }
}

// "values" are all objects in that category, clockwise, starting from north/northwest (Boolean: render or not)
// Parameter:[Edges:[Top[values], Middle:[values], Bottom:[values]], Corners:[Top:[values], Bottom:[values]]]
class TileFrame() extends TileEntityBase {
  var thingsToRender = TileFrame.fullArray(true)

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound.setInteger(TileFrame.RENDER_SETTINGS_KEY, TileFrame.getSaveableInt(thingsToRender))
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    thingsToRender = TileFrame.getFromSaveableInt(compound.getInteger(TileFrame.RENDER_SETTINGS_KEY))
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound.setInteger(TileFrame.RENDER_SETTINGS_KEY, TileFrame.getSaveableInt(thingsToRender))
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    thingsToRender = TileFrame.getFromSaveableInt(compound.getInteger(TileFrame.RENDER_SETTINGS_KEY))
  }

}
