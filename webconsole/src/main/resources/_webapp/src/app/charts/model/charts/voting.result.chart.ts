import { AbstractChart } from "../abstract.chart";
import { NumericAxis } from "../axis/numeric.axis";
import { Axis } from "../axis/abstract.axis";
import { VotingData } from "../data/voting.data";

const c3 = require('c3');

export class VotingResultChart extends AbstractChart {

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
        if (this.chartData == undefined || this.chartData.length == 0) return;
        let _chartId:string = "#" + this.id;
        let _voteIntervalTick:any = (<VotingData>this.chartData[0]).castingVote;
        let _currentValue:any = (<VotingData>this.chartData[0]).votingResult;
        this._chart = c3.generate({
            bindto: _chartId,
            data: {
                columns: [
                    ['data', _currentValue]
                ],
                type: 'gauge',
                onclick: function (d, i) { console.log("onclick", d, i); },
                onmouseover: function (d, i) { console.log("onmouseover", d, i); },
                onmouseout: function (d, i) { console.log("onmouseout", d, i); }
            },
            gauge: {
                min: _voteIntervalTick * (-2),
                max: _voteIntervalTick * 2,
//        label: {
//            format: function(value, ratio) {
//                return value;
//            },
//            show: false // to turn off the min/max labels.
//        },
//    min: 0, // 0 is default, //can handle negative min e.g. vacuum / voltage / current flow / rate of change
//    max: 100, // 100 is default
//    units: ' %',
//    width: 39 // for adjusting arc thickness
            },
            color: {
                pattern: ['#2db62d', '#ffffff', '#d9463e'], // the three color levels for the percentage values.
                threshold: {
                 unit: 'value', // percentage is default
                // max: 4*_voteIntervalTick, // 100 is default
                 values: [ -_voteIntervalTick, 0,  _voteIntervalTick]
                }
            },
        });
    }

    newValues(_data: VotingData[]) {
        console.log("New voting result data is: ", _data);
        this.chartData = _data;
        if (this._chart != undefined && !document.hidden) {
            this._chart.load({
                columns: [['data', _data[0].votingResult]]
            });
        } else {
            this.draw();
        }
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