package com.itszuvalex.femtocraft.network

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.network.messages._
import com.itszuvalex.itszulib.network.PacketHandler
import net.minecraftforge.fml.relauncher.Side

/**
  * Created by Christopher Harris (Itszuvalex) on 4/6/15.
  */
object FemtoPacketHandler extends PacketHandler(Femtocraft.ID.toLowerCase) {
  def preInit(): Unit = {
    register(classOf[MessageMultiblockSelection], Side.SERVER)
    register(classOf[MessageOpenGui], Side.SERVER)
    register(classOf[MessageRequestSyncs], Side.SERVER)
    register(classOf[MessageNaniteCapabilities], Side.CLIENT)
    register(classOf[MessageDrainNanite], Side.SERVER)
    register(classOf[MessageNaniteTeleport], Side.CLIENT)
    register(classOf[MessageSidedInventoryConfigChange], Side.SERVER)
    register(classOf[MessageSidedInventoryIOChange], Side.SERVER)
  }
}
