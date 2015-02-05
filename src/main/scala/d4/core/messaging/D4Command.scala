package d4.core.messaging

import d4.core.{AggregateRootIdType, CommandIdType}

import scala.util.Random

/**
 * Created by jeffrey on 1/28/15.
 */
abstract class D4Command extends Serializable {
  def cmdId : CommandIdType
}

abstract class D4RootCommand extends D4Command {
  val rootId : AggregateRootIdType
}

abstract class RandomD4Command extends D4Command {
  val cmdId : Long = Random.nextLong()
}

abstract class RandomD4RootCommand extends D4RootCommand {
  val cmdId : Long = Random.nextLong()
}