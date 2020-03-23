package com.itszuvalex.femtocraft.logistics.tile

import com.itszuvalex.femtocraft.api.nanite.{INanite, INaniteTank, NaniteTank}
import com.itszuvalex.femtocraft.industry.{ModuleINaniteTank, ModuleNaniteAutoIO, ModuleNaniteSidedConfiguration}
import com.itszuvalex.femtocraft.logistics.tile.TileNaniteRepository._
import com.itszuvalex.femtocraft.nanite.SidedNaniteStorageConfiguration
import com.itszuvalex.femtocraft.{Femtocraft, GuiIDs}
import com.itszuvalex.itszulib.api.wrappers.IWorld
import com.itszuvalex.itszulib.core.TileEntityCoreTickable
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviorHorizontalFacing
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}

object TileNaniteRepository {
  val REPOSITORY_VOLUME = 250
  val NANITE_TANK_KEY   = "Tank"
  val NONE_TANK_KEY     = "None"
}

class TileNaniteRepository extends TileEntityCoreTickable {
  val storage: INaniteTank = new NaniteTank(REPOSITORY_VOLUME) {
    override def canFill(nanite: INanite, vol: Int): Boolean = {
      nanitesInTank.isEmpty || containsNanite(nanite)
    }
  }
  val sidedNaniteConfig    = new SidedNaniteStorageConfiguration(_ => NANITE_TANK_KEY,
                                                                 Map(NONE_TANK_KEY -> INaniteTank.Empty,
                                                                     NANITE_TANK_KEY -> storage),
                                                                 () => world.getBlockState(pos).getValue(BlockBehaviorHorizontalFacing.FACING))

  addTileEntityModule(new ModuleINaniteTank(storage))
  addTileEntityModule(new ModuleNaniteSidedConfiguration(sidedNaniteConfig))
  addTileEntityModuleTickable(new ModuleNaniteAutoIO(sidedNaniteConfig))

  override def getMod = Femtocraft

  override def hasGUI: Boolean = true

  override def getGuiID: Int = GuiIDs.TileNaniteRepositoryGuiID

  override def hasDescription: Boolean = false

  // TODO
  override def onBlockActivated(world: IWorld, pos: BlockPos, state: IBlockState, playerIn: EntityPlayer, hand: EnumHand, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = false
}
