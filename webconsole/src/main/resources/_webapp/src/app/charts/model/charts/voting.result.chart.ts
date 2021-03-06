import { AbstractChart } from "../abstract.chart";
import { NumericAxis } from "../axis/numeric.axis";
import { Axis } from "../axis/abstract.axis";
import { VotingData } from "../data/voting.data";

import { RadialGauge } from 'canvas-gauges';
import { ChartWithGroupName } from "./group.name.based.chart";
import { isNullOrUndefined } from "util";

export class VotingResultChart extends AbstractChart implements ChartWithGroupName {

    get type(): string {
        return AbstractChart.VOTING;
    }

    private axisX:Axis;
    public group:string;

    private _chart:any = undefined;

    constructor() {
        super();
        this.setSizeX(8);
        this.setSizeY(4);
    }

    draw(): void {
        if (isNullOrUndefined(this.chartData) || this.chartData.length == 0) return;
        let _voteIntervalTick:any = (<VotingData>this.chartData[0]).castingVote;
        let _currentValue:any = (<VotingData>this.chartData[0]).votingResult;

        let _parent = $("#" + this.id).parent();
        let _width = _parent.width();
        let _height = _parent.height();

        let opts = {
            height: _height,
            width: _width,
            value: _currentValue,
            valueBox: true,
            exactTicks: true,
            majorTicks: [-_voteIntervalTick*2, -_voteIntervalTick, 0, _voteIntervalTick, 2* _voteIntervalTick],
            renderTo: this.id,
            title: this.group,
            minValue: -2 * _voteIntervalTick,
            maxValue: 2 * _voteIntervalTick,
            highlights:[
                {"from": -_voteIntervalTick*2, "to": -_voteIntervalTick, "color": "#ff2211"},
                {"from": -_voteIntervalTick, "to": _voteIntervalTick, "color": "#ffffff"},
                {"from": _voteIntervalTick, "to": _voteIntervalTick*2, "color": "#00ff00"},
            ]
        };
        this._chart = new RadialGauge(opts).draw(); // create sexy gauge!
    }

    newValues(_data: VotingData[]) {
        this.chartData = _data;
        if (!isNullOrUndefined(this._chart) && !document.hidden) {
            this._chart.value = _data[0].votingResult;
        } else {
            this.draw();
        }
    }

    public resize():void {
        let _thisReference = this;
        setTimeout(() => {
            let _parent = $("#" + _thisReference.id).parent();
            let _width = _parent.width();
            let _height = _parent.height();
            _thisReference._chart.update({height: _height, width: _width,});
        }, 400);
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

    public reinitialize() {
        if (!isNullOrUndefined(this._chart)) {
            this._chart.destroy();
            this._chart = undefined;
        }
    }
}