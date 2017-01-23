import { Axis } from './abstract.axis';
import { Chart } from './abstract.chart';

// Factory to create appropriate objects from json
export class Factory {

    // method for creating axis
    public static axisFromJSON(_json:any):Axis {
        let _type:string = _json["@type"];
        if (_type == undefined || _type.length == 0) {
            throw new Error("Type is not set for axis");
        } else {
            let _axis:Axis;
            switch(_type) {
                case Axis.CHRONO:
                    _axis = new ChronoAxis();
                    if (_json["name"] != undefined) {
                        _axis.name = _json["name"];
                    }
                    break;
                case Axis.INSTANCE:
                    _axis = new InstanceNameAxis();
                    if (_json["name"] != undefined) {
                        _axis.name = _json["name"];
                    }
                    break;
                case Axis.ATTRIBUTES:
                    _axis = new AttributeValueAxis();
                    if (_json["name"] != undefined) {
                        _axis.name = _json["name"];
                    }
                    if (_json["sourceAttribute"] != undefined) {
                        _axis.sourceAttribute = new AttributeInformation(_json["sourceAttribute"]);
                    }
                    break;
                default:
                    throw new Error("Type " + _type + " is unknown and cannot be parsed correctly");
            }
            return _axis;
        }
    }

    public static chartFromJSON(_json:any):Chart {
        let _type:string = _json["@type"];
        if (_type == undefined || _type.length == 0) {
            throw new Error("Type is not set for chart");
        } else {
            let _chart:Chart;
            switch(_type) {
                default:
                    throw new Error("Type " + _type + " is unknown and cannot be parsed correctly");
            }
            return _chart;
        }
    }
}