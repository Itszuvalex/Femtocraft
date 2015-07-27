package com.itszuvalex.femtocraft.network

import com.itszuvalex.femtocraft.Femtocraft
import cpw.mods.fml.common.network.NetworkRegistry

/**
 * Created by Christopher Harris (Itszuvalex) on 4/6/15.
 */
object PacketHandler {
  val INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Femtocraft.ID.toLowerCase)

  def init(): Unit = {
  }

}
