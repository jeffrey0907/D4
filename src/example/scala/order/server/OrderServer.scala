package order.server

import d4.core.D4BoundedContext
import order.concept.OrderD4AggregateRoot
import order.concept.OrderD4AggregateRoot.{OrderBaseInfo, UpdateBaseInfo}

/**
 * Created by jeffrey on 2/2/15.
 */
object OrderServer {

  def main(args: Array[String]) {
//    val context = D4BoundedContext("OrderServer")

    // Node 1
    val context1 = D4BoundedContext("OrderServer", "Node1", "d4Order1.conf")
    val orderNode1 = context1.add(classOf[OrderD4AggregateRoot])

    // Node 2
    val context2 = D4BoundedContext("OrderServer", "Node2", "d4Order2.conf")
    val orderNode2 = context2.add(classOf[OrderD4AggregateRoot])

    // Node3
    val context3 = D4BoundedContext("OrderServer", "Node3", "d4Order3.conf")
    val orderNode3 = context3.add(classOf[OrderD4AggregateRoot])

    // Create A Order through Node1, And the instance is in Node1
    val OrderID = "order_00001"
    orderNode1.createAggregateRoot(OrderID)

    val OrderID2 = "order_00002"
    orderNode1.createAggregateRoot(OrderID2)

    val OrderID3 = "order_00003"
    orderNode1.createAggregateRoot(OrderID3)

    // Update Order through Node3
    val cmdUpdatebaseInfoNode3 = UpdateBaseInfo(OrderID, OrderBaseInfo(OrderID, 100, 100))
    val event = orderNode3.deliver(cmdUpdatebaseInfoNode3)
    println(event.toString)
    
    // Update Order through Node1
    val cmdUpdatebaseInfoNode1 = cmdUpdatebaseInfoNode3.copy(baseInfo = cmdUpdatebaseInfoNode3.baseInfo.copy(Price=300))
    orderNode2.deliver(cmdUpdatebaseInfoNode1)
  }
}
