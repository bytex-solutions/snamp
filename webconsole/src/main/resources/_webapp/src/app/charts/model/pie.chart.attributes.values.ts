import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from './instance.axis';
import { AttributeValueAxis } from './attribute.value.axis';
import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

const d3 = require('d3');
const nv = require('nvd3');

export class PieChartOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = AbstractChart.PIE;

    private _chartObject:any = undefined;
    private _svgReadyData:any = undefined;

    public createDefaultAxisX() {
        return new InstanceNameAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    constructor() {
        super();
        this.setSizeX(3);
        this.setSizeY(3);
        this._svgReadyData = this.prepareDatasets();
    }

    public newValue(_data:ChartData):void {
        let _index:number = -1;
        for (let i = 0; i < this.chartData.length; i++) {
            if (this.chartData[i].instanceName == _data.instanceName) {
                _index = i; // remember the index
                this.chartData[i] = _data; // change the data
                break;
            }
        }
        if (_index == -1) {
            this.chartData.push(_data); // if no data with this instance is found - append it to an array
            this._svgReadyData.push({ key: _data.instanceName, y: _data.attributeValue});
        } else {
            for (let i = 0; i < this._svgReadyData.length; i++) {
                if (this._svgReadyData[i].key == _data.instanceName) {
                    this._svgReadyData[i].y = _data.attributeValue;
                }
            }
        }
        if (this._chartObject != undefined) {
            this._chartObject.update();
        }
    }

    private prepareDatasets():any {
        let _value:any = [];
        for (let i = 0; i < this.chartData.length; i++) {
            _value.push({
                key: this.chartData[i].instanceName,
                y: this.chartData[i].attributeValue
            });
        }
        return _value;
    }

    public draw():void {
        if (this.updateStopped) {
            return; //do not draw if stop was pressed
        }
        // refresh data to be actual in this phase
        this._svgReadyData = this.prepareDatasets();
        let _sam:string = (<AttributeValueAxis>this.getAxisY()).getLabelRepresentation();
        let _thisReference = this;
        nv.addGraph(function() {
            let pieChart = nv.models.pieChart()
                .x(function(d) { return d.key })
                .y(function(d) { return d.y })
                .donut(true)
                .padAngle(.08)
                .cornerRadius(5)
                .showLabels(true)
                .id('donut1');

            pieChart.pie.labelType('value').title(_sam);

            d3.select("#" + _thisReference.id)
                .datum(_thisReference._svgReadyData)
                .call(pieChart);

            _thisReference._chartObject = pieChart;
            return pieChart;
        });
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["component"] = this.component;
        _value["instances"] = this.instances;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}