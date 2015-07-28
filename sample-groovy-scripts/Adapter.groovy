communicator = getCommunicator communicationChannel

def changeStringAttribute(){
    resources.setAttributeValue resourceName, '1.0', 'Frank Underwood'
    communicator.post resources.getAttributeValue(resourceName, '1.0')
}

def changeStringAttributeSilent(){
    def res = resources.getResource resourceName
    res./1.0/.value = 'Barry Burton'
}

def changeBooleanAttribute(){
    def res = resources.getResource(resourceName)
    res.getAttribute('2.0').value = true
    assert res./2.0/.metadata.objectName instanceof String
    assert res./2.0/.metadata.readable
    assert res./2.0/.metadata.writable
    communicator.post res./2.0/.value
}

def changeIntegerAttribute(){
    resources.setAttributeValue resourceName, '3.0', 1020
    communicator.post resources.getAttributeValue(resourceName, '3.0')
}

def changeBigIntegerAttribute(){
    resources./test-target/.bi.value = 1020G
    communicator.post resources./test-target/.bi.value
}

def listen(message){
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
    communicator.post(notif)
}

//register incoming message listener
communicator.register asListener(this.&listen)

void close(){
    communicator = null
}
