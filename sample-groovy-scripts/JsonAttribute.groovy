import groovy.json.JsonSlurper

type INT32

slurper = new JsonSlurper()

def getValue(){
    return slurper.parseText('56')
}