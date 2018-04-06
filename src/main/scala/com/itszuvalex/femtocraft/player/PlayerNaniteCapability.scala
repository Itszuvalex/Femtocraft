package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.api.nanite.NaniteTank
import com.itszuvalex.femtocraft.network.FemtoPacketHandler
import com.itszuvalex.femtocraft.network.messages.MessageNaniteCapabilities
import net.minecraft.entity.Entity
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.nbt.{NBTBase, NBTTagCompound}
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities._
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
  * Created by Chris on 8/21/2016.
  */
object PlayerNaniteCapability {
  val tankVolume = 100

  def register(): Unit = {
    CapabilityManager.INSTANCE.register(classOf[IPlayerNaniteCapability], new PlayerNaniteCapabilitiesStorage, classOf[PlayerNaniteCapability])
    MinecraftForge.EVENT_BUS.register(this)
  }

  @SubscribeEvent
  def attachCapability(event: AttachCapabilitiesEvent[Entity]): Unit = {
    event.getObject match {
      case player: EntityPlayer =>
        event.addCapability(new ResourceLocation(Femtocraft.ID.toLowerCase(), "PlayerNaniteCapabilities"), new PlayerNaniteCapability(player))
      case _ =>
    }
  }

  class PlayerNaniteCapabilitiesStorage extends Capability.IStorage[IPlayerNaniteCapability] {
    override def writeNBT(capability: Capability[IPlayerNaniteCapability], instance: IPlayerNaniteCapability, side: EnumFacing): NBTBase = {
      instance match {
        case i: ICapabilitySerializable[NBTTagCompound] => i.serializeNBT()
        case _ => null
      }
    }

    override def readNBT(capability: Capability[IPlayerNaniteCapability], instance: IPlayerNaniteCapability, side: EnumFacing, nbt: NBTBase): Unit = {
      instance match {
        case i: ICapabilitySerializable[NBTTagCompound] => i.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
        case _ =>
      }
    }
  }
}

class PlayerNaniteCapability(player: EntityPlayer) extends IPlayerNaniteCapability with ICapabilitySerializable[NBTTagCompound] {
  private val _tank = new NaniteTank(PlayerNaniteCapability.tankVolume)

  def this() = this(null)

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    tank.deserializeNBT(nbt.getCompoundTag("tank"))
  }

  override def tank = _tank

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setTag("tank", tank.serializeNBT())
    nbt
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.PLAYER_NANITE_CAPABILITY)
      this.asInstanceOf[T]
    else
      null.asInstanceOf[T]
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == Capabilities.PLAYER_NANITE_CAPABILITY

  override def sync() = {
    player match {
      case null =>
      case pmp: EntityPlayerMP =>
        FemtoPacketHandler.INSTANCE.sendTo(new MessageNaniteCapabilities(this), pmp)
      case _ =>
    }
  }
}
