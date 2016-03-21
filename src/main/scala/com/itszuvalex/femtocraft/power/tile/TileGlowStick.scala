package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.Color
import net.minecraft.nbt.NBTTagCompound

import scala.util.Random

/**
  * Created by Christopher Harris (Itszuvalex) on 1/29/2016.
  */
class TileGlowStick extends TileEntityBase {
  var color: Int = Color(255.toByte,
                         (Random.nextInt(125) + 130).toByte,
                         (Random.nextInt(125) + 130).toByte,
                         (Random.nextInt(125) + 130).toByte).toInt

  override def getMod = Femtocraft

  override def hasDescription = true

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    compound.setInteger("color", color)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    color = compound.getInteger("color")
    setRenderUpdate()
  }
}
