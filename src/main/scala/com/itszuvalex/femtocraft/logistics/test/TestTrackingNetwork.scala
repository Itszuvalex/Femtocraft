package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.itszulib.logistics.{INetwork, ManagerNetwork, TileNetwork}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/30/2016.
  */
class TestTrackingNetwork(_id: Int) extends TileNetwork[TileNetworkTest, TestTrackingNetwork](_id) {
  override def create() = new TestTrackingNetwork(ManagerNetwork.getNextID)

  override def onTakeover(iNetwork: INetwork[TileNetworkTest, TestTrackingNetwork]): Unit = {

  }

  override def onSplit(iNetwork: INetwork[TileNetworkTest, TestTrackingNetwork]): Unit = {

  }

  override def onTickStart(): Unit = {

  }

  override def onTickEnd(): Unit = {

  }
}
