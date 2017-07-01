import { AbstractWeightedScalingPolicy } from "./abstract.weighted.scaling.policy";
import * as moment from 'moment/moment';
import { OpRange } from "./operational.range";

export class AttributeBasedScalingPolicy extends AbstractWeightedScalingPolicy {

    private _attributeName:string;
    private _operationalRange:OpRange;
    private _aggregation:string;
    private _analysisDepth:number;

    constructor() {
        super();
        this.attributeName = "";
        this.operationalRange = new OpRange(0, 0);
        this.analysisDepth = 0;
    }

    get attributeName(): string {
        return this._attributeName;
    }

    set attributeName(value: string) {
        this._attributeName = value;
    }

    get operationalRange(): OpRange {
        return this._operationalRange;
    }

    set operationalRange(value: OpRange) {
        this._operationalRange = value;
    }

    get aggregation(): string {
        return this._aggregation;
    }

    set aggregation(value: string) {
        this._aggregation = value;
    }

    get analysisDepth(): number {
        return this._analysisDepth;
    }

    set analysisDepth(value: number) {
        this._analysisDepth = value;
    }

    toJSON(): any {
        let _value:any = {};
        _value["voteWeight"] = this.voteWeight;
        _value["incrementalWeight"] = this.incrementalWeight;
        _value["observationTime"] = moment.duration({ milliseconds: this.observationTime}).toISOString();

        _value["attributeName"] = this.attributeName;
        _value["operationalRange"] = this.operationalRange.toString();
        _value["analysisDepth"] = moment.duration({ milliseconds: this.analysisDepth}).toISOString();
        return _value;
    }

}