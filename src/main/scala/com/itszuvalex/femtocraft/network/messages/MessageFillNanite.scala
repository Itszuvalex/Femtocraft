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

object MessageFillNanite {
  val LOC_KEY   = "Loc"
  val STACK_KEY = "Stack"
}

class MessageFillNanite(var loc: Loc4, var nanite: NaniteStackOLD) extends MessageUpdateNBT[MessageFillNanite, IMessage]({
  val compound = new NBTTagCompound
  compound.setTag(MessageFillNanite.LOC_KEY, loc.serializeNBT())
  if (nanite != null)
    compound.setTag(MessageFillNanite.STACK_KEY, nanite.serializeNBT())
  compound
}) {

  def this() = this(Loc4(0, 0, 0, 0), null)

  override def onMessage(message: MessageFillNanite, ctx: MessageContext): IMessage = {
    ItszuLib.proxy.addScheduledTask(() => {
      // Parse nbt
      message.loc = Loc4(message.nbt.getCompoundTag(MessageFillNanite.LOC_KEY))
      message.nanite = if (message.nbt.hasKey(MessageFillNanite.STACK_KEY)) NaniteStackOLD.loadFromNBT(message.nbt.getCompoundTag(MessageFillNanite.STACK_KEY)) else null

      // Do things
      message.loc.getITileEntity() match {
        case Some(tile: ITileEntity) if tile.hasModule(ManagerModules.TILE_NANITE_STORAGE_TANK_OLD, null) =>
          val storageTank = tile.getModule(ManagerModules.TILE_NANITE_STORAGE_TANK_OLD, null)
          if (storageTank != null) {
            if (ctx.getServerHandler.player.hasCapability(Capabilities.PLAYER_NANITE_CAPABILITY, EnumFacing.UP)) {
              val capability = ctx.getServerHandler.player.getCapability[IPlayerNaniteCapability](Capabilities.PLAYER_NANITE_CAPABILITY, EnumFacing.UP)
              capability.tank.nanitesInTank.foreach { nanite =>
                val amount = capability.tank.drain(nanite, 1, false)
                if (amount != null) {
                  val filled = storageTank.fill(amount, true)
                  capability.tank.drain(amount.nanite, amount.volume - (if (filled != null) filled.volume else 0), true)
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
