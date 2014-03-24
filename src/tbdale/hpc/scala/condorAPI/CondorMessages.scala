package tbdale.hpc.scala.condorAPI

trait CondorStatus

case class CondorSubmitEvent extends CondorStatus
case class CondorExecuteEvent extends CondorStatus
case class CondorEventError(msg:String) extends CondorStatus
case class CondorJobTerminatedEvent extends CondorStatus
case class CondorJobNotSubmitted extends CondorStatus
case class CondorSubmitFailed(msg:String) extends CondorStatus