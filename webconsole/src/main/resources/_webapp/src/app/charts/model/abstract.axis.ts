export abstract class Axis {
    // subtypes constants for types
    public static const CHRONO:string = "chrono";
    public static const INSTANCE:string = "instance";
    public static const ATTRIBUTES:string = "attributeValue";

    public name:string = "";

    public abstract toJSON():any;
}