package actor_model

import akka.persistence.PersistentActor
import domain_driven_design.cqrs.{Event, State}

import scala.reflect.ClassTag

abstract class BasePersistentActor[
    E <: Event: ClassTag,
    S <: State[E, S]
]() extends PersistentActor {
  var state: S
  override def persistenceId: String = this.context.self.path.name

  override def receiveRecover: Receive = {
    case event: E =>
      state += event
  }
}
