package tbdale.hpc.scala.condorAPI
import scala.actors.Actor
import scala.sys.process._
  
/*
 * Tester - for prototyping and unit testing
 */
class TestManager(logFile:String) extends Actor{
  def act(){
    val logMonitor = new CondorLogMonitor(new java.io.File(logFile), this )
    logMonitor.start(500)
    
    // message handler to receive job submissions, logMonitor events and sends status updates to submitters
    loop {
      receive {
        case logUpdate(clusterId,event) => {
	        // logMonitor has found new events in the condor log
	        println("Received log update for job:"+clusterId, event.toString) // prototyping
        }
      }
    }
  }
}
object Tester  {
  def main(args: Array[String]): Unit = {
    println(Seq("/bin/bash","-c","ls test") !! )
    val tm = new TestManager("test/condor.log")
    tm.start
    Thread.sleep(30000)
    } 
}
