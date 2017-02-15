import { AbstractComponentSpecificView } from './abstract.component.specific.view';
import { E2EView } from './abstract.e2e.view';

export class ChildComponentsView extends AbstractComponentSpecificView {
    public type:string = E2EView.CHILD_COMPONENT;

    public draw(initialData:any):any {
        // do nothing here
    }

    public updateData(currentData:any):any {
        // do nothing here
    }

    public toJSON():any {
        let _value:any = {};
        _value["@type"] = this.type;
        _value["name"] = this.name;
        _value["rootComponent"] = this.rootComponent;
        if (!$.isEmptyObject(this.preferences)) {
            _value["preferences"] = this.preferences;
        }
        return _value;
    }
}