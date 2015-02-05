package d4.core.messaging

import d4.core.CommandIdType

/**
 * Created by jeffrey on 1/30/15.
 */
abstract class D4Event extends Serializable{
  val cmdId : CommandIdType
}
