import { TwoDimensionalChart } from "../two.dimensional.chart";
import { Axis } from "../axis/abstract.axis";
import { ResourceNameAxis } from "../axis/resource.name.axis";
import { HealthStatusAxis } from "../axis/health.status.axis";
import { AbstractChart } from "../abstract.chart";
import { ChartData } from "../data/abstract.data";
import {HealthStatusChartData} from "../data/health.status.chart.data";

const d3 = require('d3');
const nv = require('nvd3');

export class ResourceGroupHealthStatusChart extends TwoDimensionalChart {
    get type():string {
        return AbstractChart.HEALTH_STATUS;
    }

    public group:string;

    private _chartObject:any = undefined;

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

    private prepareDatasets():any {
        let _value:any[] = [];

        return _value;
    }

    public draw(): void {
        // https://nvd3-community.github.io/nvd3/ - see forceDirected
        // https://github.com/nvd3-community/nvd3/blob/gh-pages/examples/forceDirected.html
        let _thisReference = this;
        nv.addGraph({
            generate: function() {
                let width = nv.utils.windowSize().width - 40,
                    height = nv.utils.windowSize().height - 40;
                let chart = nv.models.forceDirectedGraph()
                    .width(width)
                    .height(height)
                    .margin({top: 20, right: 20, bottom: 20, left: 20})
                    .color(function(d) { return d.color })
                    .nodeExtras(function(node) {
                        node
                            .append("text")
                            .attr("dx", 12)
                            .attr("dy", ".35em")
                            .text(function(d) { return d.name });
                    });
                chart.dispatch.on('renderEnd', function(){
                    console.log('render complete');
                });
                d3.select('#' + _thisReference.id)
                    .attr('width', width)
                    .attr('height', height)
                    .datum(_thisReference.prepareDatasets())
                    .call(chart);
                _thisReference._chartObject = chart;
                return chart;
            },
            callback: function(graph) {
                window.onresize = function() {
                    let width = nv.utils.windowSize().width - 40,
                        height = nv.utils.windowSize().height - 40,
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

    public newValue(_data:HealthStatusChartData):void {
        console.log("New data has been received for ResourceGroupHealthStatusChart entity: ", _data);
        this.chartData.push(_data);
        let _index:number = this.chartData.length - 1;
        if (this._chartObject != undefined) {
            let _ds:any[] = d3.select('#' + this.id).datum();
            let _found:boolean = false;

            if (!_found) {
                _ds = this.prepareDatasets();
            }
            this._chartObject.update();
        }
    }
}