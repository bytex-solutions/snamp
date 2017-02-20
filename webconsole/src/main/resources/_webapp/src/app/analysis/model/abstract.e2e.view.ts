const cytoscape = require('cytoscape');

export abstract class E2EView {

    public static CHILD_COMPONENT:string = "childComponents";
    public static COMPONENT_MODULES:string = "componentModules";
    public static LANDSCAPE:string = "landscape";

    private _cy:any = undefined;

    public name:string;
    public preferences:{ [key: string]: any } = { };
    public id:string = "e2eview" + GUID.newGuid();

    public getDisplayedMetadata():string[] {
        if (this.preferences && this.preferences["displayedMetadata"] != undefined) {
            return this.preferences["displayedMetadata"];
        } else {
            return [];
        }
    }

    public setDisplayedMetadata(metadata:string[]):void {
        this.preferences["displayedMetadata"] = metadata;
    }

    public getLayout():string {
        return this.preferences["layout"];
    }


    public setLayout(layout:string):void{
        this.preferences["layout"] = layout;
    }

    public abstract toJSON():any;

    public draw(initialData:any):any {
       let _layout:string = this.getLayout();
       var cy = cytoscape({
         container: document.getElementById('cy'),
         elements: this.getData(initialData),
         zoomingEnabled: false,
         userZoomingEnabled: false,
         layout: {
             name: _layout
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
              selector: '.multiline-manual',
              style: {
                'text-wrap': 'wrap'
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
                    'content': 'data(dl)',
                    'text-opacity': 0.9,
                    'text-valign': 'top',
                    'font-size': '23px',
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
        let result:any = [];
        let arrivals:any[] = [];

        if (currentData["arrivals"] != undefined) {
            arrivals = currentData["arrivals"];
        }

        for (let key in arrivals) {
            let _node:any = this._cy.$("#" + key);
            _node.data('arrival', arrivals[key]);
            // console.log(_node, _node.data('id'), _node.data('arrival'), this.getLabelFromMetadata(_node.data('id'), _node.data('arrival')));
            _node.data('dl', this.getLabelFromMetadata(_node.data('id'), _node.data('arrival')));
        }
    }

    public updateDisplayedMetadata(_md:string[]):void {
        this.setDisplayedMetadata(_md);
        var nodes = this._cy.filter('node');
        for (let i = 0; i < nodes.length; i++) {
            console.log(this.getLabelFromMetadata(nodes[i].data('id'), nodes[i].data('arrival')));
            nodes[i].data('dl', this.getLabelFromMetadata(nodes[i].data('id'), nodes[i].data('arrival')));
        }

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

        // add all plain vertices
        for (let key in vertices) {
            let _node:any = { data: { id: key}};
            if (arrivals[key] != undefined) {
                _node.data.arrival = arrivals[key];
            }
            result.push(_node);
        }

        // add vertices without outgoing links
        for (let key in vertices) {
            for (let i = 0; i < vertices[key].length; i++) {
                // if our resulting array does not contain element - add it
                let _found:boolean = false;
                for (let j = 0; j < result.length; j++) {
                    if (result[j].id == vertices[key][i]) {
                        _found = true;
                        break;
                    }
                }
                if (!_found) {
                    result.push({
                        data: {
                            id: vertices[key][i],
                            arrival: arrivals[key]
                        }
                    })
                }
            }
        }

        // create labels according to the rules
        for (let i = 0; i < result.length; i++) {
            result[i].data.dl = this.getLabelFromMetadata(result[i].id, result[i].arrival);
            result[i].classes = "multiline-manual";
        }

        // append edges for vertices
        for (let key in vertices) {
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

    public changeLayout(layout:string):void {
        this.setLayout(layout);
        this._cy.makeLayout({ name: layout }).run();
    }

    private getLabelFromMetadata(id:string, data:any):string {
        let result:string = id;
        let _md:string[] = this.getDisplayedMetadata();
        for (let i = 0; i < _md.length; i++) {
            if (data != undefined && data[_md[i]] != undefined) {
                if (_md[i].indexOf("/") > 0) {
                    result += "\n" + _md[i].split("/")[0] + "(" + _md[i].split("/")[1] + ")" + ": "
                            + data[_md[i].split("/")[0]][_md[i].split("/")[1]];
                } else {
                    result += "\n" + _md[i] + ": " + data[_md[i]];
                }
            }
        }
        return result;
    }
}

class GUID {
    static newGuid():string {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }
}