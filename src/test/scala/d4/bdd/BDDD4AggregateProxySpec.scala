package d4.bdd

import java.util.concurrent.TimeUnit

import d4.core.AggregateRootIdType
import d4.support.D4BoundedContextTestSupport._
import order.concept.OrderD4AggregateRoot
import OrderD4AggregateRoot.{BaseInfoUpdated, OrderBaseInfo, UpdateBaseInfo}
import d4.support.BaseFeatureTestSpec

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class BDDD4AggregateProxySpec extends BaseFeatureTestSpec {
  val orderid01: AggregateRootIdType = "OrderID000001"

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    boundedContext.add(classOf[OrderD4AggregateRoot])
  }

  override protected def afterAll(): Unit = {
    boundedContext.stop()
    super.afterAll()
  }

  feature("Helping for creating D4AggregateRoot") {
    scenario("Creating D4AggregateRoot") {
      Given("Concept has been created")
      val orderAggregateProxy = boundedContext.getAggregateProxy(classOf[OrderD4AggregateRoot])
      assert(orderAggregateProxy != null)
      var futureRoot = orderAggregateProxy.contains(orderid01)
      assert(Await.result(futureRoot, Duration(2, TimeUnit.SECONDS)).correlationId == orderid01)

      When("Create a new D4AggregateRoot")
      orderAggregateProxy.createAggregateRoot(orderid01)

      Then("There is only one D4AggregateRoot in the Concept")
      futureRoot = orderAggregateProxy.contains(orderid01)
      val root = Await.result(futureRoot, Duration(2, TimeUnit.SECONDS))
      assert(root != null)
      assert(root.correlationId == orderid01)
    }

    // TODO Restoring D4AggregateRoot
    scenario("Restoring D4AggregateRoot") {
      val orderAggregateProxy = boundedContext.getAggregateProxy(classOf[OrderD4AggregateRoot])

    }

    // TODO Remove D4AggregateRoot
    scenario ("Remove D4AggregateRoot") {

    }

  }

  feature("Deliver Command") {
    scenario("Deliver Message Sync") {
      val orderAggregateProxy = boundedContext.getAggregateProxy(classOf[OrderD4AggregateRoot])
      val updateBaseInfo = UpdateBaseInfo(orderid01, OrderBaseInfo(orderid01, 100, 100))
      val baseInfoUpdated = orderAggregateProxy.deliver(updateBaseInfo).asInstanceOf[BaseInfoUpdated]

      assert(baseInfoUpdated != null)
      assert(baseInfoUpdated.cmdId == updateBaseInfo.cmdId)
      assert(baseInfoUpdated.success)
    }

    // TODO Deliver Message ASyncf
    scenario("Deliver Message ASync") {

    }

  }
}
