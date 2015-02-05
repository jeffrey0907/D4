package d4.bdd

import d4.support.D4BoundedContextTestSupport._
import d4.support.BaseFeatureTestSpec
import order.concept.OrderD4AggregateRoot

/**
 * Created by jeffrey on 1/20/15.
 */

class BDDD4BoundedContextSpec extends BaseFeatureTestSpec {
  info("D4BoundedContext define A System")
  info("I can add A D4AggregateProxy in which I can create many D4AggregateRoot regardless of Cluster matters")

  override protected def afterAll(): Unit = {
    boundedContext.stop()
    super.afterAll()
  }

  feature("BDD D4BoundedContext manage D4AggregateRoot") {

    scenario("Add D4AggregateRoot") {
      Given("D4BoundedContext had been created")
      assert(!boundedContext.contains(classOf[OrderD4AggregateRoot]))

      When("Add a new Concept")
      boundedContext.add(classOf[OrderD4AggregateRoot])

      Then("Context contains the new Concept")
      assert(boundedContext.contains(classOf[OrderD4AggregateRoot]))
    }

    scenario("Get D4AggregateProxy") {
      Given("OrderD4AggregateRoot has been added into D4BoundedContext")
      assert(boundedContext.contains(classOf[OrderD4AggregateRoot]))

      When("Get D4AggregateProxy object by class of OrderAggregateProxy")
      val concept = boundedContext.getAggregateProxy(classOf[OrderD4AggregateRoot])

      Then("D4AggregateProxy object is not Null, And object type is OrderAggregateProxy ")
      assert(concept != null)
    }

    scenario("Remove D4AggregateProxy") {
      Given("OrderD4AggregateRoot has been added into D4BoundedContext")
      assert(boundedContext.contains(classOf[OrderD4AggregateRoot]))

      When("Remove OrderD4AggregateRoot from _D4BoundedContext")
      boundedContext.remove(classOf[OrderD4AggregateRoot])

      Then("_D4BoundedContext does not contain OrderD4AggregateRoot")
      assert(!boundedContext.contains(classOf[OrderD4AggregateRoot]))
    }
  }


}
