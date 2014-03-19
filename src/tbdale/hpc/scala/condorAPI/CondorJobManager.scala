package tbdale.hpc.scala.condorAPI

import java.io.File
import scala.collection.mutable.{Map=>MutableMap}
import scala.actors.{Actor,OutputChannel}

/*
 * CondorJobManager - maintains a list of running jobs and uses message passing to relay events
 *  @logFile condor log file to watch
 */

case class condorSubmit(job:CondorJob)
case class logUpdate(id:Long)

class CondorJobManager(logFile:String) extends Actor{
  def act(){
    // jobQueue is where we're going to keep track of the condor jobs in flight
    val jobQueue:MutableMap[Long,OutputChannel[Any]] = MutableMap[Long,OutputChannel[Any]]()
    // we need a logMonitor to watch the condor log and look for events 
    val logMonitor = new CondorLogMonitor(new File(logFile), this )
    
    // message handler to receive job submissions, logMonitor events and sends status updates to submitters
    loop {
	    receive{
	      case condorSubmit(job) =>{
	        // we received a request to start a condor job
	        val retJob = queueJob(job)
	        retJob.clusterId match {
	          case Right(id) => {
	            // it's a good submission, add to queue
	            jobQueue += (id -> sender)
	            println("added job to queue: %d , queue len: %d".format(id, jobQueue.size)) // prototyping
	            }
	          case Left(status)=>{
	            // something went wrong when we tried to shell out and submit the condor job
	            println("Error: sending failure message to sender") // prototyping
	            sender ! retJob.clusterId
	          }   
	        }       
	      }
	      case logUpdate(clusterId) => {
	        // logMonitor has found new events in the condor log
	        println("Received log update for job:"+clusterId) // prototyping
	        jobQueue get clusterId match {
	          case Some(sender) => {
	              println ("Sending job update to sender for job:"+clusterId) // prototyping
	              sender ! clusterId            
	            }
	          case None => {
	              println ("Unable to find sender for job:"+clusterId) // prototyping
	            }
	        }
	        jobQueue -= clusterId
	      }
	      case _ => {println("Error, unknown message type") /* // prototyping */ }
	    } // end receive
    } // end loop
  }
  
  private def queueJob(job:CondorJob)={
    // take a new unsubmitted job, submit to condor, and return a submitted job
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
