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
                selector: 'node',
                style: {
                    'content': 'data(title)',
                    'text-opacity': 0.8,
                    'text-valign': 'top',
                    'font-size': '14px',
                    'text-halign': 'center',
                    'font-weight': '700',
                    'background-color': '#11479e'
                }
            },
            {
                selector: 'edge',
                style: {
                    'label': 'data(title)',
                    'width': 4,
                    'target-arrow-shape': 'triangle',
                    'line-color': '#006400',
                    'target-arrow-color': '#9dbaea',
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

    private getData(currentData):any {
        return [
          { data: { id: 'a', title: 'Mobile app' } },
          { data: { id: 'b', title: 'Web-server' } },
          { data: { id: 'c', title: 'Dispatcher' } },
          { data: { id: 'd', title: 'Payment system' } },
          {
            data: {
              id: 'ab',
              title:  12,
              source: 'a',
              target: 'b'
            }
          },
          {
            data: {
              id: 'cd',
              title: 14,
              source: 'c',
              target: 'd'
            }
          },
          {
            data: {
              id: 'ac',
              title: 15,
              source: 'a',
              target: 'c'
            }
          },
          {
            data: {
              id: 'ad',
              title: 18,
              source: 'a',
              target: 'd'
            }
          }
        ];
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