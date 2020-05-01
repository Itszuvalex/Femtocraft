package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.api.nanite.{INaniteTankOLD, NaniteTankOLD}
import com.itszuvalex.itszulib.container.sync.SyncBase
import net.minecraft.nbt.{NBTBase, NBTTagCompound}

/**
  * Created by Chris on 1/31/2017.
  */
object SyncINaniteTank {
  def tankEquals(a: INaniteTankOLD, b: INaniteTankOLD): Boolean = {
    if (a == null && b == null) return true
    if ((a == null) != (b == null)) return false
    if (a.volume != b.volume) return false
    if (a.volumeFilled != b.volumeFilled) return false

    // Only need to check one way, since we guarantee equal volume.  If we have equal volume, then we will hit a mismatch where a has a nanite b doesn't,
    // or the volumes for the nanites mismatch
    a.nanitesInTank.forall { n =>
      b.containsNanite(n) && a.volForNanite(n) == b.volForNanite(n)
    }
  }
}

class SyncINaniteTank
(gui: Int, valFunc: () => INaniteTankOLD, setValFunc: (INaniteTankOLD) => Unit)
  extends SyncBase[INaniteTankOLD](gui, valFunc, setValFunc, SyncINaniteTank.tankEquals) {
  override def writeNBT(): NBTBase = {
    value.serializeNBT()
  }

  override def handleNBT(nbt: NBTBase): Unit = {
    val tank = new NaniteTankOLD(0)
    tank.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
    value = tank
  }
}
