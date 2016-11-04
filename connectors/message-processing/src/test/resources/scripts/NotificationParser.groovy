package scripts

import javax.management.Notification

Notification parse(Map<String, ?> headers, Object body){
    newNotification() setMessage body.toString() setUserData headers['Content-Type'] setSource this get()
}