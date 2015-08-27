package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.itszulib.api.core.Configurable

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
@Configurable
object TransferNode {
  @Configurable val PARENT_WHITELIST   = Array(IPowerNode.GENERATION_NODE, IPowerNode.TRANSFER_NODE)
  @Configurable val CHILDREN_WHITELIST = Array(IPowerNode.TRANSFER_NODE, IPowerNode.DIFFUSION_NODE, IPowerNode.DIRECT_NODE)

  def canAddParent(parent: IPowerNode) = TransferNode.PARENT_WHITELIST.contains(parent.getType)

  def canAddChild(child: IPowerNode) = TransferNode.CHILDREN_WHITELIST.contains(child.getType)
}

@Configurable
trait TransferNode extends PowerNode {
  /**
   *
   * @return The type of PowerNode this is.
   */
  override def getType = IPowerNode.TRANSFER_NODE

  /**
   *
   * @param parent IPowerNode that is being checked.
   * @return True if this node is capable of having that node as a parent.
   */
  override def canAddParent(parent: IPowerNode) = super.canAddParent(parent) && TransferNode.canAddParent(parent)

  /**
   *
   * @param child
   * @return True if child is capable of being a child of this node.
   */
  override def canAddChild(child: IPowerNode) = super.canAddChild(child) && TransferNode.canAddChild(child)
}
