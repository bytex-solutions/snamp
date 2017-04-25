import { TwoDimensionalChartOfAttributeValues } from '../abstract.2d.chart.attributes.values';
import { InstanceNameAxis } from '../instance.axis';
import { AttributeValueAxis } from '../attribute.value.axis';
import { AbstractChart } from '../abstract.chart';
import { ChartData } from '../chart.data';

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
        this.setSizeX(10);
        this.setSizeY(10);
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
        if (_index < 0) {
            this.chartData.push(_data); // if no data with this instance is found - append it to array
        }
        let _table = $("#" + this.id + " table");
        console.log("Panel object update: ", _data, _table);
        if (_table != undefined) {
            if (_index < 0) {
                let _tr = $("<tr/>");
                _tr.append("<td>" + _data.instanceName + "</td>");
                _tr.append("<td instance-binding='" + _data.instanceName + "'>" + _data.attributeValue + "</td>");
                _table.append(_tr);
            } else {
                _table.find("[instance-binding='" + _data.instanceName + "']").html(_data.attributeValue);
            }
        }
    }

    protected doDraw():void {
        let _table = $("<table class='table child-table'/>");
        let _thead = $("<thead></thead>");
        let _trThead = $("<tr/>");
        _trThead.append("<th>Instance</th>");
        _trThead.append("<th>" + (<AttributeValueAxis>this.getAxisY()).getLabelRepresentation()  +"</th>");
        _thead.append(_trThead);
        _table.append(_thead);
        for (let i = 0; i < this.chartData.length; i++) {
            let _tr = $("<tr/>");
            _tr.append("<td>" + this.chartData[i].instanceName + "</td>");
            _tr.append("<td instance-binding='"+ this.chartData[i].instanceName + "'>" + this.chartData[i].attributeValue + "</td>");
            _table.append(_tr);
        }
        $("#" + this.id).append(_table);
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