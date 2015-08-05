package com.itszuvalex.femtocraft.power.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.power.node.{IPowerNode, PowerNode}
import com.itszuvalex.femtocraft.power.render.PowerNodeRenderer
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.core.traits.tile.DescriptionPacket
import com.itszuvalex.itszulib.render.Vector3
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.AxisAlignedBB

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
abstract class TileNodeTest extends TileEntityBase with PowerNode with DescriptionPacket {
  override def getMod = Femtocraft

  /**
   *
   * @param child
   * @return True if child was a child of this node, and was successfully removed.
   */
  override def removeChild(child: IPowerNode): Boolean = {
    val ret = super.removeChild(child)
    setUpdate()
    ret
  }

  override def getRenderBoundingBox: AxisAlignedBB = {
    val center = Vector3(xCoord + .5f, yCoord + .5f, zCoord + .5f)
    AxisAlignedBB.getBoundingBox(center.x - PowerNodeRenderer.RENDER_RADIUS,
                                 center.y - PowerNodeRenderer.RENDER_RADIUS,
                                 center.z - PowerNodeRenderer.RENDER_RADIUS,
                                 center.x + PowerNodeRenderer.RENDER_RADIUS,
                                 center.y + PowerNodeRenderer.RENDER_RADIUS,
                                 center.z + PowerNodeRenderer.RENDER_RADIUS)
  }

  /**
   *
   * @param child
   * @return True if child is successfully added.
   */
  override def addChild(child: IPowerNode): Boolean = {
    val ret = super.addChild(child)
    setUpdate()
    ret
  }

  /**
   *
   * @param parent Parent being set.
   * @return True if parent is successfully set to input parent.
   */
  override def setParent(parent: IPowerNode): Boolean = {
    val ret = super.setParent(parent)
    setUpdate()
    ret
  }

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

  override def hasDescription: Boolean = true

  override def saveToDescriptionCompound(compound: NBTTagCompound): Unit = {
    super.saveToDescriptionCompound(compound)
    savePowerConnectionInfo(compound)
  }

  override def handleDescriptionNBT(compound: NBTTagCompound): Unit = {
    super.handleDescriptionNBT(compound)
    loadPowerConnectionInfo(compound)
    setRenderUpdate()
  }
}
