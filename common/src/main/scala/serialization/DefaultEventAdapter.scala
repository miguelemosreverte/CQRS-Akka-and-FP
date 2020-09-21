package serialization

import akka.persistence.journal.{EventAdapter, EventSeq}

trait DefaultEventAdapter extends EventAdapter {
  override def manifest(event: Any): String = event.getClass.getName
  override def toJournal(event: Any): Any = event
  override def fromJournal(e: Any, manifest: String): EventSeq = EventSeq.single(e)
}

object DefaultEventAdapter extends DefaultEventAdapter
