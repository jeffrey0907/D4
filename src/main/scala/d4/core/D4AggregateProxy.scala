package d4.core

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorIdentity, ActorRef, Props}
import akka.contrib.pattern.ShardRegion
import akka.util.Timeout
import d4.core.D4AggregateRoot.AggregateCreated
import d4.core.exception.UnInitD4ConceptException
import d4.core.messaging.{D4Event, D4RootCommand, D4Command}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.reflect.ClassTag

import akka.pattern.ask

/**
 * Created by jeffrey on 1/26/15.
 */

private [d4] object D4AggregateProxy {

  val shardResolver : ShardRegion.ShardResolver = {
    case cmd : D4RootCommand => (cmd.rootId.hashCode % 64).toString
  }

  val idExtractor : ShardRegion.IdExtractor = {
    case cmd : D4RootCommand => (cmd.rootId, cmd)
  }

  def apply(rootType : Class[_ <: D4AggregateRoot]) = {
    new D4AggregateProxy(rootType)
  }
}

class D4AggregateProxy private[d4] (val rootType : Class[_ <: D4AggregateRoot]) {

  private [core] var shardRegion : ActorRef = null
  private implicit val TIMEOUT : Timeout = 3000

  private [d4] def props() : Props = {
    Props(rootType)
  }

  def createAggregateRoot(rootId: AggregateRootIdType) = {
    if (shardRegion == null) {
      throw new UnInitD4ConceptException
    }

    ask(shardRegion, D4AggregateRoot.CreateAggregate(rootId)).mapTo[AggregateCreated]
  }

  def restoreAggregateRoot(rootId: AggregateRootIdType) = {

  }

  private [d4] def contains(rootId:AggregateRootIdType) : Future[ActorIdentity] = {
    if (shardRegion == null) {
      throw new UnInitD4ConceptException
    }

    ask(shardRegion, (new D4AggregateRoot.AggregateRootIdentify(rootId))).mapTo[ActorIdentity]
  }

  def deliver(cmd: D4RootCommand)(implicit timeout:Duration= Duration(3, TimeUnit.SECONDS)): D4Event = {
    val future = deliverA(cmd)
    Await.result(future, timeout)
  }

  def deliverA(cmd: D4RootCommand): Future[D4Event] = {
    ask(shardRegion, cmd).mapTo[D4Event]
  }

  def deliverForget(cmd: D4RootCommand): Unit = {
    shardRegion ! cmd
  }
}
