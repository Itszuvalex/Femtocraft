package com.itszuvalex.femtocraft.network.messages

import com.itszuvalex.femtocraft.api.nanite.NaniteStackOLD
import com.itszuvalex.femtocraft.api.{Capabilities, ManagerModules}
import com.itszuvalex.femtocraft.player.IPlayerNaniteCapability
import com.itszuvalex.itszulib.ItszuLib
import com.itszuvalex.itszulib.api.core.Loc4
import com.itszuvalex.itszulib.api.wrappers.ITileEntity
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

class MessageDrainNanite(var loc: Loc4, var nanite: NaniteStackOLD) extends MessageUpdateNBT[MessageDrainNanite, IMessage]({
  val compound = new NBTTagCompound
  compound.setTag(MessageDrainNanite.LOC_KEY, loc.serializeNBT())
  if (nanite != null)
    compound.setTag(MessageDrainNanite.STACK_KEY, nanite.serializeNBT())
  compound
}) {

  def this() = this(Loc4(0, 0, 0, 0), null)

  override def onMessage(message: MessageDrainNanite, ctx: MessageContext): IMessage = {
    ItszuLib.proxy.addScheduledTask(() => {
      // Parse nbt
      message.loc = Loc4(message.nbt.getCompoundTag(MessageDrainNanite.LOC_KEY))
      message.nanite = if (message.nbt.hasKey(MessageDrainNanite.STACK_KEY)) NaniteStackOLD.loadFromNBT(message.nbt.getCompoundTag(MessageDrainNanite.STACK_KEY)) else null

      // Do things
      message.loc.getITileEntity() match {
        case Some(tile: ITileEntity) if tile.hasModule(ManagerModules.TILE_NANITE_STORAGE_TANK_OLD, null) =>
          val storageTank = tile.getModule(ManagerModules.TILE_NANITE_STORAGE_TANK_OLD, null)
          if (storageTank != null) {
            val nanites = storageTank.nanitesInTank
            if (nanites.nonEmpty && ctx.getServerHandler.player.hasCapability(Capabilities.PLAYER_NANITE_CAPABILITY, EnumFacing.UP)) {
              val capability = ctx.getServerHandler.player.getCapability[IPlayerNaniteCapability](Capabilities.PLAYER_NANITE_CAPABILITY, EnumFacing.UP)
              nanites.foreach { nanite =>
                val amount = storageTank.drain(nanite, 1, false)
                if (amount != null) {
                  val filled = capability.tank.fill(amount, true)
                  storageTank.drain(amount.nanite, amount.volume - (if (filled != null) filled.volume else 0), true)
                  capability.sync()
                }
              }
            }
          }
        case _ =>
      }
    })
    null
  }

}
