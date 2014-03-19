package tbdale.hpc.scala.condorAPI
import scala.sys.process._
/*
 * CondorCommands - function wrappers for HTCondor commands
 */

object CondorCommands {
   def submit(jobDescriptor:String):Either[CondorStatus,Long]={
     val cmd = Seq("/bin/bash","-c","echo \"%s\"|condor_submit -".format(jobDescriptor))
     // val cmd = Seq("/bin/bash","-c","echo \"%s\"|/home/brian/test_submit".format(jobDescriptor)) // prototyping
     try{
       val retval = cmd !!
	     val clusterIdMatcher = """\*\* Proc (\d+).*""".r
	     retval.split("\n").foreach(line=>{
	       line match {
	         case clusterIdMatcher(clusterId) => return Right(clusterId.toLong)
	         case _ => {/*drop*/}
	       }
	     })
	     return Left(new CondorSubmitFailed("Unknown error"))
     }catch{
       case e:Exception => Left(new CondorSubmitFailed("Exception: condor_submit command failed"))
     }
   }
   def remove(clusterID:Long)={
     
   }
   
}
