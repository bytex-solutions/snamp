export abstract class ColoredAttributePredicate {
    public static CONSTANT:string = "constant";
    public static COMPARATOR:string = "comparator";
    public static RANGE:string = "isInRange";

    public abstract toJSON():any;
    public abstract represent():string;
}