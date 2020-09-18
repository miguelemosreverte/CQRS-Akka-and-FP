package domain_driven_design.building_blocks

trait Aggregate[F[_], ID] {
  def aggregateRoot: ID
  def entities: F[Entity[ID]]
}
