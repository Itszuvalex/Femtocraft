package com.itszuvalex.femtocraft.logistics.test

import com.itszuvalex.femtocraft.{FemtoBlocks, Femtocraft}
import com.itszuvalex.itszulib.api.wrappers.{ITileEntity, IWorld}
import com.itszuvalex.itszulib.core.behaviors.BlockBehaviors
import com.itszuvalex.itszulib.core.{BlockTileContainer, TileBlockContainerCore}
import net.minecraft.block.material.Material

/**
  * Created by Christopher Harris (Itszuvalex) on 1/30/2016.
  */
class BlockNetworkTest extends TileBlockContainerCore(Material.IRON, new BlockTileContainer(() => FemtoBlocks.testNetworkBlock) {
  override def createTileEntity(world: IWorld, meta: Int): ITileEntity = new TileNetworkTest
}, BlockBehaviors.DEFAULT) {
  setCreativeTab(Femtocraft.tab)
}
