package com.itszuvalex.femtocraft.player

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.nanite.NaniteTank
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.{NBTBase, NBTTagCompound}
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities._
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

/**
  * Created by Chris on 8/21/2016.
  */
object PlayerNaniteCapabilities {
  val tankVolume = 100

  @CapabilityInject(classOf[IPlayerNaniteCapabilities])
  val NANITE_CAPABILITY: Capability[IPlayerNaniteCapabilities] = null

  def register(): Unit = {
    CapabilityManager.INSTANCE.register(classOf[IPlayerNaniteCapabilities], new PlayerNaniteCapabilitiesStorage, classOf[PlayerNaniteCapabilities])
    MinecraftForge.EVENT_BUS.register(this)
  }

  class PlayerNaniteCapabilitiesStorage extends Capability.IStorage[IPlayerNaniteCapabilities] {
    override def writeNBT(capability: Capability[IPlayerNaniteCapabilities], instance: IPlayerNaniteCapabilities, side: EnumFacing): NBTBase = {
      instance match {
        case i: ICapabilitySerializable[NBTTagCompound] => i.serializeNBT()
        case _ => null
      }
    }

    override def readNBT(capability: Capability[IPlayerNaniteCapabilities], instance: IPlayerNaniteCapabilities, side: EnumFacing, nbt: NBTBase): Unit = {
      instance match {
        case i: ICapabilitySerializable[NBTTagCompound] => i.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
        case _ =>
      }
    }
  }

  @SubscribeEvent
  def attachCapability(event: AttachCapabilitiesEvent.Entity): Unit = {
    if (event.getEntity.isInstanceOf[EntityPlayer])
      event.addCapability(new ResourceLocation(Femtocraft.ID.toLowerCase(), "PlayerNaniteCapabilities"), new PlayerNaniteCapabilities())
  }
}

class PlayerNaniteCapabilities extends IPlayerNaniteCapabilities with ICapabilitySerializable[NBTTagCompound] {
  val _tank = new NaniteTank(PlayerNaniteCapabilities.tankVolume)

  override def tank = _tank

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    tank.deserializeNBT(nbt.getCompoundTag("tank"))
  }

  override def serializeNBT(): NBTTagCompound = {
    val nbt = new NBTTagCompound
    nbt.setTag("tank", tank.serializeNBT())
    nbt
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == PlayerNaniteCapabilities.NANITE_CAPABILITY)
      this.asInstanceOf[T]
    else
      null.asInstanceOf[T]
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == PlayerNaniteCapabilities.NANITE_CAPABILITY
}
