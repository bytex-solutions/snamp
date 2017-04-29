export abstract class HealthStatus {

    public static OK_TYPE:string = "OK";
    public static RESOURCE_CONNECTOR_MALFUNCTION_TYPE:string = "ResourceConnectorMalfunction";
    public static CONNECTION_PROBLEM_TYPE:string = "ConnectionProblem";
    public static ATTRIBUTE_VALUE_PROBLEM_TYPE:string = "InvalidAttributeValue";

    // name of notification in a map
    public name:string = "";

    // inner type to hold it
    public innerType:string = HealthStatus.OK_TYPE;

    // always have it from the service serialization
    public serverDetails:string = "";

    // timestamp from the server
    public serverTimestamp:String = "";

    // partial implementation for details method (see below)
    public abstract represent():string;

    // used for red/green table short descripton
    public abstract getShortDescription():string;

    // used for displaying within details modal window
    public abstract htmlDetails():string;

    // return corresponding notification level (we have two separated systems of levels)
    public abstract getNotificationLevel():string;

    // used for short description at the notification (pnotify)
    public details():string {
        return this.represent() + " (Click for details)";
    }
}