package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.itszulib.api.core.Configurable

/**
 * Created by Christopher Harris (Itszuvalex) on 8/4/15.
 */
@Configurable
object DiffusionTargetNode {
  val PARENT_WHITELIST = Array(IPowerNode.DIFFUSION_NODE)
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
  override def canAddParent(parent: IPowerNode) = DiffusionTargetNode.PARENT_WHITELIST.contains(parent.getType)

  /**
   *
   * @return Iterable of IPowerNodes this has as children. If this is a leaf node, returns null, otherwise, empty list.
   */
  override def getChildren: Iterable[IPowerNode] = null

  /**
   *
   * @param child
   * @return True if child is capable of being a child of this node.
   */
  override def canAddChild(child: IPowerNode) = false
}
