package com.itszuvalex.femtocraft.industry

import java.util

import com.itszuvalex.itszulib.api.storage.IItemStorage
import com.itszuvalex.itszulib.api.wrappers.IItemStack

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

/**
  * Created by Chris on 1/21/2017.
  */
case class SynthesizerRecipe(output: IItemStack, inputs: java.util.Collection[ItemStackCraftingComponent], ticks: Int) {
  def canCraft(storage: IItemStorage): Boolean = {
    val used = new util.TreeMap[Int, Int].asScala

    def getAmt(slot: Int): Int = storage(slot).stackSize - used.getOrElse(slot, 0)

    def useAmt(slot: Int, amt: Int): Unit = used(slot) = amt + used.getOrElse(slot, 0)

    iterateItemStorage(storage, getAmt, useAmt)
  }

  def craft(storage: IItemStorage): Boolean = {
    def getAmt(slot: Int): Int = storage(slot).stackSize

    def useAmt(slot: Int, amt: Int): Unit = storage.split(slot, amt)

    iterateItemStorage(storage, getAmt, useAmt)
  }

  private def iterateItemStorage(storage: IItemStorage, getAmt: (Int) => Int, useAmt: (Int, Int) => Unit): Boolean = {
    inputs.forall { it =>
      var req = it.req.stackSize

      storage.indices.view.filter(a => it.equivalency(storage(a))).exists { i =>
        val amt = Math.min(req, getAmt(i))
        req -= amt
        useAmt(i, amt)
        req <= 0
      }

      req <= 0
    }
  }
}
