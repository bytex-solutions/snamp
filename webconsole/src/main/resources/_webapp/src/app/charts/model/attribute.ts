export class AttributeInformation {
    public name:string = "undefined";
    public unitOfMeasurement:string = undefined;
    public type:string = undefined;
    public description:string = undefined;

    constructor(_json:any) {
        if (_json["name"] != undefined) {
            this.name = _json["name"];
        }
        if (_json["unitOfMeasurement"] != undefined) {
            this.unitOfMeasurement = _json["unitOfMeasurement"];
        }
        if (_json["type"] != undefined) {
            this.type = _json["type"];
        }
        if (_json["description"] != undefined) {
            this.description = _json["description"];
        }
    }

    public toJSON():any {
        let _value:any = {};
        _value["type"] = this.type;
        _value["name"] = this.name;
        _value["unitOfMeasurement"] = this.unitOfMeasurement;
        return _value;
    }
}