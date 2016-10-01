package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.nanite.NaniteStack
import com.itszuvalex.femtocraft.nanite.tile.TileNaniteExtractor
import com.itszuvalex.femtocraft.player.{IPlayerNaniteCapabilities, PlayerNaniteCapabilities}
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.network.messages.MessageUpdateNBT
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.network.simpleimpl.{IMessage, MessageContext}

/**
  * Created by Alex on 15.10.2015.
  */
object MessageDrainNanite {
  val LOC_KEY   = "Loc"
  val STACK_KEY = "Stack"
}

class MessageDrainNanite(var loc: Loc4, var nanite: NaniteStack) extends MessageUpdateNBT[MessageDrainNanite, IMessage]({
  val compound = new NBTTagCompound
  compound.setTag(MessageDrainNanite.LOC_KEY, loc.serializeNBT())
  if (nanite != null)
    compound.setTag(MessageDrainNanite.STACK_KEY, nanite.serializeNBT())
  compound
}) {

  def this() = this(Loc4(0, 0, 0, 0), null)

  override def onMessage(message: MessageDrainNanite, ctx: MessageContext): IMessage = {
    // Parse nbt
    message.loc = Loc4(message.nbt.getCompoundTag(MessageDrainNanite.LOC_KEY))
    message.nanite = if (message.nbt.hasKey(MessageDrainNanite.STACK_KEY)) NaniteStack.loadFromNBT(message.nbt.getCompoundTag(MessageDrainNanite.STACK_KEY)) else null

    // Do things
    message.loc.getTileEntity() match {
      case Some(tile: TileNaniteExtractor) =>
        // TODO: Take Nanite to drain and amount to drain from nanite stack
        val storageTank = tile.getStorageTank
        val nanites = storageTank.nanitesInTank
        if (nanites.nonEmpty && ctx.getServerHandler.playerEntity.hasCapability(PlayerNaniteCapabilities.NANITE_CAPABILITY, EnumFacing.UP)) {
          val capability = ctx.getServerHandler.playerEntity.getCapability[IPlayerNaniteCapabilities](PlayerNaniteCapabilities.NANITE_CAPABILITY, EnumFacing.UP)
          val amount = storageTank.drain(nanites.head, 1, false)
          if (amount != null) {
            val filled = capability.tank.fill(amount, true)
            storageTank.drain(amount.nanite, amount.volume - (if (filled != null) filled.volume else 0), true)
            capability.sync()
          }
        }
      case _ =>
    }

    null
  }

}
