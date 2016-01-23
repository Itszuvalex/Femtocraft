package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.{ICrystalMount, IPowerPedestal}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection

/**
  * Created by Christopher on 8/27/2015.
  */
object TilePowerPedestal {
  val PEDESTAL_COMPOUND = "Pedestal"
  val MOUNT_KEY         = "Mount"
}

class TilePowerPedestal extends TileEntityBase with IPowerPedestal {
  var mountLocation: Loc4 = null
  var stored       : Long = 0

  override def getMod: AnyRef = Femtocraft

  override def hasDescription: Boolean = true

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.writeToNBT(par1nbtTagCompound)
    savePowerInfo(par1nbtTagCompound)
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    loadPowerInfo(par1nbtTagCompound)
  }

  def loadPowerInfo(par1nbtTagCompound: NBTTagCompound): Unit = {
    par1nbtTagCompound.NBTCompound(TilePowerPedestal.PEDESTAL_COMPOUND) { comp =>
      mountLocation = comp.NBTCompound(TilePowerPedestal.MOUNT_KEY)(Loc4(_))
      Unit
                                                                        }
  }

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    savePowerInfo(compound)
  }

  def savePowerInfo(par1nbtTagCompound: NBTTagCompound): Unit = {
    par1nbtTagCompound(TilePowerPedestal.PEDESTAL_COMPOUND ->
                       NBTCompound(
                                    TilePowerPedestal.MOUNT_KEY -> mountLocation
                                  )
                      )
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadPowerInfo(compound)
    setRenderUpdate()
  }

  def onBlockBreak() = {
    if (mountLoc != null)
      mountLoc.getTileEntity(true) match {
        case Some(m: ICrystalMount) =>
          m.removePedestal(getLoc)
        case _ =>
      }
  }

  /**
    *
    * @return Location of the mount this is connected to.  UNKNOWN otherwise.
    */
  override def mountLoc: Loc4 = mountLocation

  def onPostBlockPlaced(): Unit = {
    if (getWorldObj.isRemote) return
    if (!checkAndAddMount(ForgeDirection.UP))
      checkAndAddMount(ForgeDirection.DOWN)
  }

  def checkAndAddMount(dir: ForgeDirection): Boolean = {
    getLoc.getOffset(dir).getTileEntity(true) match {
      case Some(i: ICrystalMount) =>
        if (i.canAcceptPedestal(getLoc) && canSetMount(getLoc.getOffset(dir))) {
          i.addPedestal(getLoc)
          setMount(getLoc.getOffset(dir))
          return true
        }
      case _ =>
    }
    false
  }

  /**
    *
    * @param loc Location to accept mount connection at.
    * @return True if mount can be added to this location.
    */
  override def canSetMount(loc: Loc4): Boolean = getLoc.getOffset(ForgeDirection.UP) == loc || getLoc.getOffset(ForgeDirection.DOWN) == loc

  /**
    *
    * @param loc Location of mount.
    */
  override def setMount(loc: Loc4): Unit = {
    mountLocation = loc
    setUpdate()
  }

}
