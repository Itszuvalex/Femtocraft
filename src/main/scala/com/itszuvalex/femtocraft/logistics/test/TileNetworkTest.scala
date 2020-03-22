package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.itszulib.core.TileEntityBase
import com.itszuvalex.itszulib.logistics.TileNetworkNode
import com.itszuvalex.itszulib.util.PlayerUtils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing

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
      val locs = EnumFacing.VALUES.map(getLoc.getOffset(_))
      EnumFacing.VALUES.map(getLoc.getOffset(_)).flatMap(_.getITileEntity(false)).collect { case i: TileNetworkTest => i }.
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

  override def onSideActivate(par5EntityPlayer: EntityPlayer, side: EnumFacing): Boolean = {
    super.onSideActivate(par5EntityPlayer, side)
    if (!world.isRemote)
      PlayerUtils.sendMessageToPlayer(par5EntityPlayer, Femtocraft.ID, "Network ID:" + getNetwork.id)
    true
  }
}
