import { E2EView } from './abstract.e2e.view';

export class LandscapeView run extends E2EView {
    public type:string = E2EView.LANDSCAPE;

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