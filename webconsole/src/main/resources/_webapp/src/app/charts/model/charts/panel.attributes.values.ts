import { TwoDimensionalChartOfAttributeValues } from '../abstract.2d.chart.attributes.values';
import { ResourceNameAxis } from '../axis/resource.name.axis';
import { AttributeValueAxis } from '../axis/attribute.value.axis';
import { AbstractChart } from '../abstract.chart';
import { AttributeChartData } from "../data/attribute.chart.data";
import { isNullOrUndefined } from "util";

export class PanelOfAttributeValues extends TwoDimensionalChartOfAttributeValues {
    get type():string {
        return AbstractChart.PANEL;
    }

    public createDefaultAxisX() {
        return new ResourceNameAxis();
    }

    public createDefaultAxisY() {
        return new AttributeValueAxis();
    }

    constructor() {
        super();
        this.setSizeX(10);
        this.setSizeY(5);
    }

    public newValues(data:AttributeChartData[]):void {
        if (document.hidden || isNullOrUndefined(data)) return;
        let _table = $("#" + this.id + " table");
        if (!isNullOrUndefined(_table) && _table.length > 0) {
            for (let i = 0; i < data.length; i++) {
                let _data = data[i];
                let _index: number = -1;
                for (let j = 0; j < this.chartData.length; j++) {
                    if ((<AttributeChartData>this.chartData[j]).resourceName == _data.resourceName) {
                        _index = j; // remember the index
                        this.chartData[j] = _data; // change the data
                        break;
                    }
                }
                if (_index < 0) {
                    this.chartData.push(_data); // if no data with this instance is found - append it to array
                    let _tr = $("<tr/>");
                    _tr.append("<td>" + _data.resourceName + "</td>");
                    _tr.append("<td instance-binding='" + _data.resourceName + "'>" + _data.attributeValue + "</td>");
                    _table.append(_tr);
                } else {
                    _table.find("[instance-binding='" + _data.resourceName + "']").html(_data.attributeValue);
                }
            }
        } else {
            this.draw();
        }
    }

    public draw():void {
        let _table = $("<table class='table child-table'/>");
        let _thead = $("<thead></thead>");
        let _trThead = $("<tr/>");
        _trThead.append("<th>Instance</th>");
        _trThead.append("<th>" + (<AttributeValueAxis>this.getAxisY()).getLabelRepresentation()  +"</th>");
        _thead.append(_trThead);
        _table.append(_thead);
        for (let i = 0; i < this.chartData.length; i++) {
            let _tr = $("<tr/>");
            _tr.append("<td>" + (<AttributeChartData>this.chartData[i]).resourceName + "</td>");
            _tr.append("<td instance-binding='"+ (<AttributeChartData>this.chartData[i]).resourceName + "'>" + (<AttributeChartData>this.chartData[i]).attributeValue + "</td>");
            _table.append(_tr);
        }
        $("#" + this.id).append(_table);
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["group"] = this.group;
        _value["resources"] = this.resources;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}