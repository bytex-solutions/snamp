import { E2EView } from './abstract.e2e.view';

const cytoscape = require('cytoscape');

export class LandscapeView extends E2EView {
    public type:string = E2EView.LANDSCAPE;

    public draw():void {
       var cy = cytoscape({
         container: document.getElementById('cy'),
         elements: this.getData(),
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
       var _thisReference = this;
        setInterval(function() {
             var _d = _thisReference.getRandom();
             cy.$('#ab').data("title", _d);
             if (_d > 50) {
                cy.$('#ab').style({'line-color': '#FF6347'});
             } else {
                cy.$('#ab').style({'line-color': '#9dbaea'});
             }

             _d = _thisReference.getRandom();
             cy.$('#cd').data("title", _d);
             if (_d > 50) {
                cy.$('#cd').style({'line-color': '#FF6347'});
             } else {
                cy.$('#cd').style({'line-color': '#9dbaea'});
             }

             _d = _thisReference.getRandom();
             cy.$('#ac').data("title", _d);
             if (_d > 50) {
                cy.$('#ac').style({'line-color': '#FF6347'});
             } else {
                cy.$('#ac').style({'line-color': '#9dbaea'});
             }

             _d = _thisReference.getRandom();
             cy.$('#ad').data("title", _d);
             if (_d > 50) {
                cy.$('#ad').style({'line-color': '#FF6347'});
             } else {
                cy.$('#ad').style({'line-color': '#9dbaea'});
             }

        }, 1500);
    }

    private getRandom():number {
      return Math.floor(Math.random() * (100 - 10) + 10);
    }


    private getData():any {

        return [
          { data: { id: 'a', title: 'Mobile app' } },
          { data: { id: 'b', title: 'Web-server' } },
          { data: { id: 'c', title: 'Dispatcher' } },
          { data: { id: 'd', title: 'Payment system' } },
          {
            data: {
              id: 'ab',
              title:  this.getRandom(),
              source: 'a',
              target: 'b'
            }
          },
          {
            data: {
              id: 'cd',
              title: this.getRandom(),
              source: 'c',
              target: 'd'
            }
          },
          {
            data: {
              id: 'ac',
              title: this.getRandom(),
              source: 'a',
              target: 'c'
            }
          },
          {
            data: {
              id: 'ad',
              title: this.getRandom(),
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