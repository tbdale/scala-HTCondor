package tbdale.hpc.scala.condorAPI

import java.io.File
import org.apache.commons.io.input._

/*
 * CondorLogMonitor 'tails' condor logfile looking for events to message back to manager
 */

class CondorLogMonitor (val logfile:File, manager: CondorJobManager ) extends TailerListenerAdapter {
     def handleLine(l:String){
   }
  override def handle(line:String) = {
          handleLine(line)
  }
 def start(delay:Int):Tailer = {
    Tailer.create(logfile, this, delay)
  }

}
