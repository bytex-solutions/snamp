import javax.jms.Message

println "Loaded"

String toString(Message message){
    return message.readUTF()
}