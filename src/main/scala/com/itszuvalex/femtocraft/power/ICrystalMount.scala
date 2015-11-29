package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.power.node.IPowerNode
import com.itszuvalex.itszulib.api.core.Loc4
import net.minecraft.item.ItemStack

/**
  * Created by Christopher on 8/27/2015.
  */
trait ICrystalMount extends IPowerNode {

  /**
    *
    * @return Crystal ItemStack.  Null if no crystal.
    */
  def getCrystalStack: ItemStack

  /**
    *
    * @param loc Location of pedestal to connect with.
    * @return True if this block can accept a pedestal connection from this location.
    */
  def canAcceptPedestal(loc: Loc4): Boolean

  /**
    *
    * @param loc Location to add as pedestal
    */
  def addPedestal(loc: Loc4): Unit

  /**
    *
    * @param loc Location to remove pedestal from.
    */
  def removePedestal(loc: Loc4): Unit

  /**
    *
    * @return Set of all locations that have pedestal connections.
    */
  def getPedestalDirections: scala.collection.Set[Loc4]
}
