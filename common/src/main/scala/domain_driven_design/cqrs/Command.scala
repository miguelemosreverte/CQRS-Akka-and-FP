package domain_driven_design.cqrs

import akka.entity.ShardedEntity.Sharded

trait Command extends Sharded
