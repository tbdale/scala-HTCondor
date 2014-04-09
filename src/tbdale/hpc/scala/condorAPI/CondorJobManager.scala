package tbdale.hpc.scala.condorAPI

import java.io.File
import scala.collection.mutable.{Map=>MutableMap}
import scala.actors.{Actor,OutputChannel}

/*
 * CondorJobManager - maintains a list of running jobs and uses message passing to relay events
 *  @logFile condor log file to watch
 */

case class SendCondorJob(condorJob:CondorJob)

protected class CondorJobManager(logFile:String,val managerLogger:CondorManagerLogger) extends Actor{

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
	        val condorShell = new CondorShell()
	        condorShell.start
	        val desc=new CondorJob(job.reqRam,
                  job.executable,
                  job.execArguments,
                  job.universe,
                  job.initialDir,
                  job.outFileFQP,
                  job.errFileFQP,
                  logFile).genDescription
	
	        condorShell ! CondorSubmitCommand(desc,sender)
	      }
	       
	      case CondorSubmitReturn(id,worker) => {
	            // it's a good submission, add to queue
	            jobQueue += (id -> worker)
	            managerLogger.logCondorManagerEvent("CondorJobManager.CondorSubmitReturn()::Added job to queue: %d , queue len: %d".format(id, jobQueue.size), managerLogger.INFORMATIONAL) 
	      }
	      case CondorSubmitFailed(status,worker)=>{
	            // something went wrong when we tried to shell out and submit the condor job
	            managerLogger.logCondorManagerEvent("CondorJobManager.CondorSubmitFailed()::Error: Condor Submit failed: sending failure message to sender",managerLogger.WARNING) 
	      }   
	            
	      case LogUpdate(clusterId,event) => {
	        // logMonitor has found new events in the condor log
	        jobQueue get clusterId match {
	          case Some(sender) => {
	              managerLogger.logCondorManagerEvent("CondorJobManager.LogUpdate()::Sending job update to sender for job:"+clusterId
	                                                   ,managerLogger.DEBUG)
	              event match {
	                case CondorJobTerminatedEvent() => {
	                  managerLogger.logCondorManagerEvent("CondorJobManager.LogUpdate()::Job terminated: %s, queue len:%d".format(clusterId,jobQueue.size),
	                                                      managerLogger.DEBUG)
	                  sender ! CondorJobTerminatedEvent
	                  jobQueue -= clusterId
	                }
	                case _ => {
	                  managerLogger.logCondorManagerEvent("CondorJobManager.LogUpdate()::Unknown event type for job cluster id:%s".format(clusterId,jobQueue.size),
	                                                               managerLogger.DEBUG)
	                }
	              }
	            }
	          case None => {
	              managerLogger.logCondorManagerEvent("CondorJobManager.LogUpdate()::Unable to find sender for job:"+clusterId,managerLogger.WARNING) 
	            }
	        }	        
	      }
	      case _ => {
	        managerLogger.logCondorManagerEvent("CondorJobManager.LogUpdate()::Unknown message",managerLogger.DEBUG)
          }
	    } // end receive
    } // end loop
    managerLogger.logCondorManagerEvent("CondorJobManager.act()::Closing CondorManager", managerLogger.INFORMATIONAL)
  }
  def getMailboxSize:Int={
    // just for test
    this.mailboxSize
  }
}
object CondorJobManager {
  private var instance:Option[CondorJobManager] = None 
  
  def getInstance(logFileFQP:String,logger:CondorManagerLogger):CondorJobManager={
    instance match{
      case Some(cjm) => cjm
      case None => {
        val cjm = new CondorJobManager(logFileFQP,logger)
        cjm.start
        instance = Some(cjm)
        cjm          
      }
    }
  }
}
