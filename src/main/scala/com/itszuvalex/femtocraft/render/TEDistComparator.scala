package com.itszuvalex.femtocraft.render

import java.util.Comparator

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.Minecraft
import net.minecraft.tileentity.TileEntity

/**
  * Created by Alex on 08.11.2015.
  */
@SideOnly(Side.CLIENT)
object TEDistComparator extends Comparator[TileEntity] {
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

  def entity = Minecraft.getMinecraft.renderViewEntity
}
