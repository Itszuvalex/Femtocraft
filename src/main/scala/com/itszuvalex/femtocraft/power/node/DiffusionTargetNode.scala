package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.itszulib.api.core.Configurable

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */
@Configurable
object DiffusionTargetNode {
  @Configurable val PARENT_WHITELIST = Array(IPowerNode.DIFFUSION_NODE)

  def canAddParent(parent: IPowerNode) = DiffusionTargetNode.PARENT_WHITELIST.contains(parent.getType)

  def canAddChild(child: IPowerNode) = false
}

@Configurable
trait DiffusionTargetNode extends PowerNode {
  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType = IPowerNode.DIFFUSION_TARGET_NODE

  /**
    *
    * @param parent IPowerNode that is being checked.
    * @return True if this node is capable of having that node as a parent.
    */
  override def canSetParent(parent: IPowerNode) = super.canSetParent(parent) && DiffusionTargetNode.canAddParent(parent)

  /**
    *
    * @return Iterable of IPowerNodes this has as children. If this is a leaf node, returns null, otherwise, empty list.
    */
  override def getChildren = null

  /**
    *
    * @param child
    * @return True if child is capable of being a child of this node.
    */
  override def canAddChild(child: IPowerNode) = DiffusionTargetNode.canAddChild(child)
}
