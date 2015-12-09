package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.tileentity.TileEntity

/**
  * Created by Christopher on 8/26/2015.
  */
trait IFrameTile extends TileEntity {

  def getType: String

  def getFormingMultiblock: String

  def getConnectedFrames: scala.collection.Set[Loc4]
}
