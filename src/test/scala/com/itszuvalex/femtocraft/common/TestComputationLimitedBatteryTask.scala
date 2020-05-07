package com.itszuvalex.femtocraft.common

import com.itszuvalex.femtocraft.TestBase
import com.itszuvalex.femtocraft.api.computation.IComputationJob
import com.itszuvalex.itszulib.api.storage.{IBattery, PowerBattery}

class TestComputationLimitedBatteryTask extends TestBase {

  "A ComputationLimitedBatteryTask" should {
    "fill only up to the computation percentage in progress" in {
      val battery = new PowerBattery(100000)
      battery.storage = battery.maxStorage
      val job  = new IComputationJob {
        override def FLOPs: Double = 5000

        override def FLOPsRequired: Double = 10000

        override def FLOPSContributablePerTick: Double = 0

        /**
         *
         * @param flops Amount of FLOPs to contribute.
         * @return Amount of flops remaining out of flops.
         */
        override def contributeFlops(flops: Double): Double = 0d
      }
      val task = new TestComputationLimitedBatteryTask(battery, job)

      // Put us up to just below 50%
      val ret = task.contribute(499, 0d, 0d)
      ret shouldBe 499d +- 0.0000001

      val capped = task.contribute(501, 0d, 0d)
      capped shouldBe 1d +- 0.0000001
    }
  }

  class TestComputationLimitedBatteryTask(bat: IBattery, comp: IComputationJob) extends ComputationLimitedBatteryTask(1000, 1, bat, comp, () => 0, () => 0) {
    override def inProgress: Boolean = true

    override def canStart: Boolean = true

    override def start(): Unit = {}

    override def onCompleted(): Unit = {}
  }

}
