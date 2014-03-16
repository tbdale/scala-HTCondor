package tbdale.hpc.scala.condorAPI

/*
 * CondorJob - data structure for HTCondor job parameters
 */
class CondorJob( val reqRam:String,
                 val executable:String,
                 val execArguments:String,
                 val universe:String,
                 val outFileFQP:String,
                 val errFileFQP:String,
                 val condorLogFQP:String,
                 val clusterId:Either[CondorStatus,Long] = Left(new CondorJobNotSubmitted)){
  def genDescription:String={
    // generate condor submit description to pass to condor_submit via STDIN
"""
Executable     = %s
Arguments      = %s    
Universe       = %s                                                    
request_memory = %s
error   = %s                                                
output  = %s                                                
log     = %s
queue
""".format(executable,execArguments,universe,reqRam,errFileFQP,outFileFQP,condorLogFQP)
  }
}