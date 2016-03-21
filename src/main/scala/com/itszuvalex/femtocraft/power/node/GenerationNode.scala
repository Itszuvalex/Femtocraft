package com.itszuvalex.femtocraft.power.node

import com.itszuvalex.itszulib.api.core.Configurable

/**
  * Created by Christopher Harris (Itszuvalex) on 8/4/15.
  */
@Configurable
object GenerationNode {
  @Configurable val CHILDREN_WHITELIST = Array(IPowerNode.TRANSFER_NODE, IPowerNode.DIRECT_NODE, IPowerNode.DIFFUSION_NODE)

  def canAddChild(child: IPowerNode) = GenerationNode.CHILDREN_WHITELIST.contains(child.getType)

  def canAddParent(parent: IPowerNode) = false
}

@Configurable
trait GenerationNode extends PowerNode {

  /**
    *
    * @return The type of PowerNode this is.
    */
  override def getType = IPowerNode.TRANSFER_NODE

  /**
    *
    * @param child
    * @return True if child is capable of being a child of this node.
    */
  override def canAddChild(child: IPowerNode) = super.canAddChild(child) && GenerationNode.canAddChild(child)

  /**
    *
    * @return The IPowerNode this has as its parent.  If this is of type 'Generation', this will be itself.
    */
  override def getParent = this

  /**
    *
    * @return Loc4 of this node's parent, null if it has no parent.  This is primarily to bypass chunk churn, as a node may have a parent set but the parent is in an unloaded chunk.  If that is the case, then
    *         it can return its parent location here, without having to explicitly load that chunk.
    */
  override def getParentLoc = getNodeLoc

  /**
    *
    * @param parent IPowerNode that is being checked.
    * @return True if this node is capable of having that node as a parent.
    */
  override def canSetParent(parent: IPowerNode) = GenerationNode.canAddParent(parent)
}
