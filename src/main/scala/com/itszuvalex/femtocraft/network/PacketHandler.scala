package com.itszuvalex.femtocraft.network

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.core.Initializable
import cpw.mods.fml.common.network.NetworkRegistry

/**
 * Created by Christopher Harris (Itszuvalex) on 4/6/15.
 */
object PacketHandler extends Initializable {
  val INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Femtocraft.ID.toLowerCase)

  override def preInit(): Unit = {
  }

}
