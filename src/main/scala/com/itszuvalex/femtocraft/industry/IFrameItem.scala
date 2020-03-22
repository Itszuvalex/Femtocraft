package com.itszuvalex.femtocraft.industry

import com.itszuvalex.itszulib.api.client.IPreviewable
import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraft.item.Item

/**
  * Created by Christopher on 8/26/2015.
  */
trait IFrameItem extends Item with IPreviewable {

  def getFrameType(stack: IItemStack): String

  def getSelectedMultiblock(stack: IItemStack): String

  def setSelectedMultiblock(stack: IItemStack, multi: String)

}
