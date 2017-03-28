import { MalfunctionStatus } from './malfunction.status';

export class InvalidAttributeValue extends MalfunctionStatus {
    public static CODE:number = 3;
    public attribute:AttributeWithValue = new AttributeWithValue();

    public represent():string {
        return "Invalid attribute (" + this.attribute.name + ")  value: " + this.attribute.value;
    }
}

export class AttributeWithValue {
    public name:string = "";
    public value:any = undefined;
}