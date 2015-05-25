communicator = getCommunicator communicationChannel

def changeStringAttribute(){
    setAttributeValue resourceName, '1.0', 'Frank Underwood'
    communicator.post getAttributeValue(resourceName, '1.0')
}

def changeStringAttributeSilent(){
    setAttributeValue resourceName, '1.0', 'Barry Burton'
}

def changeBooleanAttribute(){
    setAttributeValue resourceName, '2.0', true
    communicator.post getAttributeValue(resourceName, '2.0')
}

def changeIntegerAttribute(){
    setAttributeValue resourceName, '3.0', 1020
    communicator.post getAttributeValue(resourceName, '3.0')
}

def changeBigIntegerAttribute(){
    setAttributeValue resourceName, '3.0', 1020G
    communicator.post getAttributeValue(resourceName, '3.0')
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
