package com.itszuvalex.femtocraft.api.computation

class ComputationDistributionAlgorithm(val computers: Seq[IComputer], val jobs: Seq[IComputationJob]) {

  def distribute(): Unit = {
    val sortedComputers = (Seq[IComputer]() ++ computers).sortBy(_.efficiency)(Ordering.Double.reverse)
    val sortedJobs      = (Seq[IComputationJob]() ++ jobs).sortBy(_.FLOPSRemaining)(Ordering.Double.reverse)

    val sortedComputersIter = sortedComputers.iterator
    val sortedJobsIter      = sortedJobs.iterator

    var comp: IComputer       = if (sortedComputersIter.hasNext) sortedComputersIter.next() else null
    var job : IComputationJob = if (sortedJobsIter.hasNext) sortedJobsIter.next() else null

    var flopsToMove = if (job == null) 0d else math.min(job.FLOPSRemaining, job.FLOPSContributablePerTick)

    //  If job is null, computer doesn't need to generate FLOPS, don't do so.
    var flopsMade   = if (comp == null || job == null) 0d else comp.generateFLOPS()
    while (comp != null && job != null) {
      val flopsCanMove = math.min(flopsMade, flopsToMove)
      val flopsRemaining    = job.contributeFLOPS(flopsCanMove)
      val flopsUsed = flopsCanMove - flopsRemaining
      flopsMade -= flopsUsed
      flopsToMove -= flopsUsed

      if (flopsToMove <= 0) {
        job = if(sortedJobsIter.hasNext) sortedJobsIter.next() else null
        if (job != null)
          flopsToMove = math.min(job.FLOPSRemaining, job.FLOPSContributablePerTick)
      }

      if (flopsMade <= 0) {
        comp = if(sortedComputersIter.hasNext) sortedComputersIter.next() else null

        // If job is null, we've reached the end of the job loop, no need to generate more FLOPS
        if (comp != null && job != null)
          flopsMade = comp.generateFLOPS()
      }
    }
  }
}
