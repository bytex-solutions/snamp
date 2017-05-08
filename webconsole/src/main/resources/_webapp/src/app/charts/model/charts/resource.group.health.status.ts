import { TwoDimensionalChart } from "../two.dimensional.chart";
import { Axis } from "../axis/abstract.axis";
import { ResourceNameAxis } from "../axis/resource.name.axis";
import { HealthStatusAxis } from "../axis/health.status.axis";
import { AbstractChart } from "../abstract.chart";
import { HealthStatusChartData } from "../data/health.status.chart.data";

import 'jstree';

export class ResourceGroupHealthStatusChart extends TwoDimensionalChart {
    get type():string {
        return AbstractChart.HEALTH_STATUS;
    }

    public group:string;
    private _chartObject:any = undefined;
    private _state:any = undefined;

    createDefaultAxisX(): Axis {
        return new ResourceNameAxis();
    }

    createDefaultAxisY(): Axis {
        return new HealthStatusAxis();
    }

    constructor() {
        super();
        this.setSizeX(10);
        this.setSizeY(10);
    }


    toJSON(): any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["group"] = this.group;
        _value["X"] = this.getAxisX().toJSON();
        _value["Y"] = this.getAxisY().toJSON();
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }

    private strnormallize(input:string):string {
        return input.replace(/[&\/\\#,+()$~%.'":*?<>{}]/g,'_');
    }

    public draw(): void {
        let _thisReference:any = this;
        this._chartObject = $("#" + this.id).jstree({
            "core" : {
                'data' : _thisReference.prepareDatasets(),
                "check_callback" : true
            },
            "types" : {
                "#" : {
                    "max_children" : 1,
                    "max_depth" : 5,
                    "valid_children" : ["summary"]
                },
                "summary" : {
                    "icon" : "glyphicon glyphicon-certificate",
                    "valid_children" : ["instance"]
                },
                "instance" : {
                    "icon" : "glyphicon glyphicon-leaf",
                    "valid_children" : ["healthStatus", "timestamp"]
                },
                "healthStatus" : {
                    "icon" : "glyphicon glyphicon-list-alt",
                    "valid_children" : ["additional"]
                },
                "timestamp" : {
                    "icon" : "glyphicon glyphicon-time",
                    "valid_children" : []
                },
                "additional": {
                    "icon": false,
                    "valid_children" : []
                }
            },
            "plugins" : [ "state", "types" ]
        });
    }

    private fillStatusOfNode(...nodes:any[]):void {
        nodes.forEach(node => {
            if (this._state != undefined && this._state["core"] != undefined && node["id"] != undefined) {
                node["state"]["opened"] =  this._state["core"]["open"] != undefined && this._state["core"]["open"].indexOf(node["id"] >= 0);
                node["state"]["selected"] =  this._state["core"]["selected"] != undefined && this._state["core"]["selected"].indexOf(node["id"] >= 0);
            }
        });
    }

    private prepareDatasets():any {
        let _value:any = [];

        for (let i = 0; i < this.chartData.length; i++) {
            if ((<HealthStatusChartData>this.chartData[i]).summary) {
                let _rootNode:any = {
                    "id" : this.strnormallize((<HealthStatusChartData>this.chartData[i]).name),
                    "text": (<HealthStatusChartData>this.chartData[i]).name,
                    "children": [],
                    "type": "summary",
                    "a_attr": { 'class' : "level-" + (<HealthStatusChartData>this.chartData[i]).status.getNotificationLevel()}
                };
                this.fillStatusOfNode(_rootNode);
                for (let j = 0; j < this.chartData.length; j++) {
                    if (!(<HealthStatusChartData>this.chartData[j]).summary) {
                        let _tmp:HealthStatusChartData = <HealthStatusChartData>this.chartData[j];
                        let _childNode:any = {
                            "id": this.strnormallize(_tmp.name),
                            "text": _tmp.name,
                            "children": [],
                            "type": "instance",
                            "a_attr": { 'class': "level-" + _tmp.status.getNotificationLevel()}
                        };

                        let _healthStatus:any = {
                            "id": this.strnormallize(_tmp.name) + "_hs",
                            "text": _tmp.status.innerType,
                            "children": [],
                            "type": "healthStatus",
                            "a_attr": { 'class': "level-" + _tmp.status.getNotificationLevel()}
                        };

                        if (_tmp.status.serverDetails != undefined && _tmp.status.serverDetails.length > 0) {
                            _healthStatus["children"].push({
                                "text": "Details: " + _tmp.status.serverDetails,
                                "type": "additional"
                            });
                        }

                        if (_tmp.status.serverTimestamp != undefined && _tmp.status.serverTimestamp.length > 0) {
                            _healthStatus["children"].push({
                                "text": "Server time: " + _tmp.status.serverTimestamp,
                                "type": "additional"
                            });
                        }

                        _healthStatus["children"].push({
                            "text": "Level: " + _tmp.status.getNotificationLevel(),
                            "type": "additional"
                        });

                        _healthStatus["children"].push({
                            "text": "Details: " + _tmp.status.htmlDetails(),
                            "type": "additional"
                        });

                        let _timeStamp:any = {
                            "id": this.strnormallize(_tmp.name) + "_ts",
                            "text": _tmp.timestamp,
                            "children": [],
                            "type": "timestamp"
                        };

                        this.fillStatusOfNode(_timeStamp, _healthStatus, _childNode); // fill the state

                        _childNode["children"].push(_timeStamp);
                        _childNode["children"].push(_healthStatus);
                        _rootNode["children"].push(_childNode);
                    }
                }
                _value.push(_rootNode);
            }
        }
        return _value;
    }

    public newValue(_data:HealthStatusChartData):void {
        if (document.hidden) return;

        let _index:number = -1;
        for (let i = 0; i < this.chartData.length; i++) {
            if ((<HealthStatusChartData>this.chartData[i]).name == _data.name) {
                _index = i; // remember the index
                this.chartData[i] = _data; // change the data
                break;
            }
        }
        if (_index == -1) {
            this.chartData.push(_data); // if no data with this instance is found - append it to array
        }
        if (this._chartObject != undefined) {
            this._state = $("#" + this.id).jstree(true).get_state();
            console.log("State: ", this._state);
            $("#" + this.id).jstree(true).settings.core.data = this.prepareDatasets();
            $("#" + this.id).jstree(true).refresh(true);
        }
    }
}