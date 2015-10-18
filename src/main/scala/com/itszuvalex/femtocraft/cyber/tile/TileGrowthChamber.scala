package com.itszuvalex.femtocraft.cyber.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.Cyber.tile.TileCyberBase
import com.itszuvalex.femtocraft.cyber.particle.FXWaterSpray
import com.itszuvalex.femtocraft.logistics.storage.item.{IndexedInventory, TileMultiblockIndexedInventory}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.MultiBlockComponent
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import com.itszuvalex.itszulib.render.{Point3D, Vector3}
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB

/**
 * Created by Alex on 30.09.2015.
 */
object TileGrowthChamber {
  val MACHINE_INDEX_KEY = "MachineIndex"
  val BASE_POS_COMPOUND_KEY = "BasePos"
}

class TileGrowthChamber extends TileEntityBase with MultiBlockComponent with TileMultiblockIndexedInventory {
  var machineIndex: Int = -1
  var basePos: Loc4     = null

  def onBlockBreak(): Unit = {
    if (!isValidMultiBlock) return
    basePos.getTileEntity() match {
      case Some(te: TileCyberBase) =>
        te.breakMachinesUpwardsFromSlot(te.machineSlotMap(machineIndex))
      case _ =>
    }
  }

  def particles1(time: Long): Unit = {
    val cx = 1
    val cy = 1.775 + (1 + math.sin(time * .05)) * .5 * .02924444461012775
    val cz = 1.6 - (1 + math.sin(time * .05)) * .5 * .08034845121081742
    val x1 = cx + .140625
    val x2 = cx + .046875
    val x3 = cx - .046375
    val x4 = cx - .140625
    val dirVec = new Vector3(new Point3D(1, 1.9f, 1.6f), new Point3D(cx.toFloat, cy.toFloat, cz.toFloat)) * -.8f
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x1, yCoord + cy, zCoord + cz, dirVec.x + .025, dirVec.y, dirVec.z, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x2, yCoord + cy, zCoord + cz, dirVec.x + .00833333333333333, dirVec.y, dirVec.z, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x3, yCoord + cy, zCoord + cz, dirVec.x - .00833333333333333, dirVec.y, dirVec.z, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x4, yCoord + cy, zCoord + cz, dirVec.x - .025, dirVec.y, dirVec.z, .04f, .2f, yCoord + .21))
  }

  def particles2(time: Long): Unit = {
    val d_xz = (1 + math.sin(time * .05 + 1)) * .5 * .08034845121081742
    val cx = .4804 + math.sin(math.toRadians(60)) * d_xz
    val cy = 1.775 + (1 + math.sin(time * .05 + 1)) * .5 * .02924444461012775
    val cz = .7 + math.cos(math.toRadians(60)) * d_xz
    val x1 = cx + .07031249999999999
    val z1 = cz - .12178482240718669
    val x2 = cx + .023437499999999997
    val z2 = cz - .04059494080239556
    val x3 = cx - .023437499999999997
    val z3 = cz + .04059494080239556
    val x4 = cx - .07031249999999999
    val z4 = cz + .12178482240718669
    val dirVec = new Vector3(new Point3D(.4804f, 1.9f, .7f), new Point3D(cx.toFloat, cy.toFloat, cz.toFloat)) * -.8f
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x1, yCoord + cy, zCoord + z1, dirVec.x + .012500000000000004, dirVec.y, dirVec.z - .021650635094610966, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x2, yCoord + cy, zCoord + z2, dirVec.x + .004166666500000001, dirVec.y, dirVec.z - .007216878076195187, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x3, yCoord + cy, zCoord + z3, dirVec.x - .004166666500000001, dirVec.y, dirVec.z + .007216878076195187, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x4, yCoord + cy, zCoord + z4, dirVec.x - .012500000000000004, dirVec.y, dirVec.z + .021650635094610966, .04f, .2f, yCoord + .21))
  }

  def particles3(time: Long): Unit = {
    val d_xz = (1 + math.sin(time * .05 + 2)) * .5 * .08034845121081742
    val cx = 1.5196 - math.sin(math.toRadians(60)) * d_xz
    val cy = 1.775 + (1 + math.sin(time * .05 + 2)) * .5 * .02924444461012775
    val cz = .7 + math.cos(math.toRadians(60)) * d_xz
    val x1 = cx + .07031249999999999
    val z1 = cz + .12178482240718669
    val x2 = cx + .023437499999999997
    val z2 = cz + .04059494080239556
    val x3 = cx - .023437499999999997
    val z3 = cz - .04059494080239556
    val x4 = cx - .07031249999999999
    val z4 = cz - .12178482240718669
    val dirVec = new Vector3(new Point3D(1.5196f, 1.9f, .7f), new Point3D(cx.toFloat, cy.toFloat, cz.toFloat)) * -.8f
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x1, yCoord + cy, zCoord + z1, dirVec.x + .012500000000000004, dirVec.y, dirVec.z + .021650635094610966, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x2, yCoord + cy, zCoord + z2, dirVec.x + .004166666500000001, dirVec.y, dirVec.z + .007216878076195187, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x3, yCoord + cy, zCoord + z3, dirVec.x - .004166666500000001, dirVec.y, dirVec.z - .007216878076195187, .04f, .2f, yCoord + .21))
    Minecraft.getMinecraft.effectRenderer.addEffect(new FXWaterSpray(worldObj, xCoord + x4, yCoord + cy, zCoord + z4, dirVec.x - .012500000000000004, dirVec.y, dirVec.z - .021650635094610966, .04f, .2f, yCoord + .21))
  }

  override def clientUpdate(): Unit = {
    if (!isController) return
    if (Minecraft.getMinecraft.gameSettings.particleSetting > 0) return
    val time = worldObj.getTotalWorldTime
    if (time % 3 != 0) return
    particles1(time)
    particles2(time)
    particles3(time)
  }

  override def getRenderBoundingBox = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 2, yCoord + 2, zCoord + 2)

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    compound.setInteger(TileGrowthChamber.MACHINE_INDEX_KEY, machineIndex)
    compound.setTag(TileGrowthChamber.BASE_POS_COMPOUND_KEY, NBTCompound(basePos))
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    machineIndex = compound.getInteger(TileGrowthChamber.MACHINE_INDEX_KEY)
    basePos = Loc4(compound.getCompoundTag(TileGrowthChamber.BASE_POS_COMPOUND_KEY))
  }

  override def getMod: AnyRef = Femtocraft

  override def defaultInventory: IndexedInventory = new IndexedInventory(6)

  override def hasDescription: Boolean = true
}
