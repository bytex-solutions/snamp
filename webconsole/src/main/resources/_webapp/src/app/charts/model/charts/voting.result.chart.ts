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
        this.setSizeX(15);
        this.setSizeY(15);
    }

    draw(): void {
        if (this.chartData == undefined || this.chartData.length == 0) return;
        let _chartId:string = "#" + this.id;
        this._chart = c3.generate({
            bindto: _chartId,
            data: {
                columns: [
                    ['data', 91.4]
                ],
                type: 'gauge',
                onclick: function (d, i) { console.log("onclick", d, i); },
                onmouseover: function (d, i) { console.log("onmouseover", d, i); },
                onmouseout: function (d, i) { console.log("onmouseout", d, i); }
            },
            gauge: {
//        label: {
//            format: function(value, ratio) {
//                return value;
//            },
//            show: false // to turn off the min/max labels.
//        },
    min: 0, // 0 is default, //can handle negative min e.g. vacuum / voltage / current flow / rate of change
    max: 100, // 100 is default
//    units: ' %',
//    width: 39 // for adjusting arc thickness
            },
            color: {
                pattern: ['#FF0000', '#F97600', '#F6C600', '#60B044'], // the three color levels for the percentage values.
                threshold: {
//            unit: 'value', // percentage is default
//            max: 200, // 100 is default
                    values: [30, 60, 90, 100]
                }
            },
            size: {
                height: 180
            }
        });
    }

    newValues(_data: VotingData[]) {
        console.log("New voting result data is: ", _data);
        this.chartData = _data;
        if (this._chart != undefined && !document.hidden) {
            this._chart.load({
                columns: [['data', _data[0]]]
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