package tbdale.hpc.scala.condorAPI

  
trait CondorManagerLogger extends Enumeration {    
  type SEVERITY = Value
  val DEBUG = Value("DEBUG")
  val INFORMATIONAL = Value("INFORMATIONAL")
  val WARNING = Value("WARNING")
  val FATAL = Value("FATAL")
  def logCondorManagerEvent(msg:String,sev:SEVERITY)

}
