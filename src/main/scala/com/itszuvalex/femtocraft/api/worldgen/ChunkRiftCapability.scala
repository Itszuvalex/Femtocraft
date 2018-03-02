package com.itszuvalex.femtocraft.api.worldgen

import com.itszuvalex.femtocraft.Femtocraft
import com.itszuvalex.femtocraft.api.Capabilities
import com.itszuvalex.femtocraft.worldgen.Rift
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.nbt.{NBTBase, NBTTagCompound, NBTTagList}
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.{Capability, CapabilityManager, ICapabilitySerializable}
import net.minecraftforge.event.AttachCapabilitiesEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

import scala.collection.mutable.ArrayBuffer

object ChunkRiftCapability {
  val RIFTS_KEY = "Rifts"

  def register(): Unit = {
    CapabilityManager.INSTANCE.register(classOf[IChunkRiftCapability], new ChunkRiftCapabilityStorage, classOf[ChunkRiftCapability])
    MinecraftForge.EVENT_BUS.register(this)
  }

  class ChunkRiftCapabilityStorage extends Capability.IStorage[IChunkRiftCapability] {
    override def writeNBT(capability: Capability[IChunkRiftCapability], instance: IChunkRiftCapability, side: EnumFacing): NBTBase = {
      instance match {
        case i: ICapabilitySerializable[NBTTagCompound] => i.serializeNBT()
        case _ => null
      }
    }

    override def readNBT(capability: Capability[IChunkRiftCapability], instance: IChunkRiftCapability, side: EnumFacing, nbt: NBTBase): Unit = {
      instance match {
        case i: ICapabilitySerializable[NBTTagCompound] => i.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
        case _ =>
      }
    }
  }

  @SubscribeEvent
  def attachCapability(event: AttachCapabilitiesEvent[Chunk]): Unit = {
    event.addCapability(new ResourceLocation(Femtocraft.ID.toLowerCase(), "ChunkRiftCapability"), new ChunkRiftCapability(event.getObject))
  }
}

class ChunkRiftCapability(val chunk: Chunk) extends IChunkRiftCapability with ICapabilitySerializable[NBTTagCompound] {
  private val riftBuffer = ArrayBuffer[IRift]()

  override def rifts: Iterable[IRift] = riftBuffer

  override def addRift(rift: IRift): Unit = {
    riftBuffer += rift

    if (!chunk.getWorld.isRemote)
      chunk.setModified(true)
  }

  override def removeRift(rift: IRift): Unit = {
    riftBuffer -= rift

    if (!chunk.getWorld.isRemote)
      chunk.setModified(true)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == Capabilities.CHUNK_RIFT_CAPABILITY)
      this.asInstanceOf[T]
    else
      null.asInstanceOf[T]
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = capability == Capabilities.CHUNK_RIFT_CAPABILITY

  override def deserializeNBT(nbt: NBTTagCompound): Unit = {
    riftBuffer.clear()
    val rifts = nbt.getTagList(ChunkRiftCapability.RIFTS_KEY, 10)
    riftBuffer ++= (0 until rifts.tagCount()).map(rifts.getCompoundTagAt).map { tag => val rift = new Rift(new Loc4(0, 0, 0, 0)); rift.deserializeNBT(tag); rift }
  }

  override def serializeNBT(): NBTTagCompound = {
    val ret = new NBTTagCompound
    val rifts = new NBTTagList
    riftBuffer.map(_.serializeNBT()).foreach(rifts.appendTag)
    ret.setTag(ChunkRiftCapability.RIFTS_KEY, rifts)
    ret
  }
}
