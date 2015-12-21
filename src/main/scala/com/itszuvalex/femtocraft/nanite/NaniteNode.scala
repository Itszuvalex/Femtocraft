package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity

/**
  * Created by Christopher Harris (Itszuvalex) on 8/25/15.
  */
object NaniteNode {
  val NODE_COMPOUND_KEY = "NaniteNode"
  val NODE_PARENT_KEY   = "Parent"
}

trait NaniteNode extends TileEntity with INaniteNode {
  var parentLoc: Loc4 = null

  override def canSetHive(hive: INaniteHive): Boolean = true

  override def getHiveLoc = parentLoc

  override def getHive = if(getHiveLoc == null) null else getHiveLoc.getTileEntity(force = true) match {
    case Some(i : INaniteHive) => i
    case _ => null
  }

  override def setHive(hive: INaniteHive) = {
    if (hive == null) {
      parentLoc = null
    }
    else {
      parentLoc = hive.getHiveLoc
    }
    NaniteManager.refreshParentlessStatus(this)
    true
  }

  override def getNodeLoc = new Loc4(this)

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    saveParentInfo(compound)
  }

  def saveParentInfo(compound: NBTTagCompound) =
    compound(NaniteNode.NODE_COMPOUND_KEY ->
             NBTCompound(
                          NaniteNode.NODE_PARENT_KEY -> parentLoc
                        )
            )

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    loadParentInfo(compound)
  }

  def loadParentInfo(compound: NBTTagCompound) =
    compound.NBTCompound(NaniteNode.NODE_COMPOUND_KEY) { comp =>
      parentLoc = comp.NBTCompound(NaniteNode.NODE_PARENT_KEY)(Loc4(_))
      Unit
                                                       }
}
