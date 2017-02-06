import { E2EView } from './abstract.e2e.view';

export class Dashboard {
    public type:string = "E2EDashboard";
    public views:E2EView[] = [];

    constructor(){};

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        let _views:any[] = [];
        for (let i = 0; i < this.views.length; i++) {
            _views.push(this.views[i].toJSON());
        }
        _value["views"] = _views;
        return _value;
    }
}