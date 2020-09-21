package pub_sub.algebra

case class PubSub[MessageBrokerRequirements, MessageProcessorOut, MessageProcessorAlgorithmOut](
    messageProcessor: MessageBrokerRequirements => MessageProcessor.MessageProcessor[MessageProcessorOut,
                                                                                     MessageProcessorAlgorithmOut],
    messageProducer: MessageBrokerRequirements => MessageProducer.MessageProducer[_]
)
