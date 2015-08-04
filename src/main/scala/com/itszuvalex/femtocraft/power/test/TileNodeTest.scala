package com.itszuvalex.femtocraft.power.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.PowerNode
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
abstract class TileNodeTest extends TileEntityBase with PowerNode {
  override def getMod = Femtocraft

  override def initializePowerSettings(): Unit = {
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    val ret = super.onSideActivate(par5EntityPlayer, side)
    if (worldObj.isRemote) return ret
    val loc = getNodeLoc
    val parentLoc = getParentLoc
    val children = getChildren
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Location is: " + loc)
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Parent is: " + (if (parentLoc == null) "null" else parentLoc))
    PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Children(" + (if (children == null) "leaf" else children.size) + "):")
    if (children != null) children.foreach(loc => PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "    " + loc.getNodeLoc))
    ret
  }

  override def hasDescription = false
}
