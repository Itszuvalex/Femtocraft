package com.itszuvalex.femtocraft.render

import java.util.Comparator

import net.minecraft.entity.EntityLivingBase
import net.minecraft.tileentity.TileEntity

/**
  * Created by Alex on 08.11.2015.
  */
class TEDistComparator(entity: EntityLivingBase) extends Comparator[TileEntity] {
  override def compare(o1: TileEntity, o2: TileEntity): Int = {
    if (distSqr(o1) > distSqr(o2)) -1
    else if (distSqr(o1) < distSqr(o2)) 1
    else 0
  }

  def distSqr(te: TileEntity): Double = {
    val dx = math.abs(entity.posX - te.xCoord.toDouble)
    val dy = math.abs(entity.posY - te.yCoord.toDouble)
    val dz = math.abs(entity.posZ - te.zCoord.toDouble)
    dx * dx + dy * dy + dz * dz
  }
}
