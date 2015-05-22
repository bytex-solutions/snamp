communicator = getCommunicator communicationChannel

def changeStringAttribute(){
    setAttributeValue resourceName, '1.0', 'Frank Underwood'
    communicator.post getAttributeValue(resourceName, '1.0')
}

def listen(message){
    switch(message){
        case 'changeStringAttribute':
            changeStringAttribute()
        break
    }
}

//register incoming message listener
communicator.register asListener(this.&listen)

void close(){
    communicator = null
}
