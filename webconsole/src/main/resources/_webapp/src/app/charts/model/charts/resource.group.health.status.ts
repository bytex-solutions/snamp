import { TwoDimensionalChart } from "../two.dimensional.chart";
import { Axis } from "../axis/abstract.axis";
import { ResourceNameAxis } from "../axis/resource.name.axis";
import { HealthStatusAxis } from "../axis/health.status.axis";
import { AbstractChart } from "../abstract.chart";
import { HealthStatusChartData } from "../data/health.status.chart.data";

const d3 = require('d3');
const nv = require('nvd3');

export class ResourceGroupHealthStatusChart extends TwoDimensionalChart {
    get type():string {
        return AbstractChart.HEALTH_STATUS;
    }

    public group:string;

    private _chartObject:any = undefined;
    private _datum:any = undefined;

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

    private getWidth():number {
        return document.getElementById(this.id).getBoundingClientRect().width;
    }

    private getHeight():number {
        return document.getElementById(this.id).getBoundingClientRect().height;
    }

    public draw(): void {
        // https://nvd3-community.github.io/nvd3/ - see forceDirected
        // https://github.com/nvd3-community/nvd3/blob/gh-pages/examples/forceDirected.html
        let _thisReference = this;
        nv.addGraph({
            generate: function() {
                let width = _thisReference.getWidth() - 40,
                    height = _thisReference.getHeight() - 40;
                console.log(width, height, 'd3 window calculated sizes');
                let chart = nv.models.forceDirectedGraph()
                    .width(width)
                    .height(height)
                    .linkDist(60)
                    .radius(function(d) { return d.main ? 20 : 10})
                    .margin({top: 20, right: 20, bottom: 20, left: 20})
                    .color(function(d) { return d.color })
                    .nodeExtras(function(node) {
                        node
                            .append("text")
                            .attr("dx", 12)
                            .attr("dy", ".35em")
                            .text(function(d) { return d.name });
                    });

                _thisReference._datum =  d3.select('#' + _thisReference.id)
                    .attr('width', width)
                    .attr('height', height)
                    .datum(_thisReference.prepareDatasets());

                _thisReference._datum .call(chart);
                _thisReference._chartObject = chart;
                d3.select('#' + _thisReference.id).selectAll(".line").style('stroke', 'black');
                return chart;
            },
            callback: function(graph) {
                window.onresize = function() {
                    let width = _thisReference.getWidth() - 40,
                        height = _thisReference.getHeight() - 40,
                        margin = graph.margin();
                    if (width < margin.left + margin.right + 20)
                        width = margin.left + margin.right + 20;
                    if (height < margin.top + margin.bottom + 20)
                        height = margin.top + margin.bottom + 20;

                    graph.width(width).height(height);
                    d3.select('#' + _thisReference.id)
                        .attr('width', width)
                        .attr('height', height)
                        .call(graph);
                };
            }
        });
    }

    private static statusToColor(status:string):string {
        return status == "info" ? "#099000" : "#ea0000";
    }

    private prepareDatasets():any {
        let _value:any = {
            'nodes': [],
            'links': []
        };
        for (let i = 0; i < this.chartData.length; i++) {
            let _newNode:any = {
                "name" : (<HealthStatusChartData>this.chartData[i]).name,
                "color" : ResourceGroupHealthStatusChart.statusToColor((<HealthStatusChartData>this.chartData[i]).status.getNotificationLevel()),
                "details" : (<HealthStatusChartData>this.chartData[i]).status.htmlDetails(),
                "main": (<HealthStatusChartData>this.chartData[i]).summary,
                "x": this.getWidth()/2,
                "y": this.getHeight()/2
            };
/*            if ((<HealthStatusChartData>this.chartData[i]).summary) {
                _newNode['x'] = ;
                _newNode['y'] = this.getHeight()/2;
            }*/
            _value["nodes"].push(_newNode);

        }
        for (let i = 0; i < this.chartData.length; i++) {
            if ((<HealthStatusChartData>this.chartData[i]).summary) {
                for (let j = 0; j < this.chartData.length; j++) {
                    if (i != j) {
                        _value["links"].push({
                            "source": i,
                            "target": j,
                            "value": 1
                        });
                    }
                }
            }
        }
        return _value;
    }

    public newValue(_data:HealthStatusChartData):void {
        if (document.hidden) return;
        console.log("New data has been received for ResourceGroupHealthStatusChart entity: ", _data);
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
            if (this._chartObject != undefined) {
                this._datum.datum(this.prepareDatasets()).call(this._chartObject);
            }
        } else {
            if (this._chartObject != undefined) {
                let _node:any = d3.select('#' + this.id).selectAll("g").filter(function(d) { return d.name === _data.name });
                _node.attr('color', ResourceGroupHealthStatusChart.statusToColor(_data.status.getNotificationLevel()));
                _node.attr('details', _data.status.htmlDetails());
                _node.select('circle').style("fill",  ResourceGroupHealthStatusChart.statusToColor(_data.status.getNotificationLevel()));
            }
        }
    }
}