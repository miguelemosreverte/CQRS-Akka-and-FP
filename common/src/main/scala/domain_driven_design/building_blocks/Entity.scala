package domain_driven_design.building_blocks

trait Entity[ID] {
  def id: ID
}
