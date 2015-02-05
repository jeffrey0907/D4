package d4.core

import d4.support.BaseFlatTestSpec
import d4.support.D4BoundedContextTestSupport._
import order.concept.OrderD4AggregateRoot

/**
 * Created by jeffrey on 1/26/15.
 */

class D4BoundedContextSpec extends BaseFlatTestSpec {

  override protected def afterAll(): Unit = {
    boundedContext.stop()
    super.afterAll()
  }

  //
  //  Test for A D4BoundedContext
  //
  behavior of "A D4BoundedContext"

  it should "Has no BoundedContext in it" in {
    assert(!D4BoundedContext.contains(boundedContext.name))
    assert(boundedContext != null)
  }

  //
  //  Test for A AggregateRoot
  //
  behavior of "A AggregateRoot"

  it should "added into a BoundedContext" in {
    Given("OrderAggregateProxy not in BoundedContext")
    assert(!boundedContext.contains(classOf[OrderD4AggregateRoot]))

    When("Add OrderAggregateProxy into BoundedContext")
    val aggregateProxy = boundedContext.add(classOf[OrderD4AggregateRoot])

    Then("BoundedContext contains OrderAggregateProxy")
    assert(aggregateProxy != null)
    assert(boundedContext.contains(classOf[OrderD4AggregateRoot]))
    assert(boundedContext.getAggregateProxy(classOf[OrderD4AggregateRoot]) == aggregateProxy)

    // when the Concept is already in the BoundedContext
    // do noting just return the existing one
    val newOrderConcept = boundedContext.add(classOf[OrderD4AggregateRoot])
    assert(newOrderConcept == aggregateProxy)
  }

  it should "Retrieve OrderAggregateProxy" in {
    Given("OrderAggregateProxy has been in BoundedContext")
    assert(boundedContext.contains(classOf[OrderD4AggregateRoot]))

    When("Get OrderAggregateProxy from BoundedContext")
    val context = boundedContext.getAggregateProxy(classOf[OrderD4AggregateRoot])

    Then("Return the instance of OrderAggregateProxy")
    assert(context != null)
  }

  it should "removed from a BoundedContext" in {
    Given("OrderAggregateProxy has already in BoundedContext")
    assert(boundedContext.contains(classOf[OrderD4AggregateRoot]))

    When("Remove OrderAggregateProxy from BoundedContext")
    boundedContext.remove(classOf[OrderD4AggregateRoot])

    Then("OrderAggregateProxy has gone")
    assert(!boundedContext.contains(classOf[OrderD4AggregateRoot]))
    assert(boundedContext.getAggregateProxy(classOf[OrderD4AggregateRoot]) == null)
  }

}
