type TABLE('GroovyTable', 'desc', [column1: [type: INT32, description: 'descr', indexed: true], column2: [type: BOOL, description: 'descr']])

def communicator = getCommunicator 'test-session'
communicator.register(asListener { msg -> println msg })
communicator.post 'Hello from communicator'

def getValue(){
    asTable([[column1: 6, column2: false], [column1: 7, column2: true]])
}

def setValue(value){
    asTable(value)
}