package com.itszuvalex.femtocraft.api

import com.itszuvalex.itszulib.api.core.Loc4

abstract class NetworkNodeVisitor {

  def visit(loc: Loc4): Unit

  def iterate(iterator: Iterator[(Loc4, _)]): Unit = iterator.foreach(p => visit(p._1))

}
