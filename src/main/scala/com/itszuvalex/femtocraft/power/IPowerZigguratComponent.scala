package com.itszuvalex.femtocraft.power

import com.itszuvalex.femtocraft.graph.{IManyChildNode, ISingleParentNode}
import com.itszuvalex.itszulib.api.core.Loc4

/**
  * Created by Christopher Harris (Itszuvalex) on 12/17/15.
  */
trait IPowerZigguratComponent extends ISingleParentNode[Loc4] with IManyChildNode[Loc4]
