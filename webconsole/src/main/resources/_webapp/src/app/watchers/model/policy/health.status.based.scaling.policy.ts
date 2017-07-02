import { AbstractWeightedScalingPolicy } from "./abstract.weighted.scaling.policy";
import * as moment from 'moment/moment';

export class HealthStatusBasedScalingPolicy extends AbstractWeightedScalingPolicy {

    private _level:string;

    public static getStatuses():string[] {
        return ["LOW", "MODERATE", "SUBSTANTIAL", "SEVERE", "CRITICAL"];
    }

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
        _value["observationTime"] = moment.duration({ milliseconds: this.observationTime}).toISOString();

        _value["level"] = this.level;
        return _value;
    }

    public getPoliticType():string {
        return "Health status based scaling policy";
    }


}