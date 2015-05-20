import groovy.json.JsonSlurper

type INT32

slurper = new JsonSlurper()

def getValue(){
    slurper.parseText('56')
}