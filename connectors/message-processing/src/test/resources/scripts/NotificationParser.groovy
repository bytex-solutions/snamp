package scripts

import javax.management.Notification

Notification parse(Map<String, ?> headers, Object body){
    newNotification() setMessage "Hello" setUserData 56 setSource this get()
}