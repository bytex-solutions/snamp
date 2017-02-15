import { E2EView } from './abstract.e2e.view';

const cytoscape = require('cytoscape');

export class LandscapeView extends E2EView {
    public type:string = E2EView.LANDSCAPE;
    private _cy:any = undefined;

    public draw(initialData:any):any {
       var cy = cytoscape({
         container: document.getElementById('cy'),
         elements: this.getData(initialData),
         layout: {
             name: 'circle'
           },
         style: [
            {
               "selector":"core",
               "style":{
                  "selection-box-color":"#AAD8FF",
                  "selection-box-border-color":"#8BB0D0",
                  "selection-box-opacity":"0.5"
               }
            },
            {
               "selector":"node:selected",
               "style":{
                  "border-width":"6px",
                  "border-color":"#AAD8FF",
                  "border-opacity":"0.5",
                  "background-color":"#77828C",
                  "text-outline-color":"#77828C"
               }
            },
            {
                selector: 'node',
                style: {
                    'content': 'data(id)',
                    'text-opacity': 0.9,
                    'text-valign': 'center',
                    'font-size': '20px',
                    'text-halign': 'center',
                    'font-weight': '700',
                    'color': 'white',
                    'text-outline-width': 2,
                    'background-color': '#999',
                    'text-outline-color': '#999'
                }
            },
            {
                selector: 'edge',
                style: {
                    'width': 2  ,
                    'target-arrow-shape': 'triangle',
                    'line-color': '#999',
                    'target-arrow-color': '#999',
                    'curve-style': 'bezier'
                }
            }
         ]
       });

       this._cy = cy;
       return cy;
    }

    public updateData(currentData:any):any {
        console.log(currentData);
    }

    private getData(currentData:any):any {
        let result:any = [];

        let vertices:any[] = [];
        let arrivals:any[] = [];
        if (currentData["vertices"] != undefined) {
            vertices = currentData["vertices"];
        }

        if (currentData["arrivals"] != undefined) {
            arrivals = currentData["arrivals"];
        }

        for (let key in vertices) {
            let _node:any = { data: { id: key}};
            if (arrivals[key] != undefined) {
                _node.data.arrival = arrivals[key];
            }
            result.push(_node);

            for (let i = 0; i < vertices[key].length; i++) {
                result.push({
                    data: {
                        id: key + "2" + vertices[key][i],
                        source: key,
                        target: vertices[key][i]
                     }
                });
            }
        }

        return result;
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}