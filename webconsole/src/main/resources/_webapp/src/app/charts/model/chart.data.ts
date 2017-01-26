export class ChartData {
    public attributeName:string;
    public attributeValue:any;
    public chartType:string;
    public instanceName:string;
    public timestamp:Date = new Date();

    public static fromJSON(_json:any):ChartData {
        let _value:ChartData = new ChartData();
        // for (var prop in obj) _value[prop] = _json[prop];
        if (_json["attributeName"] != undefined) {
            _value.attributeName = _json["attributeName"];
        }
        if (_json["attributeValue"] != undefined) {
            _value.attributeValue = _json["attributeValue"];
        }
        if (_json["chartType"] != undefined) {
            _value.chartType = _json["chartType"];
        }
        if (_json["instanceName"] != undefined) {
            _value.instanceName = _json["instanceName"];
        }
        return _value;
    }
}