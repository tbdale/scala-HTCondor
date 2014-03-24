package tbdale.hpc.scala.condorAPI

trait CondorStatus
case class CondorSubmitReturn(clusterId:Long) extends CondorStatus
case class CondorSubmitEvent extends CondorStatus
case class CondorExecuteEvent extends CondorStatus
case class CondorEventError(msg:String) extends CondorStatus
case class CondorJobTerminatedEvent extends CondorStatus
case class CondorJobNotSubmitted extends CondorStatus
case class CondorSubmitFailed(msg:String) extends CondorStatus

case class SendCondorJob(job:CondorJob)
case class LogUpdate(id:Long,event:CondorStatus)
