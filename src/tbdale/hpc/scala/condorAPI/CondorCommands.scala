package tbdale.hpc.scala.condorAPI
import scala.sys.process._
/*
 * CondorCommands - function wrappers for HTCondor commands
 */

object CondorCommands {
   def submit(jobDescriptor:String):CondorStatus={
     val cmd = Seq("/bin/bash","-c","echo \"%s\"|condor_submit -".format(jobDescriptor))
     // val cmd = Seq("/bin/bash","-c","echo \"%s\"|/home/brian/test_submit".format(jobDescriptor)) // prototyping
     try{
       val retval = cmd !!
	     val clusterIdMatcher = """\*\* Proc (\d+).*""".r
	     retval.split("\n").foreach(line=>{
	       line match {
	         case clusterIdMatcher(clusterId) => return CondorSubmitReturn(clusterId.toLong)
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
