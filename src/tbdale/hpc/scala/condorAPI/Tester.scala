package tbdale.hpc.scala.condorAPI
import scala.actors.Actor
import scala.sys.process._
  
/*
 * Tester - for prototyping and unit testing
 */
class TestWorker(manager:CondorJobManager) extends Actor{
  def act(){       
    // message handler to receive job submissions, logMonitor events and sends status updates to submitters
    println("Starting worker")
    val condorJob  = new CondorJob("32M","/bin/sleep","30","Vanilla","/tmp/sleep.out","/tmp/sleep.err")
    manager ! SendCondorJob(condorJob)
    loop {
      receive {
        case CondorSubmitEvent() => {println("receieved submit confirmation")}
        case CondorExecuteEvent() => {println("receieved execute confirmation")}
        case CondorJobTerminatedEvent() => {println("received job completed")}
        case _ => {println("received event")}
        }
       }    
   }
}
object Tester  {
  def main(args: Array[String]): Unit = {
    val manager = new CondorJobManager("~bdale/condor_test.log")    
    manager.start
    
    println("Creating worker")
    val tm = new TestWorker(manager)
    tm.start
    //stupid loop to keep threads alive
    while(true) Thread.sleep(100)
    } 
}
