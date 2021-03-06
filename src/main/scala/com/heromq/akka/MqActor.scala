package com.heromq.akka

import akka.actor.Actor
import javax.jms.{Destination, Session}
import org.fusesource.stomp.jms.StompJmsConnectionFactory
import grizzled.slf4j.Logging

trait MessagingStyle {
  def destinationName: String
  def createDestination(session: Session): Destination
}

trait MQ extends MessagingStyle {
  def createDestination(session: Session) = session.createQueue(destinationName)
}

trait PubSub extends MessagingStyle {
  def createDestination(session: Session) = session.createTopic(destinationName)
}

trait MqActor extends Actor with Logging {thisEnv: MessagingStyle =>

  def brokerUri: String

  lazy val connection = {
    val cf = new StompJmsConnectionFactory
    cf.setBrokerURI(brokerUri)
    cf.createConnection()
  }

  lazy val session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE)
  lazy val destination = thisEnv.createDestination(session)

  override def preStart() {
    connection.start()
    info("started receiving")
  }

  override def postStop() {
    connection.close()
  }

}
