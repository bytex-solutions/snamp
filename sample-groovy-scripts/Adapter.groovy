communicator = getCommunicator communicationChannel

def changeStringAttribute(){
    setAttributeValue resourceName, '1.0', 'Frank Underwood'
    communicator.post getAttributeValue(resourceName, '1.0')
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
}

def listen(message){
    switch(message){
        case 'changeStringAttribute':
            changeStringAttribute()
        case 'changeBooleanAttribute':
            changeBooleanAttribute()
        case 'changeIntegerAttribute':
            changeIntegerAttribute()
        case 'changeBigIntegerAttribute':
            changeBigIntegerAttribute()
        break
    }
}

def handleNotification(metadata, notif){

}

//register incoming message listener
communicator.register asListener(this.&listen)

void close(){
    communicator = null
}
