package d4.core.messaging

import d4.core._

/**
 * Created by jeffrey on 2/13/15.
 */
abstract class D4Reply extends Serializable {
  val cmdId : CommandIdType
}
