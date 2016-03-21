package com.itszuvalex.femtocraft.worldgen.block

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.TileDescriptionPacket
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.NBTTagCompound

import scala.util.Random

/**
  * Created by Alex on 08.08.2015.
  */

class TileCrystalsWorldgen extends TileEntityBase with TileDescriptionPacket {
  val COLOR_KEY          = "Color"
  val COLOR_OFFSET_KEY   = "ColorOffsets"
  val COLOR_COMPOUND_KEY = "ColorSettings"

  var color: Int = new Color(255.toByte,
                             (Random.nextInt(125) + 115).toByte,
                             (Random.nextInt(125) + 115).toByte,
                             (Random.nextInt(125) + 115).toByte).toInt

  var colorOffsets: Array[Int] = new Array[Int](11)
  (1 to 10).foreach(num => colorOffsets(num) = new Color(225.toByte,
                                                         Random.nextInt(30).toByte,
                                                         Random.nextInt(30).toByte,
                                                         Random.nextInt(30).toByte).toInt)

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    readColorData(compound)
    setRenderUpdate()
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    writeColorData(compound)
  }

  private def writeColorData(compound: NBTTagCompound) =
    compound(
              COLOR_COMPOUND_KEY ->
              NBTCompound(
                           COLOR_KEY -> color,
                           COLOR_OFFSET_KEY -> colorOffsets
                         )
            )

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    writeColorData(compound)
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    readColorData(compound)
  }

  private def readColorData(compound: NBTTagCompound) =
    compound.NBTCompound(COLOR_COMPOUND_KEY) { comp =>
      color = comp.Int(COLOR_KEY)
      colorOffsets = comp.IntArray(COLOR_OFFSET_KEY)
      Unit
                                             }

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true
}
