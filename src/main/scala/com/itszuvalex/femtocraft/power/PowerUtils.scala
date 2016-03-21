package com.itszuvalex.femtocraft.power

import com.itszuvalex.itszulib.api.core.Loc4

import scala.annotation.tailrec

/**
  * Created by Christopher Harris (Itszuvalex) on 12/17/15.
  */
object PowerUtils {

  /**
    *
    * @param loc Location to search for
    * @param zigguratPieceLoc Location of any piece of the Ziggurat
    * @return True if loc contains a IPowerZigguratBlock and it is in the same Ziggurat as the ZigguratPieceLoc, false otherwise.
    */
  def isLocInZiggurat(loc: Loc4, zigguratPieceLoc: Loc4): Boolean = {
    val locRoot = getZigguratRootLoc(loc)
    locRoot != null && locRoot == getZigguratRootLoc(zigguratPieceLoc)
  }

  /**
    *
    * Used to prevent looping parent/child chains in Ziggurats.  Only add a child location to a block in a ziggurat, if this
    * function returns false when passed the child location and the to-be-parent's location. The only exception is if the root
    * node is setting the parent of itself to itself.
    *
    * @param loc location to search for
    * @param zigguratLoc Location containing an IPowerZigguratComponent
    * @return True if location is the location of any parents of the block in zigguratLoc
    */
  @tailrec
  def isLocInZigguratParentChain(loc: Loc4, zigguratLoc: Loc4): Boolean =
    if (loc == null) false
    else
      loc.getTileEntity(true) match {
        case _ if loc == zigguratLoc => true
        case Some(tile: IPowerZigguratComponent) => isLocInZigguratParentChain(loc, tile.getParent)
        case _ => false
      }

  /**
    * Non-tail-optimized recursive depth-first search for loc, by looking at all children of zigguratLoc.
    * Obviously, if you want to see if the loc is in the Ziggurat at all, you should pass the ZigguratRootNode as the zigguratLoc.
    *
    * Need to see about possible optimizations for this.
    *
    * @param loc
    * @param zigguratLoc
    * @return
    */
  def isLocInZigguratChildChain(loc: Loc4, zigguratLoc: Loc4): Boolean = {
    if (loc == null) false
    else if (loc == zigguratLoc) true
    else loc.getTileEntity(true) match {
      case Some(tile: IPowerZigguratComponent) => tile.getChildren.exists(isLocInZigguratChildChain(loc, _))
      case _ => false
    }
  }

  /**
    *
    * @param loc Loc containing any hooked up IPowerZiggurat block.
    * @return Loc of the root node, or null if initial Loc doesn't contain an IPowerZigguratBlock implementer.
    */
  def getZigguratRootLoc(loc: Loc4): Loc4 = getZigguratRootHelper(loc, null)

  @tailrec
  private def getZigguratRootHelper(loc: Loc4, prev: Loc4): Loc4 =
    if (loc == null || loc == prev) loc
    else
      loc.getTileEntity(true) match {
        case Some(tile: IPowerZigguratComponent) => getZigguratRootHelper(tile.getParent, loc)
        case _ => prev
      }

}
