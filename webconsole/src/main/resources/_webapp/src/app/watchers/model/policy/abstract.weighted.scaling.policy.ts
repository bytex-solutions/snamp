import { AbstractPolicy } from "./abstract.policy";
import { SnampUtils } from "../../../services/app.utils";
import {isNullOrUndefined} from "util";

export abstract class AbstractWeightedScalingPolicy extends AbstractPolicy {

    constructor() {
        super();
        this.voteWeight = 0;
        this.observationTime = 0;
        this.incrementalWeight = false;
    }
    private _voteWeight:number;
    private _observationTime:number;
    private _incrementalWeight:boolean;

    get voteWeight(): number {
        return this._voteWeight;
    }

    set voteWeight(value: number) {
        this._voteWeight = value;
    }

    get observationTime(): number {
        return this._observationTime;
    }

    set observationTime(value: number) {
        this._observationTime = value;
    }

    get incrementalWeight(): boolean {
        return this._incrementalWeight;
    }

    set incrementalWeight(value: boolean) {
        this._incrementalWeight = value;
    }

    formatObservationTime():string {
        return SnampUtils.toHumanizedDuration(this.observationTime);
    }

    public getPolicyWeight():string {
        return isNullOrUndefined(this.voteWeight) ? "N/A" : this.voteWeight.toString();
    }
}