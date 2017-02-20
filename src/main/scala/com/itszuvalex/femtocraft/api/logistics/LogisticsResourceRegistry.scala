package com.itszuvalex.femtocraft.api.logistics

import java.util

import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

/**
  * Created by Chris on 2/19/2017.
  */
object LogisticsResourceRegistry {
  val RESOURCE_ITEMS   = new IResource {
    override def resourceKey = "items"
  }
  val RESOURCE_NANITES = new IResource {
    override def resourceKey = "nanites"
  }
  val RESOURCE_FLUIDS  = new IResource {
    override def resourceKey = "fluids"
  }

  val resources = new ArrayBuffer[IResource]()

  def addResource(resource: IResource): Unit = resources += resource

  def getResources: util.Collection[IResource] = resources

  def init(): Unit = {
    addResource(RESOURCE_FLUIDS)
    addResource(RESOURCE_ITEMS)
    addResource(RESOURCE_NANITES)
  }

  def getResource(key: String): Option[IResource] = resources.find(_.resourceKey == key)

}
