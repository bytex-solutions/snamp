import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from './instance.axis';
import { AttributeValueAxis } from './attribute.value.axis';
import { AbstractChart } from './abstract.chart';
import { ChartData } from './chart.data';

export class PanelOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    public type:string = AbstractChart.PANEL;

    public createDefaultAxisX() {
        return new InstanceNameAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    constructor() {
        super();
        this.setSizeX(2);
        this.setSizeY(4);
    }

    public newValue(_data:ChartData):void {
        if (document.hidden) return;
        let _index:number = -1;
        for (let i = 0; i < this.chartData.length; i++) {
            if (this.chartData[i].instanceName == _data.instanceName) {
                _index = i; // remember the index
                this.chartData[i] = _data; // change the data
                break;
            }
        }
        if (_index == -1) {
            this.chartData.push(_data); // if no data with this instance is found - append it to array
        }
        let _chr = $("#panel_" + this.id);
        if (_chr != undefined && !document.hidden) {
            if (_index == -1) {
                _chr.append('<dt>' + _data.instanceName + '</dt>');
                let _newDD = $('<dd>' + _data.attributeValue + '</dd>');
                _newDD.attr("id", "ddInstance" + _data.instanceName);
                _chr.append(_newDD);
            } else {
                _chr.find("#ddInstance" + _data.instanceName).html(_data.attributeValue);
            }
        }
    }

    protected doDraw():void {
        let ctx = $("#" + this.id);
        let _result = $('<dl class="border-around"></dl>');
        _result.attr("id", "panel_" + this.id);
        ctx.parent().append(_result);
        ctx.remove();
        for (let i = 0; i < this.chartData.length; i++) {
            _result.append('<dt>' + this.chartData[i].instanceName + '</dt>');
            let _newDD = $('<dd>' + this.chartData[i].attributeValue + '</dd>');
            _newDD.attr("id", "ddInstance" + this.chartData[i].instanceName);
            _result.append(_newDD);
        }
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