package domain_driven_design

trait Entity[ID] {
  def id: ID
}
