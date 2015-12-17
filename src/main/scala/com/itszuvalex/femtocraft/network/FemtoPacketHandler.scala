package com.itszuvalex.femtocraft.network

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.network.messages.{MessageBuildMachine, MessageGrowthChamberUpdate, MessageMultiblockSelection, MessageOpenGui}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.relauncher.Side

/**
  * Created by Christopher Harris (Itszuvalex) on 4/6/15.
  */
object FemtoPacketHandler {
  val INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Femtocraft.ID.toLowerCase)

  private var messageIndex = 1

  def preInit(): Unit = {
    INSTANCE.registerMessage(classOf[MessageMultiblockSelection], classOf[MessageMultiblockSelection], nextIndex, Side.SERVER)
    INSTANCE.registerMessage(classOf[MessageBuildMachine], classOf[MessageBuildMachine], nextIndex, Side.SERVER)
    INSTANCE.registerMessage(classOf[MessageGrowthChamberUpdate], classOf[MessageGrowthChamberUpdate], nextIndex, Side.CLIENT)
    INSTANCE.registerMessage(classOf[MessageOpenGui], classOf[MessageOpenGui], nextIndex, Side.SERVER)
  }

  private def nextIndex = {
    messageIndex += 1
    messageIndex - 1
  }

}
