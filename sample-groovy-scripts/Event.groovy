import com.itworks.snamp.concurrent.Repeater

println resourceName

emitter = new Repeater(300){
    void doAction(){
        emitNotification('Dummy event')
    }
}

emitter.run()

void close(){
    super.close()
    emitter.close()
}