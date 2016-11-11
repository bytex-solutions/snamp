package scripts

def parse(headers, body){
    create notification setMessage body.toString() setUserData headers['Content-Type'] setSource this get()
}