package tbdale.hpc.scala.condorAPI
import scala.actors.Actor
import scala.sys.process._
  
/*
 * Tester - for prototyping and unit testing
 */
class TestWorker(manager:CondorJobManager, num:Int) extends Actor{
  def act(){       
    // message handler to receive job submissions, logMonitor events and sends status updates to submitters
    println("Starting worker")
    val condorJob  = new CondorJob("32M","/bin/sleep","10","Vanilla","/tmp/sleep.out","/tmp/sleep.err")
    manager ! SendCondorJob(condorJob)
    loop {
      receive {
        case CondorSubmitEvent() => {println("Receieved submit confirmation:"+num)}
        case CondorExecuteEvent() => {println("Receieved execute confirmation:"+num)}
        case CondorJobTerminatedEvent() => {println("Job complete:"+num);exit}
        case _ => {println("received event")}
        }
       }    
   }
}
object Tester  {
  def main(args: Array[String]): Unit = {
    val manager = new CondorJobManager("~bdale/condor_test.log")    
    manager.start
    (1 to 1000).foreach( num =>{
      println("Creating worker:"+num)
      val tm = new TestWorker(manager,num)
      tm.start
      Thread.sleep(100) //throttle
    })
    println("Tester exiting")
    } 
}
