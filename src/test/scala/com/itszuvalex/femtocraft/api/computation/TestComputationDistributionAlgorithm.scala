package com.itszuvalex.femtocraft.api.computation

import com.itszuvalex.femtocraft.TestBase
import com.itszuvalex.femtocraft.computation.ComputerConstant

class TestComputationDistributionAlgorithm extends TestBase {

  "A ComputationDistributionAlgorithm" should {
    "when distributing" should {
      "prioritize higher efficiency computers" in {
        val comp1      = new TestComputer(1000d, Double.MaxValue)
        val comp2      = new TestComputer(1000d, 0d)

        val job = new TestJob(750d, 500d)

        new ComputationDistributionAlgorithm(Seq(comp1, comp2), Seq(job)).distribute()

        comp1.usedFlops shouldBe 500d
        comp2.usedFlops shouldBe 0d
        job.FLOPSRemaining shouldBe 250d
      }

      "power multiple jobs with one computer" in {
        val comp = new TestComputer(5000d, 0d)

        val job1 = new TestJob(1000d, 1000d)
        val job2 = new TestJob(1000d, 1000d)
        val job3 = new TestJob(1000d, 1000d)
        val job4 = new TestJob(1000d, 1000d)
        val job5 = new TestJob(1000d, 1000d)
        val job6 = new TestJob(1000d, 1000d)
        val jobs = Seq[TestJob](job1, job2, job3, job4, job5, job6)

        new ComputationDistributionAlgorithm(Seq(comp), jobs).distribute()

        comp.usedFlops shouldBe comp.FLOPSPerTick

        jobs.map(_.FLOPs).sum shouldBe comp.FLOPSPerTick
      }

      "use multiple computers to power one job" in {
        val comp1 = new TestComputer(1000d, 0d)
        val comp2 = new TestComputer(1000d, 0d)
        val comp3 = new TestComputer(1000d, 0d)
        val comp4 = new TestComputer(1000d, 0d)
        val comp5 = new TestComputer(1000d, 0d)
        val comps = Seq[TestComputer](comp1, comp2, comp3, comp4, comp5)

        val job = new TestJob(10000d, 10000d)

        new ComputationDistributionAlgorithm(comps, Seq(job)).distribute()

        job.FLOPs shouldBe 5000d
        comps.map(_.usedFlops).sum shouldBe job.FLOPs
      }
    }
  }

  class TestComputer(val flops: Double, e: Double) extends ComputerConstant(() => flops) {
    var usedFlops = 0d

    override def useFLOPS(flops: Double): Unit = usedFlops += flops

    override def efficiency: Double = e
  }

  class TestJob(val flopsRequired: Double, val flopsContributable: Double) extends IComputationJob {
    var flops = 0d

    override def FLOPs: Double = flops

    override def FLOPSRequired: Double = flopsRequired

    override def FLOPSContributablePerTick: Double = flopsContributable

    /**
     *
     * @param f Amount of FLOPs to contribute.
     * @return Amount of flops remaining out of flops.
     */
    override def contributeFLOPS(f: Double): Double = {
      val room = math.min(FLOPSRemaining, FLOPSContributablePerTick)
      val min  = math.min(f, room)
      flops += min
      f - min
    }
  }

}
