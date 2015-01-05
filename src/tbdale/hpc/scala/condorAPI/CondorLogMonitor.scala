package tbdale.hpc.scala.condorAPI

import scala.collection.mutable.ListBuffer
import java.io.File
import org.apache.commons.io.input._

/*
 * CondorLogMonitor 'tails' condor logfile looking for events to message back to manager
 */

case class LogUpdate(id:Long,event:CondorStatus)

class CondorLogMonitor (val logfile:File, manager: CondorJobManager ) extends TailerListenerAdapter {
  val lineBuffer = new ListBuffer[String] // line buffer to store lines from tailer

  override def handle(line:String) = {
    // simple string matching for now
    line match {
      case "<c>" => {lineBuffer.clear;lineBuffer.append(line)}
      case "</c>" => {processEvent}
      case _ => lineBuffer.append(line)
     }
  }
  def processEvent{
    // using regex to pick out parts of the xml log entry
    val clusterIdMatcher = """.*<a n="Cluster"><i>(.*)</i></a>.*""".r
    val typeMatcher = """.*<a n="MyType"><s>(.*)</s></a>.*""".r    
        
    def getEvent(lines:List[String],clusterId:Long,event:Option[CondorStatus]):(Long,Option[CondorStatus])={
      // recursive function to walk list and return a tuple of id and event
      if (lines.size != 0){
	      lines.head match {
	        case clusterIdMatcher(id) => getEvent(lines.tail,id.toLong,event)	        
	        case typeMatcher(t) => {
	          t match {
	            case "SubmitEvent" => getEvent(lines.tail, clusterId, Some(new CondorSubmitEvent))
	            case "ExecuteEvent" => getEvent(lines.tail, clusterId, Some(new CondorExecuteEvent))
	            case "JobTerminatedEvent" => getEvent(lines.tail, clusterId, Some(new CondorJobTerminatedEvent(Some(JobTerminatedReason.COMPLETED),getReturnCode(lines,clusterId) ) ) )
	            case _=> getEvent(lines.tail,clusterId,event)
	          }
	        }	        
	        case _ => getEvent(lines.tail,clusterId,event)
	      }
      } else{
        (clusterId,event)
      }
    }
    
    // recursively walk list to pick out event and id
    val (id,event) = getEvent(lineBuffer.toList,-1l,None)
    // send condor event message to manager
    manager ! LogUpdate(id,
                        event match {case Some(e) => e; case None => new CondorEventError("Unknown event")}
                       )
  }
  def getReturnCode(eventLines:List[String],clusterId:Long):Some[Int] = {
    val retvalMatcher = """.*<a n="ReturnValue"><i>(.*)</i></a>.*""".r  // condor log xml pattern matcher for job exit code      
    if (eventLines.size != 0){
      eventLines.head match {
	    case retvalMatcher(retval) => { return Some(retval.toInt) } // return exit code found from condor job
	    case _ => {getReturnCode(eventLines.tail,clusterId)} // continue looking
	  }  
	}else{
	  manager.managerLogger.logCondorManagerEvent("CondorJobManager.CondorLogMonitor()::Could not find exit code for job ( "+clusterId.toString+"), returning success anyway!", manager.managerLogger.WARNING)
	  return Some(0)
	}
  }
  def start(delay:Int):Tailer = {
    manager.managerLogger.logCondorManagerEvent("CondorJobManager.CondorLogMonitor()::Starting tailer.", manager.managerLogger.INFORMATIONAL)
    Tailer.create(logfile, this, delay)
 }
}
