package com.itszuvalex.femtocraft.api.computation

class ComputationDistributionAlgorithm(val computers: Seq[IComputer], val jobs: Seq[IComputationJob]) {

  def distribute(): Unit = {
    val sortedComputers = (Seq[IComputer]() ++ computers).sortBy(_.efficiency)(Ordering.Double.reverse)
    val sortedJobs      = (Seq[IComputationJob]() ++ jobs).sortBy(_.FLOPSRemaining)(Ordering.Double.reverse)

    val sortedComputersIter = sortedComputers.iterator
    val sortedJobsIter      = sortedJobs.iterator

    var comp: IComputer       = if (sortedComputersIter.hasNext) sortedComputersIter.next() else null
    var job : IComputationJob = if (sortedJobsIter.hasNext) sortedJobsIter.next() else null

    var flopsMade   = if (comp == null) 0d else comp.FLOPSPerTick
    var flopsToMove = if (job == null) 0d else math.min(job.FLOPSRemaining, job.FLOPSContributablePerTick)
    while (comp != null && job != null) {
      val flopsCanMove = math.min(flopsMade, flopsToMove)
      val flopsRemaining    = job.contributeFLOPS(flopsCanMove)
      val flopsUsed = flopsCanMove - flopsRemaining
      comp.useFLOPS(flopsUsed)
      flopsMade -= flopsUsed
      flopsToMove -= flopsUsed

      if (flopsMade <= 0) {
        comp = if(sortedComputersIter.hasNext) sortedComputersIter.next() else null
        if (comp != null)
          flopsMade = comp.FLOPSPerTick
      }

      if (flopsToMove <= 0) {
        job = if(sortedJobsIter.hasNext) sortedJobsIter.next() else null
        if (job != null)
          flopsToMove = math.min(job.FLOPSRemaining, job.FLOPSContributablePerTick)
      }
    }
  }
}
