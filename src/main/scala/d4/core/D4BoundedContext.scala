package d4.core

import java.util.concurrent.{TimeUnit, ConcurrentHashMap}

import akka.actor.Actor.Receive
import akka.actor._
import akka.contrib.pattern.ClusterSharding
import com.typesafe.config.{Config, ConfigFactory, ConfigValueFactory}
import d4.core.D4AggregateSupervisor.{AggregateProxyRemoved, RemoveAggregateProxy, AggregateProxyAdded, AddAggregateProxy}

import scala.collection.convert.decorateAll._
import scala.concurrent.duration.FiniteDuration

/**
 * Created by jeffrey on 1/26/15.
 */
object D4BoundedContext {
  private[core] val HOSTNAME_KEY: String = "akka.remote.netty.tcp.hostname"
  private[core] val PORT_KEY: String = "akka.remote.netty.tcp.port"
  private[core] val SEEDNODES_KEY: String = "akka.cluster.seed-nodes"

  private val boundedContextMap = new ConcurrentHashMap[String, D4BoundedContext]

  def apply(serverName:String, alias:String, configFile:String): D4BoundedContext = {
    if (!boundedContextMap.containsKey(alias)) {
      val context = new D4BoundedContext(serverName, configFile)
      boundedContextMap.put(alias, context)
      context.init()
    }
    boundedContextMap.get(alias)
  }

  def apply(serverName:String) : D4BoundedContext = {
    apply(serverName, serverName, "d4.conf")
  }

  def get(name:String) : D4BoundedContext = {
    boundedContextMap.get(name)
  }

  def contains(name:String) : Boolean = {
    boundedContextMap.containsKey(name)
  }

  def hasConcept() : Boolean = {
    !boundedContextMap.isEmpty
  }

  def stop(name:String): Option[D4BoundedContext] = {
    if (boundedContextMap.containsKey(name)) {
      boundedContextMap.get(name).stop()
      Some(boundedContextMap.remove(name))
    } else {
      None
    }
  }
}

class D4BoundedContext private[d4] (val name:String, configFile:String = "d4.conf") (private implicit var _system : ActorSystem = null) {
  private var _boundedContxtConfig : D4BoundedContextConfig = null
  private var _aggregateSupervisor : ActorRef = null

  private val conceptMaps = new ConcurrentHashMap[Class[_ <: D4AggregateRoot], D4AggregateProxy]

  def getAggregateProxy(aggregateRootType: Class[_ <: D4AggregateRoot]) : D4AggregateProxy = {
    if (!isInitialized) {
      null
    } else {
      conceptMaps.get(aggregateRootType)
    }
  }

  def add(aggregaateRootType : Class[_ <: D4AggregateRoot]) = {
    if (!isInitialized) {
      null
    } else {
      if (!conceptMaps.containsKey(aggregaateRootType)) {
        val aggregateProxy = D4AggregateProxy(aggregaateRootType)
        _system.actorOf(Props(aggregaateRootType), aggregaateRootType.getName)
        // TODO how to watch shardRegionActorRef
        val shardRegion = ClusterSharding(_system).start(this.name+"Shard", Some(aggregateProxy.props), D4AggregateProxy.idExtractor, D4AggregateProxy.shardResolver)
        aggregateProxy.shardRegion = shardRegion

        val inbox = Inbox.create(_system)
        val cmdAdd = AddAggregateProxy(aggregateProxy)
        inbox.send(_aggregateSupervisor, cmdAdd)
        inbox.receive(FiniteDuration(1, TimeUnit.SECONDS)) match {
          case AggregateProxyAdded(cmdAdd.cmdId, result) => if (result) conceptMaps.put(aggregaateRootType, aggregateProxy)
        }
        inbox.getRef()
      }
      conceptMaps.get(aggregaateRootType)
    }
  }

  def contains(aggregateRootType: Class[_ <: D4AggregateRoot]): Boolean = {
    if (!isInitialized) {
      false
    } else {
      conceptMaps.get(aggregateRootType) != null
    }
  }

  def remove(aggregateRootType: Class[_ <: D4AggregateRoot]) = {
    if (!isInitialized) {
      null
    } else {
      if (conceptMaps.containsKey(aggregateRootType)) {
        val proxy = conceptMaps.get(aggregateRootType)
        val inbox = Inbox.create(_system)
        val removeCmd = RemoveAggregateProxy(proxy)
        inbox.send(_aggregateSupervisor, removeCmd)
        inbox.receive(FiniteDuration(1, TimeUnit.SECONDS)) match {
          case AggregateProxyRemoved(removeCmd.cmdId, result) => if (result) conceptMaps.remove(aggregateRootType)
        }
      }
    }
  }

  private [d4] def stop(): Unit = {
    if (_system != null)
      _system.shutdown()
  }

  def init(config:Config = null) : D4BoundedContext = {
    if (_boundedContxtConfig == null) {
      _boundedContxtConfig = new D4BoundedContextConfig(loadConfig(config))
    }

    startServer()
    this
  }

  private def isInitialized : Boolean = {
    _system != null
  }

  private def loadConfig(_config : Config) = {
    var config: Config = ConfigFactory.empty
    if (_config != null) {
      config = config.withFallback(_config)
    }

    config = config.withFallback(ConfigFactory.parseURL(getClass.getClassLoader.getResource(configFile)).getConfig(this.name))

    val seeds = for (c <- config.getStringList("d4.cluster.seeds").asScala.filter(D4BoundedContextConfig.isValidatedSeedNode(_)))
    yield {
      "akka.tcp://%s@%s".format(this.name, c)
    }

    var finalD4Config =  ConfigFactory.empty().withValue(D4BoundedContext.HOSTNAME_KEY, ConfigValueFactory.fromAnyRef(config.getString("d4.remote.hostname"))).
      withValue(D4BoundedContext.PORT_KEY, ConfigValueFactory.fromAnyRef(config.getString("d4.remote.port"))).
      withValue(D4BoundedContext.SEEDNODES_KEY, ConfigValueFactory.fromIterable(seeds.asJavaCollection))

    val defaultD4Config = ConfigFactory.load().getConfig("d4")
    if (defaultD4Config != null) {
      finalD4Config = finalD4Config.withFallback(defaultD4Config)
    }
    finalD4Config
  }

  def getBoundedContextConfig(): D4BoundedContextConfig = {
    _boundedContxtConfig
  }

  private [core] def system() : Option[ActorSystem] = {
    Option(_system)
  }

  private def startServer() : Unit = {
    if (_system == null) {
      _system = ActorSystem.create(this.name, this.getBoundedContextConfig().config)
    }

    if (_aggregateSupervisor == null && _system != null) {
      _aggregateSupervisor = _system.actorOf(D4AggregateSupervisor.props(this))
    }
  }

}
