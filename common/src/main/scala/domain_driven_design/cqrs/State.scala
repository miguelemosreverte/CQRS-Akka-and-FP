package domain_driven_design.cqrs

trait State[E <: Event, Self <: State[E, Self]] {

  def +(event: E): Self

}
