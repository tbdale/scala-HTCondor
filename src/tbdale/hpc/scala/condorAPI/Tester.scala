package tbdale.hpc.scala.condorAPI

  
/*
 * Tester - for prototyping and unit testing
 */

object Tester  {
  def main(args: Array[String]): Unit = {
    val condorLogFile = ("/tmp/test.log")
    val condorManager = new CondorJobManager(condorLogFile)
    condorManager.start
    val condorJob  = new CondorJob("32M","/bin/sleep","30","Vanilla","/tmp/sleep.out","/tmp/sleep.err",condorLogFile)
    println("Submitting job:\n"+condorJob.genDescription)
    condorManager !? condorSubmit(condorJob) 
    println("Job complete")
  } 
}
