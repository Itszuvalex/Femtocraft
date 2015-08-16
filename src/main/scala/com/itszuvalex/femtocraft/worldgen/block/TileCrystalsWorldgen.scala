package com.itszuvalex.femtocraft.worldgen.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.NBTTagCompound

import scala.util.Random

/**
 * Created by Alex on 08.08.2015.
 */

class TileCrystalsWorldgen extends TileEntityBase {
  val COLOR_KEY = "Color"
  val COLOR_OFFSET_KEY = "ColorOffsets"
  val COLOR_COMPOUND_KEY = "ColorSettings"

  var color : Int = new Color(255.toByte,
                              (Random.nextInt(125) + 115).toByte,
                              (Random.nextInt(125) + 115).toByte,
                              (Random.nextInt(125) + 115).toByte  ).toInt

  var colorOffsets : Array[Int] = new Array[Int](11)
  (1 to 10).foreach( num => colorOffsets(num) = new Color(225.toByte,
                                                          Random.nextInt(30).toByte,
                                                          Random.nextInt(30).toByte,
                                                          Random.nextInt(30).toByte  ).toInt )

  override def writeToNBT(p_145841_1_ : NBTTagCompound): Unit = {
    super.writeToNBT(p_145841_1_)
    val colors = new NBTTagCompound
    colors.setInteger(COLOR_KEY, color)
    colors.setIntArray(COLOR_OFFSET_KEY, colorOffsets)
    p_145841_1_.setTag(COLOR_COMPOUND_KEY, colors)
  }

  override def readFromNBT(p_145839_1_ : NBTTagCompound): Unit = {
    super.readFromNBT(p_145839_1_)
    if (p_145839_1_.hasKey(COLOR_COMPOUND_KEY)) {
      val colors = p_145839_1_.getCompoundTag(COLOR_COMPOUND_KEY)
      color = colors.getInteger(COLOR_KEY)
      colorOffsets = colors.getIntArray(COLOR_OFFSET_KEY)
    }
  }

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = false
}
