package com.itszuvalex.femtocraft.util

import com.itszuvalex.itszulib.util.StringUtils

/**
  * Created by Christopher on 9/1/2015.
  */
object StringUtil {
  def formatPowerString(current: Long, powerMax: Long): String = {
    StringUtils.formatIntegerString(current.toString) + '/' + StringUtils.formatIntegerString(powerMax.toString) + " DE"
  }
}
