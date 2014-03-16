package tbdale.hpc.scala.condorAPI

import java.io.File
import scala.actors.Actor

/*
 * CondorJobManager - maintains a list of running jobs and uses message passing to relay events
 *  @logFile condor log file to watch
 */

case class condorSubmit(job:CondorJob)

class CondorJobManager(logFile:String) extends Actor{
  def act(){
    val logMonitor = new CondorLogMonitor(new File(logFile) )
    receive{
      case condorSubmit(job) =>{
        // just to test message passing
        val retJob = queueJob(job)
        sender ! retJob.clusterId
      }
      case _ => {println("Error, unknown message type")  }
    }
  }
  private def queueJob(job:CondorJob)={
    new CondorJob(job.reqRam,
                  job.executable,
                  job.execArguments,
                  job.universe,
                  job.outFileFQP,
                  job.errFileFQP,
                  job.condorLogFQP,
                  CondorCommands.submit(job.genDescription)
                  )
  }

}
