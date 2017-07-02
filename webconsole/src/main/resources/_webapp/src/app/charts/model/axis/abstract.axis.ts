export abstract class Axis {
    // subtypes constants for types
    public static CHRONO:string = "chrono";
    public static RESOURCE:string = "resource";
    public static ATTRIBUTES:string = "attributeValue";
    public static HEALTH_STATUS:string = "healthStatus";
    public static NUMERIC:string = "value";

    constructor(){};

    public name:string = "";

    public abstract toJSON():any;
}