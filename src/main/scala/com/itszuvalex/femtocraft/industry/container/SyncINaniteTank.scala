package com.itszuvalex.femtocraft.industry.container

import com.itszuvalex.femtocraft.api.nanite.{INaniteTank, NaniteTank}
import com.itszuvalex.itszulib.container.sync.SyncBase
import net.minecraft.nbt.{NBTBase, NBTTagCompound}

/**
  * Created by Chris on 1/31/2017.
  */
object SyncINaniteTank {
  def tankEquals(a: INaniteTank, b: INaniteTank): Boolean = {
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
(gui: Int, valFunc: () => INaniteTank, setValFunc: (INaniteTank) => Unit)
  extends SyncBase[INaniteTank](gui, valFunc, setValFunc, SyncINaniteTank.tankEquals) {
  override def writeNBT(): NBTBase = {
    value.serializeNBT()
  }

  override def handleNBT(nbt: NBTBase): Unit = {
    val tank = new NaniteTank(0)
    tank.deserializeNBT(nbt.asInstanceOf[NBTTagCompound])
    value = tank
  }
}
