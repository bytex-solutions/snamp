communicator = getCommunicator communicationChannel

def changeStringAttribute(){
    resources.setAttributeValue resourceName, 'string', 'Frank Underwood'
    communicator.post resources.getAttributeValue(resourceName, 'string')
}

def changeStringAttributeSilent(){
    def res = resources.getResource resourceName
    res./string/.value = 'Barry Burton'
}

def changeBooleanAttribute(){
    def res = resources.getResource(resourceName)
    res.getAttribute('boolean').value = true
    assert res./boolean/.metadata.objectName instanceof String
    assert res./boolean/.metadata.readable
    assert res./boolean/.metadata.writable
    communicator.sendSignal res./boolean/.value
}

def changeIntegerAttribute(){
    resources.setAttributeValue resourceName, 'int32', 1020
    communicator.sendSignal resources.getAttributeValue(resourceName, 'int32')
}

def changeBigIntegerAttribute(){
    resources./test-target/./bigint/.value = 1020G
    communicator.sendSignal resources./test-target/./bigint/.value
}

void listen(message){
    switch(message){
        case 'changeStringAttributeSilent':
            changeStringAttributeSilent()
        break
        case 'changeStringAttribute':
            changeStringAttribute()
        break
        case 'changeBooleanAttribute':
            changeBooleanAttribute()
        break
        case 'changeIntegerAttribute':
            changeIntegerAttribute()
        break
        case 'changeBigIntegerAttribute':
            changeBigIntegerAttribute()
        break
    }
}

def handleNotification(metadata, notif){
    communicator.sendSignal(notif)
}

//register incoming message listener
communicator.addMessageListener(this.&listen, {msg -> true})

void close(){
    communicator = null
}
