import { MalfunctionStatus } from './malfunction.status';

export class InvalidAttributeValue extends MalfunctionStatus {
    public attribute:AttributeWithValue = new AttributeWithValue();

    public represent():string {
        return "Invalid attribute (" + this.attribute.name + ")  value: " + this.attribute.value;
    }

    public getShortDescription():string {
        return "Invalid attribute " + this.attribute.name;
    }

    public htmlDetails():string {
        let _details:string = "";
         _details += "<strong>Name: </strong>" + this.name + "<br/>";
         _details += "<strong>Level: </strong>" + this.level + "<br/>";
         _details += "<strong>Attribute name: </strong>" + this.attribute.name + "<br/>";
         _details += "<strong>Attribute value: </strong>" + this.attribute.value + "<br/>";
         if (this.serverTimestamp.length > 0) {
             _details += "<strong>Server timestamp: </strong>" + this.serverTimestamp + "<br/>";
         }
         if (this.serverDetails.length > 0) {
             _details += "<strong>Details: </strong>" + this.serverDetails + "<br/>";
         }
         return _details;
    }
}

export class AttributeWithValue {
    public name:string = "";
    public value:any = undefined;
}