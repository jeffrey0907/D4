package d4.core

import akka.actor.ActorIdentity
import d4.core.D4AggregateRoot._
import d4.core.messaging.{D4Event, D4RootCommand, RandomD4RootCommand}

/**
 * Created by jeffrey on 1/26/15.
 */
object D4AggregateRoot {

  sealed abstract class  AggregateCommand extends RandomD4RootCommand
  sealed abstract class  AggregateEvent extends D4Event

  // Aggregate Commands
  case class AggregateRootIdentify(rootId:AggregateRootIdType) extends AggregateCommand
  case class CreateAggregate(rootId: AggregateRootIdType) extends AggregateCommand
  case class RestoreAggregate(rootId: AggregateRootIdType) extends AggregateCommand

  // Aggregate Events
  case class AggregateCreated(rootId: AggregateRootIdType, cmdId: CommandIdType) extends AggregateEvent
  case class AggregateRestored(rootId: AggregateRootIdType, cmdId: CommandIdType) extends AggregateEvent

  // Aggregate State
  object AggregateState extends Enumeration {
    type AggregateState = Value
    val Unload = Value
    val Loaded = Value
  }
}

abstract class D4AggregateRoot extends D4Entity {
  private var _rootId : AggregateRootIdType = ""
  private var _state = AggregateState.Unload

  def rootId = this._rootId

  import d4.core.D4AggregateRoot.AggregateState._

  override def receive: Receive = {
    case cmd : AggregateCommand => handleAggregateCommand(cmd)
    case cmd : D4RootCommand if _state == Loaded => handleCommand(cmd)
    case m => unhandled(m)
  }

  private def handleAggregateCommand(cmd : AggregateCommand) = {
    cmd match {
      case cmd@CreateAggregate(rId) => createAggregate(rId, cmd.cmdId)
      case cmd@RestoreAggregate(rId) => restoreAggregate(rId, cmd.cmdId)
      case AggregateRootIdentify(rId) => sender ! ActorIdentity(rId, Some(self))
    }
  }

  private def createAggregate(rootId : AggregateRootIdType, cmdId: CommandIdType) : Unit = {
    this._rootId = rootId
    onCreate(this._rootId)
    sender ! AggregateCreated(rootId, cmdId)
    _state = Loaded

    log.info(self.path.toStringWithAddress(self.path.address) + " createAggregateRoot " + rootId)
  }

  private def restoreAggregate(rootId : AggregateRootIdType, cmdId: CommandIdType) : Unit = {
    this._rootId = rootId
    onRestore(this._rootId)
    sender ! AggregateRestored(rootId, cmdId)
    _state = Loaded
  }

  protected def onCreate(rootId : AggregateRootIdType): Unit = {

  }

  protected def onRestore(rootId : AggregateRootIdType): Unit = {

  }

  def handleCommand(cmd : D4RootCommand)
}
