package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.logistics.TileNetworkNode
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.util.ForgeDirection

/**
  * Created by Christopher Harris (Itszuvalex) on 1/30/2016.
  */
object TileNetworkTest {
  val range = 32f
}

class TileNetworkTest extends TileEntityBase with TileNetworkNode[TileNetworkTest, TestTrackingNetwork] {
  var seek = true
  network = ManagerTestNetwork.NewNetwork()
  network.register()

  override def getMod = Femtocraft

  override def hasDescription = false


  override def serverUpdate(): Unit = {
    super.serverUpdate()
    if (seek) {
      seek = false
      val locs = ForgeDirection.VALID_DIRECTIONS.map(getLoc.getOffset(_))
      ForgeDirection.VALID_DIRECTIONS.map(getLoc.getOffset(_)).flatMap(_.getTileEntity(false)).collect { case i: TileNetworkTest => i }.
      foreach { i =>
        getNetwork.addConnection(getLoc, i.getLoc)
              }
    }
  }

  override def validate(): Unit = {
    super.validate()
    network.addNode(this)
    ManagerTestNetwork.tracker.trackLocation(getLoc)
  }

  override def invalidate(): Unit = {
    super.invalidate()
    network.removeNode(this)
    ManagerTestNetwork.tracker.removeLocation(getLoc)
  }

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: Int): Boolean = {
    super.onSideActivate(par5EntityPlayer, side)
    if (!worldObj.isRemote)
      PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Network ID:" + getNetwork.id)
    true
  }
}
