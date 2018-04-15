package com.itszuvalex.femtocraft.util

trait KeyObject {
  lazy val name = this.getClass.getTypeName

  def namespacedKey(key: String) = s"$name$key"
}
