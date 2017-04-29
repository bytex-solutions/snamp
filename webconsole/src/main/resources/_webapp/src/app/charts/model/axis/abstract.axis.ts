export abstract class Axis {
    // subtypes constants for types
    public static CHRONO:string = "chrono";
    public static INSTANCE:string = "resource";
    public static ATTRIBUTES:string = "attributeValue";

    constructor(){};

    public name:string = "";

    public abstract toJSON():any;
}