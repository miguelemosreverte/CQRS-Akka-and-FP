package actor_model.system_parallelizable

import java.util.concurrent.atomic.AtomicInteger

object AvailablePortProvider {
  // expected ports to bind
  // @TODO add to configuration
  private val existingPorts = 55000 to 56000
  private val lastIndexGiven = new AtomicInteger(0)
  def port: Int = synchronized {
    existingPorts(lastIndexGiven.getAndIncrement())
  }
}
