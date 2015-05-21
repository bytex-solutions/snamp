println resourceName

def action = { emitNotification('Dummy event') }

job = schedule action, 300

void close(){
    super.close()
    job.close()
}