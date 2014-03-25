package tbdale.hpc.scala.condorAPI
import scala.actors.{Actor,OutputChannel}
import scala.sys.process._
/*
 * CondorCommands - function wrappers for HTCondor commands
 */
case class CondorSubmitCommand(desc:String,worker:OutputChannel[Any])

class CondorShell extends Actor{
  def act(){
    loop{
      receive{
        case CondorSubmitCommand(desc,worker) =>{
          sender ! submit(desc,worker)
          exit
        }
      }
    }
  }
   def submit(jobDescriptor:String,worker:OutputChannel[Any]):CondorStatus={
     val cmd = Seq("/bin/bash","-c","echo \"%s\"|condor_submit -".format(jobDescriptor))
     // val cmd = Seq("/bin/bash","-c","echo \"%s\"|/home/brian/test_submit".format(jobDescriptor)) // prototyping
     try{
       val retval = cmd !!
	     val clusterIdMatcher = """\*\* Proc (\d+).*""".r
	     retval.split("\n").foreach(line=>{
	       line match {
	         case clusterIdMatcher(clusterId) => return CondorSubmitReturn(clusterId.toLong,worker)
	         case _ => {/*drop*/}
	       }
	     })
	     return new CondorSubmitFailed("Unknown error")
     }catch{
       case e:Exception => return(new CondorSubmitFailed("Exception: condor_submit command failed"))
     }
   }
   def remove(clusterID:Long)={
     
   }
   
}
