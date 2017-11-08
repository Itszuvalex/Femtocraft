package com.itszuvalex.femtocraft.api.logistics

/**
  * Created by Chris on 2/19/2017.
  */
trait IResource[T] {
  def resourceKey: String

  /**
    * 'Empty' Resources MUST be sorted last.  Otherwise, we will always fill empty slots before merging.
    *
    * @param a Resource A
    * @param b Resource B
    *
    * @return True if A is less than B.
    */
  def sort(a: T, b: T): Boolean
}
