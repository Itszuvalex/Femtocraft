package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.itszulib.api.core.Configurable

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */
@Configurable
object DiffusionNode {
  @Configurable val PARENT_WHITELIST = Array(IPowerNode.GENERATION_NODE, IPowerNode.TRANSFER_NODE)
  @Configurable val CHILD_WHITELIST  = Array(IPowerNode.DIFFUSION_TARGET_NODE)

  def canAddParent(parent: IPowerNode) = DiffusionNode.PARENT_WHITELIST.contains(parent.getType)

  def canAddChild(child: IPowerNode) = DiffusionNode.CHILD_WHITELIST.contains(child.getType)
}

@Configurable
trait DiffusionNode extends PowerNode {
  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType = IPowerNode.DIFFUSION_NODE

  /**
    *
    * @param parent IPowerNode that is being checked.
    * @return True if this node is capable of having that node as a parent.
    */
  override def canSetParent(parent: IPowerNode) = super.canSetParent(parent) && DiffusionNode.canAddParent(parent)

  /**
    *
    * @param child
    * @return True if child is capable of being a child of this node.
    */
  override def canAddChild(child: IPowerNode) = super.canAddChild(child) && DiffusionNode.canAddChild(child)
}
