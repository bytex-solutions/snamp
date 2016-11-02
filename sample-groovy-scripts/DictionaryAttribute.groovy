type DICTIONARY('GroovyType', 'GroovyDescr', [key1: [type: INT64, description: 'descr'], key2: [type: BOOL, description: 'descr']])

def getValue(){
    asDictionary(key1: 67L, key2: true)
}

def setValue(value){
    println value
}
