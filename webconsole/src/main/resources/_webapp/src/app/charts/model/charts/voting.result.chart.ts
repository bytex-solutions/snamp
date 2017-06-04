import { AbstractChart } from "../abstract.chart";
import { NumericAxis } from "../axis/numeric.axis";
import { Axis } from "../axis/abstract.axis";
import { VotingData } from "../data/voting.data";

export class VotingResultChart extends AbstractChart {

    get type(): string {
        return AbstractChart.VOTING;
    }

    private axisX:Axis;
    public group:string;

    draw(): void {
        console.log("render new voting chart...");
    }

    newValues(_data: VotingData[]) {
        console.log("New voting result data is: ", _data);
    }

    public getAxisX():Axis {
        if(this.axisX == null){
            this.axisX = new NumericAxis();
            (<NumericAxis>this.axisX).unitOfMeasurement = "weight";
            this.axisX.name = "votesForScaling";
        }
        return this.axisX;
    }

    public setAxisX(axis:Axis):void {
        this.axisX = axis;
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["group"] = this.group;
        _value["X"] = this.getAxisX().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}