import { ColoredAttributePredicate } from './colored.predicate';
import { ConstantAttributePredicate } from './constant.attribute.predicate';
import { NumberComparatorPredicate } from './number.comparator.predicate';
import { IsInRangePredicate } from './range.comparator';

import { ColoredAttributeChecker } from './colored.checker';

export class ScriptletDataObject {
    public language:string;
    public script:string;
    public isURL:boolean;
    public object:ColoredAttributeChecker;

    constructor(){
        this.language = "n/a";
        this.script = "empty";
        this.isURL = false;
        this.object = undefined;
    }

    public static fromJSON(json:string):ScriptletDataObject {
        let instance:ScriptletDataObject = new ScriptletDataObject();
        if (json["language"] != undefined) {
            instance.language = json["language"];
        }
        if (json["script"] != undefined) {
            instance.script = json["script"];
        }
        if (json["isURL"] != undefined) {
            instance.isURL = (json["isURL"] == 'true');
        }
        switch(instance.language) {
            case "Groovy":
            case "JavaScript":
                instance.object = undefined;
                break;
            case "ColoredAttributeChecker":
                instance.object = new ColoredAttributeChecker();
                if (instance.script == undefined || instance.script.length < 5) {
                    return instance;
                }
                let _jsonChecker:any = JSON.parse(instance.script);
                let _yellow = _jsonChecker["yellow"];
                switch (_yellow["@type"]) {
                     case ColoredAttributePredicate.CONSTANT:
                        instance.object.yellow = new ConstantAttributePredicate();
                        (<ConstantAttributePredicate>instance.object.yellow).value = _yellow["value"];
                        break;
                     case ColoredAttributePredicate.COMPARATOR:
                         instance.object.yellow = new NumberComparatorPredicate();
                         (<NumberComparatorPredicate>instance.object.yellow).value = _yellow["value"];
                         (<NumberComparatorPredicate>instance.object.yellow).operator = _yellow["operator"];
                         break;
                     case ColoredAttributePredicate.RANGE:
                         instance.object.yellow = new IsInRangePredicate();
                         (<IsInRangePredicate>instance.object.yellow).rangeStart = _yellow["rangeStart"];
                         (<IsInRangePredicate>instance.object.yellow).rangeEnd = _yellow["rangeEnd"];
                         (<IsInRangePredicate>instance.object.yellow).isRangeEndInclusive = _yellow["isRangeEndInclusive"];
                         (<IsInRangePredicate>instance.object.yellow).isRangeStartInclusive = _yellow["isRangeStartInclusive"];
                         break;
                     default:
                        throw new Error("Could not recognize yellow checker type: " + _yellow["@type"]);
                }

                let _green = _jsonChecker["green"];
                switch (_green["@type"]) {
                     case ColoredAttributePredicate.CONSTANT:
                        instance.object.green = new ConstantAttributePredicate();
                        (<ConstantAttributePredicate>instance.object.green).value = _green["value"];
                        break;
                     case ColoredAttributePredicate.COMPARATOR:
                         instance.object.green = new NumberComparatorPredicate();
                         (<NumberComparatorPredicate>instance.object.green).value = _green["value"];
                         (<NumberComparatorPredicate>instance.object.green).operator = _green["operator"];
                         break;
                     case ColoredAttributePredicate.RANGE:
                         instance.object.green = new IsInRangePredicate();
                         (<IsInRangePredicate>instance.object.green).rangeStart = _green["rangeStart"];
                         (<IsInRangePredicate>instance.object.green).rangeEnd = _green["rangeEnd"];
                         (<IsInRangePredicate>instance.object.green).isRangeEndInclusive = _green["isRangeEndInclusive"];
                         (<IsInRangePredicate>instance.object.green).isRangeStartInclusive = _green["isRangeStartInclusive"];
                         break;
                     default:
                        throw new Error("Could not recognize green checker type: " + _green["@type"]);
                }
                break;
            default:
                throw new Error("Cannot recognize language type: " + instance.language);
        }
        return instance;
    }

    public toJSON():any {
        let _value:any = {};
        _value["language"] = this.language;
        _value["script"] = this.script;
        _value["isURL"] = this.isURL;
        return _value;
    }
}