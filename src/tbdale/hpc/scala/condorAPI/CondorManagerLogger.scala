package tbdale.hpc.scala.condorAPI

  
trait CondorManagerLogger extends Enumeration { 
  /*
   * Use to implement your own logger for the CondorManager
   */
  type SEVERITY = Value
  val DEBUG = Value("DEBUG") // debug messages, add your own control structure to turn on/off
  val INFORMATIONAL = Value("INFORMATIONAL") // status messages
  val WARNING = Value("WARNING") // problems with jobs or calls but not with the CondorManager
  val FATAL = Value("FATAL") // severe problems that impact the CondorManager as well
  def logCondorManagerEvent(msg:String,sev:SEVERITY)

}
