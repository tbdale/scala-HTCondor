package tbdale.hpc.scala.condorAPI

import java.io.File
import scala.collection.mutable.{Map=>MutableMap}
import scala.actors.{Actor,OutputChannel}

/*
 * CondorJobManager - maintains a list of running jobs and uses message passing to relay events
 *  @logFile condor log file to watch
 */


class CondorJobManager(logFile:String) extends Actor{
  def act(){
    
    // create condor log file    
    new File(logFile)
    
    // jobQueue is where we're going to keep track of the condor jobs in flight
    val jobQueue:MutableMap[Long,OutputChannel[Any]] = MutableMap[Long,OutputChannel[Any]]()
    // we need a logMonitor to watch the condor log and look for events 
    val logMonitor = new CondorLogMonitor(new File(logFile), this )
    logMonitor.start(500)
    
    // message handler to receive job submissions, logMonitor events and sends status updates to submitters
    loop {
	    receive{
	      case SendCondorJob(job) =>{
	        // we received a request to start a condor job
	        val status = queueJob(job,logFile)
	        status match {
	          case CondorSubmitReturn(id) => {
	            // it's a good submission, add to queue
	            jobQueue += (id -> sender)
	            println("Added job to queue: %d , queue len: %d".format(id, jobQueue.size)) // prototyping
	            }
	          case CondorSubmitFailed(status)=>{
	            // something went wrong when we tried to shell out and submit the condor job
	            println("Error: sending failure message to sender") // prototyping
	            sender ! CondorSubmitFailed(status)
	          }   
	        }       
	      }
	      case LogUpdate(clusterId,event) => {
	        // logMonitor has found new events in the condor log
	        println("Received log update for job: %s, queue len:%d".format(clusterId,jobQueue.size)) // prototyping
	        jobQueue get clusterId match {
	          case Some(sender) => {
	              println ("Sending job update to sender for job:"+clusterId) // prototyping
	              sender ! event	              
	              // remove from queue if job completed/terminated
	              event match {
	                case CondorJobTerminatedEvent() => {
	                  println("Job terminated: %s, queue len:%d".format(clusterId,jobQueue.size)) // prototyping
	                  jobQueue -= clusterId
	                }
	                case _ => {}
	              }
	            }
	          case None => {
	              println ("Unable to find sender for job:"+clusterId) // prototyping
	            }
	        }	        
	      }
	      case _ => {println("Error, unknown message type") /* // prototyping */ }
	    } // end receive
    } // end loop
  }
  
  private def queueJob(job:CondorJob,condorLogFQP:String):CondorStatus={
    // take a new unsubmitted job, submit to condor, and return a submitted job
    CondorCommands.submit(new CondorJob(job.reqRam,
                  job.executable,
                  job.execArguments,
                  job.universe,
                  job.outFileFQP,
                  job.errFileFQP,
                  condorLogFQP).genDescription)                  
  }

}
