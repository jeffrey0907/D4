package d4.support

import com.typesafe.config.Config
import d4.core.D4BoundedContext
import org.scalatest.{FeatureSpec, GivenWhenThen, BeforeAndAfterAll, FlatSpec}

/**
 * Created by jeffrey on 1/28/15.
 */
abstract class BaseFlatTestSpec(implicit val _config: Config)
  extends FlatSpec with BeforeAndAfterAll  with GivenWhenThen {

  protected implicit val boundedContext: D4BoundedContext = createD4BoundedContext()

  private def createD4BoundedContext() = {
    val context = new D4BoundedContext(D4BoundedContextTestSupport.D4BoundedContextName)
    context.init(_config)
  }

}

abstract class BaseFeatureTestSpec(implicit val _config : Config)
  extends FeatureSpec with BeforeAndAfterAll with GivenWhenThen{

  protected implicit val boundedContext : D4BoundedContext = createD4BoundedContext()

  private def createD4BoundedContext() = {
    val context = new D4BoundedContext(D4BoundedContextTestSupport.D4BoundedContextName)
    context.init(_config)
  }

}
