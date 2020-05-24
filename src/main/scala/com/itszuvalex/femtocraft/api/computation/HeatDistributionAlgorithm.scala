package com.itszuvalex.femtocraft.api.computation

class HeatDistributionAlgorithm(val generators: Seq[IHeatStorage], val distributors: Seq[IHeatCirculator], val sinks: Seq[IHeatSink]) {

  def distribute(): Unit = {
    // Can't move heat if there's nowhere to move it from/to
    if (generators.isEmpty || sinks.isEmpty)
      return

    val energyCap = distributors.view.map(_.energyTransfer()).sum

    // TODO Do this entire thing better

    // Sort gens with hottest first
    val sortedGen = generators.sortWith(_.temperature.kelvin > _.temperature.kelvin)
    //val genAvgTemp = Temperature.kelvin(generators.view.map(_.temperature.kelvin).sum / generators.size.toDouble)

    // Sort sinks with coolest first
    val sortedSinks  = sinks.sortWith(_.heatStorage.temperature.kelvin < _.heatStorage.temperature.kelvin)
    val sinkTemps    = sinks.view.map(_.heatStorage.temperature.kelvin)
    val sinksAvgTemp = Temperature.kelvin(sinkTemps.sum / sinks.size.toDouble)
    val sinksMaxTemp = Temperature.kelvin(sinkTemps.max)

    // Only take heat from generators above the sink average temp.
    val filteredGens = generators.filter(_.temperature.kelvin >= sinksAvgTemp.kelvin)

    var energyRemainingToRemove = energyCap
    val energyFromGens          = filteredGens.map { g =>
      val tempDif = g.temperature.kelvin - sinksAvgTemp.kelvin
      val energy  = math.min(tempDif * g.material.heatCapacity * g.mass, energyRemainingToRemove)
      val removed = g.removeEnergy(energy)
      energyRemainingToRemove -= removed
      removed
    }.sum

    var tempDifferentialInKelvin = 0
    if (sortedSinks.size == 1) {
      sortedSinks.head.heatStorage.addEnergy(energyFromGens)
    }
    else {
      sortedSinks.map { s =>
        val tempDif = sinksMaxTemp.kelvin - s.heatStorage.temperature.kelvin
        tempDifferentialInKelvin += tempDif
        (s, tempDif)
      }.foreach { s =>
        s._1.heatStorage.addEnergy(energyFromGens * (s._2 / tempDifferentialInKelvin))
      }
    }
  }
}
