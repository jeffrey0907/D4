package order.concept

import java.util.Date

import d4.core.messaging.{D4Event, D4RootCommand, RandomD4RootCommand}
import d4.core.{AggregateRootIdType, CommandIdType, D4AggregateRoot}
import order.concept.OrderD4AggregateRoot.{BaseInfoUpdated, UpdateBaseInfo}

/**
 * Created by jeffrey on 1/28/15.
 */

object OrderD4AggregateRoot {

  sealed abstract class OrderCommand extends RandomD4RootCommand
  sealed abstract class OrderEvent extends D4Event

  // Order Command
  case class UpdateBaseInfo(rootId: AggregateRootIdType, baseInfo: OrderBaseInfo) extends OrderCommand

  // Order Event
  case class BaseInfoUpdated(cmdId: CommandIdType, success: Boolean) extends OrderEvent

  @SerialVersionUID(0)
  case class OrderBaseInfo(OrderId:String, Uid:Long, Price:Double, OrderTime:Date = new Date())
}

class OrderD4AggregateRoot extends D4AggregateRoot {

  override def handleCommand(cmd: D4RootCommand): Unit = {
    cmd match {
      case cmd : UpdateBaseInfo => updateBaseInfo(cmd)
    }
  }

  def updateBaseInfo(cmd: UpdateBaseInfo): Unit = {
    require(!cmd.baseInfo.OrderId.isEmpty)
    require(cmd.baseInfo.Uid > 0)

    sender() ! BaseInfoUpdated(cmd.cmdId, true)
    log.info(self.path.toStringWithAddress(self.path.address) + " Update baseInfo " + cmd.baseInfo.toString + " Commands from " +
    sender().path.toStringWithAddress(sender().path.address))
  }
}
