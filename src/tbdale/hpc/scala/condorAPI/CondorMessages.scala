package tbdale.hpc.scala.condorAPI

trait CondorStatus

case class CondorJobNotSubmitted() extends CondorStatus
case class CondorSubmitFailed(msg:String) extends CondorStatus