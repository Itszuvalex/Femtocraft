package com.itszuvalex.femtocraft.industry.item

import com.itszuvalex.itszulib.api.wrappers.IItemStack
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.EntityPlayer

/**
  * Created by Chris on 12/11/2016.
  */
trait IMultiToolUpgrade {

  def name: String

  def toolTypes(item: IItemStack): Set[String]

  /**
    *
    * @param item
    *
    * @return True if activated via left click.  False if right-click.
    */
  def isPrimary(item: IItemStack): Boolean

  /**
    *
    * @param item
    *
    * @return True if this tool can be swapped to automatically via the MultiTool
    */
  def isAutoSwitchable(item: IItemStack): Boolean

  /**
    *
    * @param stack
    * @param toolClass
    * @param player
    * @param blockState
    * @return
    */
  def getHarvestLevel(stack: IItemStack, toolClass: String, @javax.annotation.Nullable player: EntityPlayer, @javax.annotation.Nullable blockState: IBlockState): Int

}
