import { Axis } from './axis/abstract.axis';
import { AbstractChart } from './abstract.chart';

import { ChronoAxis } from './axis/chrono.axis';
import { ResourceNameAxis } from './axis/resource.name.axis';
import { AttributeValueAxis } from './axis/attribute.value.axis';
import { AttributeInformation } from './attribute';

import { TwoDimensionalChartOfAttributeValues } from './abstract.2d.chart.attributes.values';
import { ChartOfAttributeValues } from './abstract.chart.attributes.values';

import { VerticalBarChartOfAttributeValues } from './charts/vbar.chart.attributes.values';
import { HorizontalBarChartOfAttributeValues } from './charts/hbar.chart.attributes.values';
import { LineChartOfAttributeValues } from './charts/line.chart.attributes.values';
import { PanelOfAttributeValues } from './charts/panel.attributes.values';
import { PieChartOfAttributeValues } from './charts/pie.chart.attributes.values';
import { TwoDimensionalChart } from "./two.dimensional.chart";
import {ResourceGroupHealthStatusChart} from "./charts/resource.group.health.status";
import {HealthStatusAxis} from "./axis/health.status.axis";

// Factory to create appropriate objects from json
export class Factory {

    // method for creating axis
    public static axisFromJSON(_json:any):Axis {
        let _type:string = _json["@type"];
        if (_type == undefined || _type.length == 0) {
            throw new Error("Type is not set for axis");
        } else {
            let _axis:Axis;
            switch(_type) {
                case Axis.CHRONO:
                    _axis = new ChronoAxis();
                    break;
                case Axis.RESOURCE:
                    _axis = new ResourceNameAxis();
                    break;
                case Axis.ATTRIBUTES:
                    _axis = new AttributeValueAxis();
                    if (_json["sourceAttribute"] != undefined) {
                        (<AttributeValueAxis>_axis).sourceAttribute = new AttributeInformation(_json["sourceAttribute"]);
                    }
                    break;
                case Axis.HEALTH_STATUS:
                    _axis = new HealthStatusAxis();
                    break;
                default:
                    throw new Error("Type " + _type + " is unknown and cannot be parsed correctly");
            }
             if (_json["name"] != undefined) {
                _axis.name = _json["name"];
             }
            return _axis;
        }
    }

    // method for creating charts
    public static chartFromJSON(_json:any):AbstractChart {
        let _type:string = _json["@type"];
        if (_type == undefined || _type.length == 0) {
            throw new Error("Type is not set for chart");
        } else {
            let _chart:AbstractChart;
            switch(_type) {
                case AbstractChart.VBAR:
                    _chart = new VerticalBarChartOfAttributeValues();
                    break;
                case AbstractChart.HBAR:
                    _chart = new HorizontalBarChartOfAttributeValues();
                    break;
                case AbstractChart.LINE:
                    _chart = new LineChartOfAttributeValues();
                    break;
                case AbstractChart.PANEL:
                    _chart = new PanelOfAttributeValues();
                    break;
                case AbstractChart.PIE:
                    _chart = new PieChartOfAttributeValues();
                    break;
                case AbstractChart.HEALTH_STATUS:
                    _chart = new ResourceGroupHealthStatusChart();
                    break;
                default:
                    throw new Error("Type " + _type + " is unknown and cannot be parsed correctly");
            }

            if (_chart instanceof ChartOfAttributeValues) {
                if (_json["group"] != undefined) { // ChartOfAttributeValues
                    (<ChartOfAttributeValues>_chart).group = _json["group"];
                }
                if (_json["resources"] != undefined) { // ChartOfAttributeValues
                    (<ChartOfAttributeValues>_chart).resources = _json["resources"];
                }
            }

            if (_chart instanceof TwoDimensionalChart) {
                if (_json["X"] != undefined) { // TwoDimensionalChartOfAttributeValues
                    (<TwoDimensionalChart>_chart).setAxisX(Factory.axisFromJSON(_json["X"]));
                }
                if (_json["Y"] != undefined) { // TwoDimensionalChartOfAttributeValues
                    (<TwoDimensionalChart>_chart).setAxisY(Factory.axisFromJSON(_json["Y"]));
                }
                (<TwoDimensionalChart>_chart).getAxisX(); // set not null axis. just secure it.
                (<TwoDimensionalChart>_chart).getAxisY(); // set not null axis. just secure it.
            }
            if (_json["name"] != undefined) {
                _chart.name = _json["name"];
            }
            if (_json["preferences"] != undefined) {
                _chart.preferences = _json["preferences"];
            }
            console.log("New chart has been instantiated from the json data object: ", _chart);
            return _chart;
        }
    }

    public static create2dChart(type:string, name:string, groupName:string, component?:string, instances?:string[],
        sourceAttribute?:AttributeInformation):AbstractChart {
            let _chart:AbstractChart;
            type = AbstractChart.TYPE_MAPPING[type];
            switch(type) {
                case AbstractChart.VBAR:
                    _chart = new VerticalBarChartOfAttributeValues();
                    break;
                case AbstractChart.HBAR:
                    _chart = new HorizontalBarChartOfAttributeValues();
                    break;
                case AbstractChart.LINE:
                    _chart = new LineChartOfAttributeValues();
                    break;
                case AbstractChart.PANEL:
                    _chart = new PanelOfAttributeValues();
                    break;
                case AbstractChart.PIE:
                    _chart = new PieChartOfAttributeValues();
                    break;
                case AbstractChart.HEALTH_STATUS:
                    _chart = new ResourceGroupHealthStatusChart();
                    (<ResourceGroupHealthStatusChart>_chart).group = component;
                    break;
                default:
                    throw new Error("Type " + type + " is unknown and cannot be parsed correctly");
            }

            _chart.name = name;
            _chart.setGroupName(groupName);

            if (_chart instanceof TwoDimensionalChart) {
                _chart.getAxisX();
                _chart.getAxisY();
            }

            if (_chart instanceof TwoDimensionalChartOfAttributeValues) {
                if (component) {
                    _chart.group = component;
                }

                if (instances) {
                    _chart.resources = instances;
                }

                if (sourceAttribute) {
                    _chart.setSourceAttribute(sourceAttribute);
                }
            }

            return _chart;

    }
}