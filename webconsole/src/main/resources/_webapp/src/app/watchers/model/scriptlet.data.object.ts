import { ColoredAttributePredicate } from './colored.predicate';
import { ConstantAttributePredicate } from './constant.attribute.predicate';
import { NumberComparatorPredicate } from './number.comparator.predicate';
import { IsInRangePredicate } from './range.comparator';
import { Guid } from './entity';

import { ColoredAttributeChecker } from './colored.checker';
import { Entity } from "../../configuration/model/model.entity";
import { AbstractPolicy } from "./policy/abstract.policy";
import { HealthStatusBasedScalingPolicy } from "./policy/health.status.based.scaling.policy";
import { AttributeBasedScalingPolicy } from "./policy/attribute.based.scaling.policy";
import * as moment from 'moment/moment';
import { OpRange } from "./policy/operational.range";

export class ScriptletDataObject extends Entity {
    public language:string;
    public script:string;
    public isURL:boolean;
    public object:ColoredAttributeChecker;
    public policyObject:AbstractPolicy;
    public id:string = Guid.newGuid();

    constructor(params:any){
        super("", params);
        this.language = "Groovy";
        this.script = "";
        this.isURL = false;
        this.object = undefined;
        this.policyObject = undefined;
    }

    public shortScript():string {
        return ((this.script.length > 60) ? this.script.substring(0, 60) + '...' : this.script);
    }

    // add "MetricBased"(AttributeBasedScalingPolicy) and
    public static fromJSON(json:string):ScriptletDataObject {
        console.log("Json from data object is: ", json);
        let instance:ScriptletDataObject = new ScriptletDataObject(json["parameters"]);
        if (json["language"] != undefined) {
            instance.language = json["language"];
        }
        if (json["script"] != undefined) {
            instance.script = json["script"];
        }
        if (json["isURL"] != undefined) {
            instance.isURL = (json["url"] == 'true');
        }
        switch(instance.language) {
            case "Groovy":
            case "JavaScript":
                instance.object = undefined;
                break;
            case "HealthStatusBased":
                instance.policyObject = new HealthStatusBasedScalingPolicy();
                let _jsonHSB:any = JSON.parse(instance.script);
                (<HealthStatusBasedScalingPolicy>instance.policyObject).level = _jsonHSB["level"];
                (<HealthStatusBasedScalingPolicy>instance.policyObject).observationTime =
                    (!isNaN(parseFloat(_jsonHSB["observationTime"])) && isFinite(_jsonHSB["observationTime"]))
                        ? _jsonHSB["observationTime"] :  moment.duration(_jsonHSB["observationTime"]).asMilliseconds();
                (<HealthStatusBasedScalingPolicy>instance.policyObject).incrementalWeight = _jsonHSB["incrementalWeight"];
                (<HealthStatusBasedScalingPolicy>instance.policyObject).voteWeight = _jsonHSB["voteWeight"];
                break;
            case "MetricBased":
                let _jsonMB:any = JSON.parse(instance.script);
                instance.policyObject = new AttributeBasedScalingPolicy();
                (<AttributeBasedScalingPolicy>instance.policyObject).analysisDepth =
                    (!isNaN(parseFloat(_jsonMB["observationTime"])) && isFinite(_jsonMB["observationTime"]))
                        ? _jsonMB["observationTime"] :  moment.duration(_jsonMB["observationTime"]).asMilliseconds();
                (<AttributeBasedScalingPolicy>instance.policyObject).incrementalWeight = _jsonMB["incrementalWeight"];
                (<AttributeBasedScalingPolicy>instance.policyObject).voteWeight = _jsonMB["voteWeight"];

                (<AttributeBasedScalingPolicy>instance.policyObject).aggregation = _jsonMB["aggregation"];
                (<AttributeBasedScalingPolicy>instance.policyObject).attributeName = _jsonMB["attributeName"];
                (<AttributeBasedScalingPolicy>instance.policyObject).operationalRange = OpRange.fromString(_jsonMB["operationalRange"]);
                (<AttributeBasedScalingPolicy>instance.policyObject).analysisDepth =
                    (!isNaN(parseFloat(_jsonMB["analysisDepth"])) && isFinite(_jsonMB["analysisDepth"]))
                        ? _jsonMB["analysisDepth"] :  moment.duration(_jsonMB["analysisDepth"]).asMilliseconds();

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
        _value["url"] = this.isURL;
        _value["parameters"] = this.stringifyParameters();
        if (this.language == "ColoredAttributeChecker") {
            if (this.object == undefined) {
                throw new Error("Trying to serialize ColoredAttributeChecker instance without the object");
            } else {
                this.script = JSON.stringify(this.object.toJSON());
            }
        } else if (this.language == "HealthStatusBased" || this.language == "MetricBased") {
            if (this.object == undefined) {
                throw new Error("Trying to serialize " + this.language + " instance without the object");
            } else {
                this.script = JSON.stringify(this.policyObject.toJSON());
            }
        }
        console.log("Trying to stringify current scriptlet object: ", _value);
        return _value;
    }
}