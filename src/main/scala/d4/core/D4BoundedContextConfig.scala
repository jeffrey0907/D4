package d4.core

import com.typesafe.config.Config

/**
 * Created by jeffrey on 1/27/15.
 */

final class D4BoundedContextConfig(val config:Config) {

  val remoteIP = {
    config.getString(D4BoundedContext.HOSTNAME_KEY)
  }

  val remotePort = {
    config.getInt(D4BoundedContext.PORT_KEY)
  }

  val seedNodes = {
    config.getStringList(D4BoundedContext.SEEDNODES_KEY)
  }

}

object D4BoundedContextConfig {
  private[core] def isValidatedSeedNode(seedNode:String) : Boolean = {
    seedNode.matches("\\S+:\\d+")
  }
}
