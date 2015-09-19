package com.itszuvalex.femtocraft.network

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.network.messages.MessageMultiblockSelection
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.relauncher.Side

/**
 * Created by Christopher Harris (Itszuvalex) on 4/6/15.
 */
object PacketHandler {
  val INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Femtocraft.ID.toLowerCase)

  private var messageIndex = 1

  private def nextIndex = {
    messageIndex += 1
    messageIndex - 1
  }

  def preInit(): Unit = {
    INSTANCE.registerMessage(classOf[MessageMultiblockSelection], classOf[MessageMultiblockSelection], nextIndex, Side.SERVER)
  }

}
