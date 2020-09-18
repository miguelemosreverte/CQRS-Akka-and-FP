package proof_of_concept.infrastructure

import akka.persistence.PersistentActor
import domain_driven_design.cqrs.{Event, State}

abstract class BasePersistentActor[
    E <: Event,
    StateIdentity,
    S <: State[E, StateIdentity]
]() extends PersistentActor {
  var state: S
  override def persistenceId: String = this.context.self.path.name

  override def receiveRecover: Receive = {
    case event: E =>
      state += event
  }
}
