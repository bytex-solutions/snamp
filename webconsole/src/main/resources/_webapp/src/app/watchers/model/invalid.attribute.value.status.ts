import { MalfunctionStatus } from './malfunction.status';

export class InvalidAttributeValue extends MalfunctionStatus {
    public code:number = 3;
    public attribute:AttributeWithValue = new AttributeWithValue();

    public represent():string {
        return "Invalid attribute (" + this.attribute.name + ")  value: " + this.attribute.value;
    }

    public getShortDescription():string {
        return "Invalid attribute";
    }

    public htmlDetails():string {
        let _details:string = "";
         _details += "<strong>Watcher name: </strong>" + this.name + "<br/>";
         _details += "<strong>Resource: </strong>" + this.resourceName + "<br/>";
         _details += "<strong>Critical: </strong>" + this.critical + "<br/>";
         _details += "<strong>Attribute name: </strong>" + this.attribute.name + "<br/>";
         _details += "<strong>Attribute value: </strong>" + this.attribute.value + "<br/>";
         return _details;
    }
}

export class AttributeWithValue {
    public name:string = "";
    public value:any = undefined;
}