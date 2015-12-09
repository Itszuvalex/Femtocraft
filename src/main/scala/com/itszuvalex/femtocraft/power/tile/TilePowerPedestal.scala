package com.itszuvalex.femtocraft.power.tile

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.{IPowerCrystal, ICrystalMount, IPowerPedestal}
import com.itszuvalex.itszulib.api.core.{Configurable, Loc4}
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.common.util.ForgeDirection

/**
  * Created by Christopher on 8/27/2015.
  */
@Configurable object TilePowerPedestal {
  @Configurable val MAX_STORE: Long   = 10000
                val PEDESTAL_COMPOUND = "Pedestal"
                val MOUNT_KEY         = "Mount"
                val STORED_KEY        = "Stored"
}

@Configurable class TilePowerPedestal extends TileEntityBase with IPowerPedestal {
  var mountLocation: Loc4 = null
  var stored       : Long = 0

  override def getMod: AnyRef = Femtocraft

  /**
    *
    * @param loc Location of mount.
    */
  override def setMount(loc: Loc4): Unit = mountLocation = loc

  /**
    *
    * @param amount Amount of power to attempt to consume.
    * @param doConsume Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully removed.
    */
  override def consume(amount: Long, doConsume: Boolean): Long = {
    val ret = Math.min(amount, getStored)
    if (doConsume)
      stored -= ret
    ret
  }

  /**
    *
    * @return Location of the mount this is connected to.  UNKNOWN otherwise.
    */
  override def mountLoc: Loc4 = mountLocation

  /**
    *
    * @param amount Amount of power to store.
    * @param doStore Pass true to actually consume resources.  False simulates the store.
    * @return Amount of @amount successfully stored.
    */
  override def store(amount: Long, doStore: Boolean): Long = {
    val ret = Math.min(amount, getMax - getStored)
    if (doStore)
      stored += ret
    ret
  }

  /**
    *
    * @return Amount of power stored.
    */
  override def getStored: Long = stored

  /**
    *
    * @return Amount of power capable of being stored.
    */
  override def getMax: Long = mountLoc match {
    case loc if loc != null => loc.getTileEntity(false).orNull match {
      case mount: ICrystalMount =>
        mount.getCrystalStack match {
          case stack if stack.getItem.isInstanceOf[IPowerCrystal] =>
            val cry = stack.getItem.asInstanceOf[IPowerCrystal]
            (TilePowerPedestal.MAX_STORE * cry.getStorageMultiplier(stack)).toLong
          case _ => TilePowerPedestal.MAX_STORE
        }
      case _ => TilePowerPedestal.MAX_STORE
    }
    case _ => TilePowerPedestal.MAX_STORE
  }

  /**
    *
    * @param loc Location to accept mount connection at.
    * @return True if mount can be added to this location.
    */
  override def canSetMount(loc: Loc4): Boolean = getLoc.getOffset(ForgeDirection.UP) == loc || getLoc.getOffset(ForgeDirection.DOWN) == loc

  override def hasDescription: Boolean = false

  override def writeToNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.writeToNBT(par1nbtTagCompound)
    par1nbtTagCompound(TilePowerPedestal.PEDESTAL_COMPOUND ->
                       NBTCompound(
                                    TilePowerPedestal.MOUNT_KEY -> mountLocation,
                                    TilePowerPedestal.STORED_KEY -> stored
                                  ))
  }

  override def readFromNBT(par1nbtTagCompound: NBTTagCompound): Unit = {
    super.readFromNBT(par1nbtTagCompound)
    par1nbtTagCompound.NBTCompound(TilePowerPedestal.PEDESTAL_COMPOUND) { comp =>
      mountLocation = comp.NBTCompound(TilePowerPedestal.MOUNT_KEY)(Loc4(_))
      stored = comp.Long(TilePowerPedestal.STORED_KEY)
      Unit
                                                                        }
  }
}
