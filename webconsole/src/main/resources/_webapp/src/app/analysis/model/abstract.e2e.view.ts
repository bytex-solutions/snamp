const cytoscape = require('cytoscape');

export abstract class E2EView {

    private static DELIMITER:string = "---";

    public static CHILD_COMPONENT:string = "childComponents";
    public static COMPONENT_MODULES:string = "componentModules";
    public static LANDSCAPE:string = "landscape";

    private _cy:any = undefined;

    public name:string;
    public preferences:{ [key: string]: any } = { };
    public id:string = "e2eview" + GUID.newGuid();

    private verticesCount:number = 0;
    private arrivalsCount:number = 0;

    // checkboxes for setting which data aspects to display
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

    // layout
    public getLayout():string {
        return this.preferences["layout"];
    }


    public setLayout(layout:string):void {
        this.preferences["layout"] = layout;
    }

    // text size
    public getTextSize():string {
        return this.preferences["textsize"];
    }

    public setTextSize(size:string):void {
        this.preferences["textsize"] = size;
    }

    // text color
    public setTextColor(color:string):void {
        this.preferences["textcolor"] = color;
    }

    public getTextColor():string {
        return this.preferences["textcolor"];
    }

    // background color
    public setBackgroundColor(color:string):void {
        this.preferences["backgroundcolor"] = color;
    }

    public getBackgroundColor():string {
        return this.preferences["backgroundcolor"];
    }

    // text outline color
    public setTextOutlineColor(color:string):void {
        this.preferences["textoutlinecolor"] = color;
    }

    public getTextOutlineColor():string {
        return this.preferences["textoutlinecolor"];
    }

    // text outline width
    public setTextOutlineWidth(width:number):void {
        this.preferences["textoutlinewidth"] = width;
    }

    public getTextOutlineWidth():number {
        return this.preferences["textoutlinewidth"];
    }

    // text weight
    public setTextWeight(weight:number):void {
        this.preferences["textweight"] = weight;
    }

    public getTextWeight():number {
        return this.preferences["textweight"];
    }

    // edge width
    public setEdgeWidth(width:number):void {
        this.preferences["edgewidth"] = width;
    }

    public getEdgeWidth():number {
        return this.preferences["edgewidth"];
    }

    // edge line color
    public setEdgeLineColor(color:string):void {
        this.preferences["edgelinecolor"] = color;
    }

    public getEdgeLineColor():string {
        return this.preferences["edgelinecolor"];
    }

    // edge arrow color
    public setEdgeArrowColor(color:string):void {
        this.preferences["edgearrowcolor"] = color;
    }

    public getEdgeArrowColor():string {
        return this.preferences["edgearrowcolor"];
    }

    // edge arrow shape
    public setEdgeArrowShape(shape:string):void {
        this.preferences["edgearrowshape"] = shape;
    }

    public getEdgeArrowShape():string {
        return this.preferences["edgearrowshape"];
    }


    public abstract toJSON():any;

    public draw(initialData:any):any {
       let _layout:string = this.getLayout();
       console.log("id element for find: ", document.getElementById(this.id));
       let cy = cytoscape({
         container: document.getElementById(this.id),
         elements: this.getData(initialData),
         zoomingEnabled: true,
         userZoomingEnabled: true,
         wheelSensitivity: 0.15,
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
                    'font-size': this.getTextSize() + 'px',
                    'text-halign': 'right',
                    'font-weight': this.getTextWeight(),
                    'color': this.getTextSize(),
                    'text-outline-width': this.getTextOutlineWidth(),
                    'background-color': this.getBackgroundColor(),
                    'text-outline-color': this.getTextOutlineColor()
                }
            },
            {
                selector: 'edge',
                style: {
                    'width': this.getEdgeWidth(),
                    'target-arrow-shape': this.getEdgeArrowShape(),
                    'line-color': this.getEdgeLineColor(),
                    'target-arrow-color': this.getEdgeArrowColor(),
                    'curve-style': 'bezier'
                }
            }
         ]
       });
       this._cy = cy;
       return cy;
    }

    private toFixed(value:any, precision:number):string {
        let power = Math.pow(10, precision || 0);
        return String(Math.round(value * power) / power);
    }

    public updateData(currentData:any):any {
        let originalData:any = currentData;
        currentData = JSON.parse(JSON.stringify(currentData).replace(/\//g, E2EView.DELIMITER));
        console.log(currentData);
        let arrivals:any[] = [];

        if (currentData["arrivals"] != undefined) {
            arrivals = currentData["arrivals"];
            if (arrivals.length != this.arrivalsCount) {
                this._cy.json({elements: this.getData(originalData)});
                return;
            }
        }

        if (currentData["vertices"] != undefined) {
            if (currentData["vertices"].length != this.verticesCount) {
                this._cy.json({elements: this.getData(originalData)});
                return;
            }
        }

        for (let key in arrivals) {
            let _node:any = this._cy.$("#" + key);
            _node.data('arrival', arrivals[key]);
            _node.data('dl', this.getLabelFromMetadata(_node.data('id'), _node.data('arrival')));
        }
    }

    public updateDisplayedMetadata(_md:string[]):void {
        this.setDisplayedMetadata(_md);
        let nodes = this._cy.filter('node');
        for (let i = 0; i < nodes.length; i++) {
            nodes[i].data('dl', this.getLabelFromMetadata(nodes[i].data('id'), nodes[i].data('arrival')));
        }

    }

    private getData(currentData:any):any {
        currentData = JSON.parse(JSON.stringify(currentData).replace(/\//g, E2EView.DELIMITER));
        let result:any = [];

        let vertices:any[] = [];
        let arrivals:any[] = [];
        if (currentData["vertices"] != undefined) {
            vertices = currentData["vertices"];
            this.verticesCount = vertices.length;
        }

        if (currentData["arrivals"] != undefined) {
            arrivals = currentData["arrivals"];
            this.arrivalsCount = arrivals.length;
        }

        // add all plain vertices
        for (let key in arrivals) {
            result.push({ data: { id: key, arrival: arrivals[key]}});
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

    public changeTextSize(textSize:string):void {
        this.setTextSize(textSize);
        this._cy.style().selector('node').style({'font-size': this.getTextSize() + 'px'}).update();
    }

    public changeTextColor(color:string):void {
        this.setTextColor(color);
        this._cy.style().selector('node').style({'color': this.getTextColor()}).update();
    }

    public changeBackgroundColor(color:string):void {
        this.setBackgroundColor(color);
        this._cy.style().selector('node').style({'background-color': this.getBackgroundColor()}).update();
    }

    public changeTextOutlineColor(color:string):void {
        this.setTextOutlineColor(color);
        this._cy.style().selector('node').style({'text-outline-color': this.getTextOutlineColor()}).update();
    }

    public changeTextOutlineWidth(width:number):void {
        this.setTextOutlineWidth(width);
        this._cy.style().selector('node').style({'text-outline-width': this.getTextOutlineWidth()}).update();
    }

    public changeTextWeight(weight:number):void {
        this.setTextWeight(weight);
        this._cy.style().selector('node').style({'font-weight': this.getTextWeight()}).update();
    }

    public changeEdgeWidth(width:number):void {
        this.setEdgeWidth(width);
        this._cy.style().selector('edge').style({'width': this.getEdgeWidth()}).update();
    }

    public changeEdgeLineColor(color:string):void {
        this.setEdgeLineColor(color);
        this._cy.style().selector('edge').style({'line-color': this.getEdgeLineColor()}).update();
    }

    public changeEdgeArrowColor(color:string):void {
        this.setEdgeArrowColor(color);
        this._cy.style().selector('edge').style({'target-arrow-color': this.getEdgeArrowColor()}).update();
    }

    public changeEdgeArrowShape(shape:string):void {
        this.setEdgeArrowShape(shape);
        this._cy.style().selector('edge').style({'target-arrow-shape': this.getEdgeArrowShape()}).update();
    }

    private getLabelFromMetadata(id:string, data:any):string {
        let result:string = id;
        if (result != undefined && result.indexOf(E2EView.DELIMITER) > 0) {
            result = result.split(E2EView.DELIMITER)[0] + " (module: " + result.split(E2EView.DELIMITER)[1] + ")";
        }
        let _md:string[] = this.getDisplayedMetadata();
        for (let i = 0; i < _md.length; i++) {
            if (data != undefined) {
                if (_md[i].indexOf("/") > 0) {
                    result += "\n" + _md[i].split("/")[0] + "(" + _md[i].split("/")[1] + ")" + ": "
                            + this.toFixed(data[_md[i].split("/")[0]][_md[i].split("/")[1]], 5);
                } else {
                    result += "\n" + _md[i] + ": " + this.toFixed(data[_md[i]], 5);
                }
            }
        }
        return result;
    }
}

class GUID {
    static newGuid():string {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            let r = Math.random()*16|0, v = c == 'x' ? r : (r&0x3|0x8);
            return v.toString(16);
        });
    }
}