import { E2EView } from './abstract.e2e.view';

const cytoscape = require('cytoscape');

export class LandscapeView extends E2EView {
    public type:string = E2EView.LANDSCAPE;

    public draw():void {
       var cy = cytoscape({
         container: document.getElementById('cy'),
         elements: this.getData(),
         style: [
            {
                selector: 'node',
                style: {
                    'content': 'data(id)',
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
                    'width': 4,
                    'target-arrow-shape': 'triangle',
                    'line-color': '#9dbaea',
                    'target-arrow-color': '#9dbaea',
                    'curve-style': 'bezier'
                }
            }
         ]
       });

       cy.layout({
           name: 'dagre'
       });
    }

    private getData():any {

        return [
          { data: { id: 'a' } },
          { data: { id: 'b' } },
          { data: { id: 'c' } },
          { data: { id: 'd' } },
          { data: { id: 'e' } },
          { data: { id: 'f' } },
          {
            data: {
              id: 'ab',
              source: 'a',
              target: 'b'
            }
          },
          {
            data: {
              id: 'cd',
              source: 'c',
              target: 'd'
            }
          },
          {
            data: {
              id: 'ef',
              source: 'e',
              target: 'f'
            }
          },
          {
            data: {
              id: 'ac',
              source: 'a',
              target: 'd'
            }
          },
          {
            data: {
              id: 'be',
              source: 'b',
              target: 'e'
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