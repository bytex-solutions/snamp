import { AbstractWeightedScalingPolicy } from "./abstract.weighted.scaling.policy";
import { SnampUtils } from "../../../services/app.utils";

export class HealthStatusBasedScalingPolicy extends AbstractWeightedScalingPolicy {

    private _level:string;

    constructor() {
        super();
        this.level = "CRITICAL";
    }

    get level(): string {
        return this._level;
    }

    set level(value: string) {
        this._level = value;
    }

    toJSON(): any {
        let _value:any = {};
        _value["voteWeight"] = this.voteWeight;
        _value["incrementalWeight"] = this.incrementalWeight;
        _value["observationTime"] = SnampUtils.toDurationString(this.observationTime);

        _value["level"] = this.level;
        return _value;
    }

    public getPoliticType():string {
        return "Health status based scaling policy";
    }
}