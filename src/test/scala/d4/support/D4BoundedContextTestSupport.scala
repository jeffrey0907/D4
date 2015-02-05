package d4.support

import java.util.concurrent.atomic.AtomicInteger

import com.typesafe.config.{Config, ConfigFactory}
import d4.core.D4BoundedContext
import org.slf4j.{LoggerFactory, Logger}

/**
 * Created by jeffrey on 1/28/15.
 */
object D4BoundedContextTestSupport {
  private val logger = LoggerFactory.getLogger(D4BoundedContextTestSupport.getClass)

  val D4BoundedContextName: String = "D4BoundedContext"

  def createD4BoundedContext(): D4BoundedContext = {
    D4BoundedContext(D4BoundedContextName)
  }

  implicit def testD4BoundedContext : D4BoundedContext = createD4BoundedContext()


  private val testPort : AtomicInteger = new AtomicInteger(2661)
  private val seedNode = "localhost:%d"

  private val ConfigTemplate =
    """
      |d4.remote.port=%d
      |d4.cluster.seeds=[%s]
    """.stripMargin

  def createContextConfig(): Config = {
    val port = testPort.getAndIncrement
    val seed = "\"%s\"".format(seedNode.format(port))

    logger.info("current Port is:[%d]".format(port))

    ConfigFactory.parseString(ConfigTemplate.format(port, seed))
  }

  implicit def testContextConfig : Config = createContextConfig()
}
