package com.itszuvalex.femtocraft.industry.api

/**
 * Created by Christopher on 8/10/2015.
 */
trait IJobRequester {

  def notify(event: JobEvent)

}
