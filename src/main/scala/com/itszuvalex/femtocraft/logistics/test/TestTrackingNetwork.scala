package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.itszulib.api.core.IModule
import com.itszuvalex.itszulib.logistics.{ManagerNetwork, TileNetwork}

/**
  * Created by Christopher Harris (Itszuvalex) on 1/30/2016.
  */
class TestTrackingNetwork(_id: Int) extends TileNetwork[TileNetworkTest, TestTrackingNetwork](_id) {
  override def create() = new TestTrackingNetwork(ManagerNetwork.instance.getNextID)


  override def networkModule: IModule[TileNetworkTest] = null

  override def onTakeover(iNetwork: TestTrackingNetwork): Unit = {

  }

  override def onSplit(iNetwork: TestTrackingNetwork): Unit = {

  }

  override def onTickStart(): Unit = {

  }

  override def onTickEnd(): Unit = {

  }
}
