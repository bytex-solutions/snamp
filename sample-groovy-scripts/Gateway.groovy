import com.bytex.snamp.core.Communicator.MessageType

def changeStringAttribute(messageId, communicator){
    resources.setAttributeValue resourceName, 'string', 'Frank Underwood'
    communicator.sendMessage(resources.getAttributeValue(resourceName, 'string'), MessageType.RESPONSE, messageId)
}

def changeStringAttributeSilent(){
    def res = resources.getResource resourceName
    res./string/.value = 'Barry Burton'
}

def changeBooleanAttribute(messageId, communicator){
    def res = resources.getResource(resourceName)
    res.getAttribute('boolean').value = true
    assert res./boolean/.metadata.objectName instanceof String
    assert res./boolean/.metadata.readable
    assert res./boolean/.metadata.writable
    communicator.sendMessage(res./boolean/.value, MessageType.RESPONSE, messageId)
}

def changeIntegerAttribute(messageId, communicator){
    resources.setAttributeValue resourceName, 'int32', 1020
    communicator.sendMessage(resources.getAttributeValue(resourceName, 'int32'), MessageType.RESPONSE, messageId)
}

def changeBigIntegerAttribute(messageId, communicator){
    resources./test-target/./bigint/.value = 1020G
    communicator.sendMessage(resources./test-target/./bigint/.value, MessageType.RESPONSE, messageId)
}

void listen(message, communicator){
    switch(message.payload){
        case 'changeStringAttributeSilent':
            changeStringAttributeSilent()
        break
        case 'changeStringAttribute':
            changeStringAttribute(message.messageID, communicator)
        break
        case 'changeBooleanAttribute':
            changeBooleanAttribute(message.messageID, communicator)
        break
        case 'changeIntegerAttribute':
            changeIntegerAttribute(message.messageID, communicator)
        break
        case 'changeBigIntegerAttribute':
            changeBigIntegerAttribute(message.messageID, communicator)
        break
    }
}

def handleNotification(metadata, notif){
    communicator.sendSignal(notif)
}

//register incoming message listener
def communicator = getCommunicator communicationChannel
communicator.addMessageListener({msg -> listen(msg, communicator)}, MessageType.REQUEST)

void close(){
}
