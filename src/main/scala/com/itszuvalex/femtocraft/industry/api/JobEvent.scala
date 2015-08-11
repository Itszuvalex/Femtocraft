package com.itszuvalex.femtocraft.industry.api

import com.itszuvalex.itszulib.api.core.Loc4
import cpw.mods.fml.common.eventhandler.Event

/**
 * Created by Christopher on 8/10/2015.
 */
object JobEvent {

  class JobStartedEvent(id: String, val loc: Loc4) extends JobEvent(id)

  class JobCancelledEvent(id: String) extends JobEvent(id)

  class JobFinishedEvent(id: String) extends JobEvent(id)

}

abstract class JobEvent(val id: String) extends Event


