import { AbstractWeightedScalingPolicy } from "./abstract.weighted.scaling.policy";
import { OpRange } from "./operational.range";
import { SnampUtils } from "../../../services/app.utils";

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
        this.aggregation = "MAX";
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

    formatAnalysisDepth():string {
        return SnampUtils.toHumanizedDuration(this.analysisDepth);
    }

    toJSON(): any {
        let _value:any = {};
        _value["voteWeight"] = this.voteWeight;
        _value["incrementalWeight"] = this.incrementalWeight;
        _value["observationTime"] = SnampUtils.toDurationString(this.observationTime);

        _value["attributeName"] = this.attributeName;
        _value["operationalRange"] = this.operationalRange.toString();
        _value["analysisDepth"] = SnampUtils.toDurationString(this.analysisDepth);
        _value["aggregation"] = this.aggregation;
        return _value;
    }

    public getPoliticType():string {
        return "Attribute based scaling policy";
    }

}