import { E2EView } from './abstract.e2e.view';
import { SnampUtils } from "../../services/app.utils";

export class LandscapeView extends E2EView {
    public type:string = E2EView.LANDSCAPE;

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        if (this.isShelfLifeSet) {
            _value["shelfLife"] = SnampUtils.toDurationString(this.shelfLife);
        }
        return _value;
    }
}