package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.itszulib.api.core.Loc4

/**
  * Created by Christopher Harris (Itszuvalex) on 8/15/15.
  */
trait ILogisticsConnected {

  def getConnections: scala.collection.Set[Loc4]

}
