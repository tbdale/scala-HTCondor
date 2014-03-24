package tbdale.hpc.scala.condorAPI

import scala.collection.mutable.ListBuffer
import java.io.File
import org.apache.commons.io.input._

/*
 * CondorLogMonitor 'tails' condor logfile looking for events to message back to manager
 */

//class CondorLogMonitor (val logfile:File, manager: CondorJobManager ) extends TailerListenerAdapter {
class CondorLogMonitor (val logfile:File, manager: TestManager ) extends TailerListenerAdapter {
  val lineBuffer = new ListBuffer[String] // line buffer to store lines from tailer

  override def handle(line:String) = {
    // simple string matching for now
    println("Tailer got line:"+line) //prototyping
    line match {
      case "<c>" => {lineBuffer.clear;lineBuffer.append(line)}
      case "</c>" => {processEvent}
      case _ => lineBuffer.append(line)
     }
  }
  def processEvent{
    println("Processing event") //prototyping
    // using regex to pick out parts of the xml log entry
    val clusterIdMatcher = """.*<a n="Cluster"><i>(.*)</i></a>.*""".r
    val typeMatcher = """.*<a n="MyType"><s>(.*)</s></a>.*""".r
    
    
    def getEvent(lines:List[String],clusterId:Long,event:Option[CondorStatus]):(Long,Option[CondorStatus])={
      // recursive function to walk list and return a tuple of id and event
      if (lines.size != 0){
	      lines.head match {
	        case clusterIdMatcher(id) => getEvent(lines.tail,id.toLong,event)
	        case typeMatcher(t) => {
	          println(t)
	          t match {
	            case "SubmitEvent" => getEvent(lines.tail, clusterId, Some(new CondorSubmitEvent))
	            case "ExecuteEvent" => getEvent(lines.tail, clusterId, Some(new CondorExecuteEvent))
	            case "JobTerminatedEvent" => getEvent(lines.tail, clusterId, Some(new CondorJobTerminatedEvent))
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
    println("sending message %s,%s".format(id,event.toString()))
    manager ! logUpdate(id,
                        event match {case Some(e) => e; case None => new CondorEventError("Unknown event")}
                       )
  }
  def start(delay:Int):Tailer = {
    println("Starting Log tailer") // prototyping
    Tailer.create(logfile, this, delay)
 }
}
