package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.api.nanite.{INaniteOLD, INaniteTankOLD, NaniteTankOLD}
import com.itszuvalex.femtocraft.industry.{ModuleINaniteTank, ModuleNaniteAutoIO, ModuleNaniteSidedConfiguration}
import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository._
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import com.itszuvalex.itszulib.core.modules.ModuleGui

object TileNaniteRepository {
  val REPOSITORY_VOLUME = 250
  val NANITE_TANK_KEY   = "Tank"
  val NONE_TANK_KEY     = "None"
}

class TileNaniteRepository extends TileEntityCoreTickable {
  val storage: INaniteTankOLD = new NaniteTankOLD(REPOSITORY_VOLUME) {
    override def canFill(nanite: INaniteOLD, vol: Int): Boolean = {
      nanitesInTank.isEmpty || containsNanite(nanite)
    }
  }
  val sidedNaniteConfig       = new SidedNaniteStorageConfiguration(_ => NANITE_TANK_KEY,
                                                                 Map(NONE_TANK_KEY -> INaniteTankOLD.Empty,
                                                                     NANITE_TANK_KEY -> storage),
                                                                 () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))

  addTileEntityModule(new ModuleINaniteTank(storage))
  addTileEntityModule(new ModuleNaniteSidedConfiguration(sidedNaniteConfig))
  addTileEntityModule(new ModuleGui(Femtocraft, GuiIDs.TileNaniteRepositoryGuiID _))
  addTileEntityModuleTickable(new ModuleNaniteAutoIO(sidedNaniteConfig))
}
