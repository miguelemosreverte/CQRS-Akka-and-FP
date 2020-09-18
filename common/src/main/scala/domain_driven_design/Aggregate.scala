package domain_driven_design

trait Aggregate[F[_], ID] {
  def entities: F[Entity[ID]]
}
