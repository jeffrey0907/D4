package d4.core

import akka.actor._
import d4.core.D4AggregateSupervisor.{AggregateProxyRemoved, RemoveAggregateProxy, AggregateProxyAdded, AddAggregateProxy}
import d4.core.messaging.{D4Event, RandomD4Command, D4Command}

import scala.collection.mutable

/**
 * Created by jeffrey on 1/30/15.
 */
private [core] object D4AggregateSupervisor {

  // Command
  sealed trait SupervisorCmd extends RandomD4Command
  case class AddAggregateProxy(proxy : D4AggregateProxy) extends SupervisorCmd
  case class RemoveAggregateProxy(proxy: D4AggregateProxy) extends SupervisorCmd

  // Event
  sealed trait SupervisorEvent extends D4Event
  case class AggregateProxyAdded(cmdId: CommandIdType, success : Boolean) extends SupervisorEvent
  case class AggregateProxyRemoved(cmdId: CommandIdType, success : Boolean) extends SupervisorEvent

  def props(context : D4BoundedContext) = {
    Props(classOf[D4AggregateSupervisor], context)
  }
}

private [core] class D4AggregateSupervisor(val boundedContext : D4BoundedContext)
  extends Actor with ActorLogging {
  private val proxyMap = mutable.Map[ActorRef, D4AggregateProxy]()

  override def receive: Receive = {
    case cmd @ AddAggregateProxy(proxy) => addAggregateProxy(cmd.cmdId, proxy)
    case cmd @ RemoveAggregateProxy(proxy) => removeAggregateProxy(cmd.cmdId, proxy)
  }

  def addAggregateProxy(cmdId: CommandIdType, proxy: D4AggregateProxy): Unit = {
    if (proxy != null && proxy.shardRegion != null) {
      proxyMap += proxy.shardRegion -> proxy
      sender ! AggregateProxyAdded(cmdId, true)
    } else {
      sender ! AggregateProxyAdded(cmdId, false)
    }
  }

  def removeAggregateProxy(cmdId: CommandIdType, proxy: D4AggregateProxy): Unit = {
    if (proxy != null && proxy.shardRegion != null) {
      proxy.shardRegion ! PoisonPill
      proxyMap -= proxy.shardRegion
      sender() ! AggregateProxyRemoved(cmdId, true)
    } else {
      sender() ! AggregateProxyRemoved(cmdId, false)
    }

    log.info(proxy.rootType.getName)
  }

}