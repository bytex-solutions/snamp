export abstract class HealthStatus {

    public static OK_TYPE:string = "OK";
    public static RESOURCE_NA_TYPE:string = "ResourceIsNotAvailable";
    public static CONNECTION_PROBLEM_TYPE:string = "ConnectionProblem";
    public static ATTRIBUTE_VALUE_PROBLEM_TYPE:string = "InvalidAttributeValue";

    public code:number = -1;
    public resourceName:string = "";
    public name:string = "";

    public abstract isCritical():boolean;
    public abstract represent():string;
    public abstract getShortDescription():string;
    public abstract htmlDetails():string;

    public details():string {
        return this.represent() + " (Click for details)";
    }
}