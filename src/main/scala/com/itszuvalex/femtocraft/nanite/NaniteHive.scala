package com.itszuvalex.femtocraft.nanite

import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTAdditions._
import com.itszuvalex.itszulib.implicits.NBTHelpers.NBTLiterals._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity

import scala.collection._

/**
  * Created by Christopher Harris (Itszuvalex) on 8/25/15.
  */
object NaniteHive {
  val HIVE_COMPOUND_KEY = "NaniteHive"
  val NODE_CHILDREN_KEY = "Children"
}


trait NaniteHive extends TileEntity with INaniteHive {
  val childrenNodeLocs = mutable.HashSet[Loc4]()

  override def addNode(node: INaniteNode) = {
    if (node == null) true
    else {
      childrenNodeLocs += node.getNodeLoc
      true
    }
  }

  override def getHiveLoc: Loc4 = new Loc4(this)

  override def canAddNode(node: INaniteNode): Boolean = true

  override def removeNode(node: INaniteNode): Unit = {
    if (node == null) return
    if (childrenNodeLocs.contains(node.getNodeLoc)) {
      childrenNodeLocs -= node.getNodeLoc
    }
  }

  override def getNodes: Iterable[INaniteNode] = childrenNodeLocs.flatMap(_.getTileEntity(true)).collect { case node: INaniteNode => node }

  override def getNodeLocs: Set[Loc4] = childrenNodeLocs

  override def writeToNBT(compound: NBTTagCompound): Unit = {
    super.writeToNBT(compound)
    saveChildrenInfo(compound)
  }

  def saveChildrenInfo(compound: NBTTagCompound) =
    compound(NaniteHive.HIVE_COMPOUND_KEY ->
             NBTCompound(
                          NaniteHive.NODE_CHILDREN_KEY -> NBTList(childrenNodeLocs.map(NBTCompound))
                        )
            )

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    loadChildrenInfo(compound)
  }

  def loadChildrenInfo(compound: NBTTagCompound) = {
    compound.NBTCompound(NaniteHive.HIVE_COMPOUND_KEY) { comp =>
      childrenNodeLocs.clear()
      childrenNodeLocs ++= comp.NBTList(NaniteHive.NODE_CHILDREN_KEY).map(Loc4(_))
                                                       }
  }

}
