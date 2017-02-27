import { E2EView } from './abstract.e2e.view';
import { AbstractComponentSpecificView } from './abstract.component.specific.view';
import { ChildComponentsView } from './child.components.view';
import { ComponentModulesView } from './component.modules.view';
import { LandscapeView } from './landscape.view';

// Factory to create appropriate objects from json
export class Factory {

    public static createView(viewName:string, viewType:string, rootComponent?:string):E2EView {
        let _view:E2EView;
        switch(viewType) {
            case E2EView.CHILD_COMPONENT:
                _view = new ChildComponentsView();
                break;
            case E2EView.COMPONENT_MODULES:
                _view = new ComponentModulesView();
                break;
            case E2EView.LANDSCAPE:
                _view = new LandscapeView();
                break;
            default:
                throw new Error("Type " + viewType + " is unknown and cannot be parsed correctly");
        }
        _view.name = viewName;
        if (rootComponent) {
            if (_view instanceof AbstractComponentSpecificView) {
                (<AbstractComponentSpecificView>_view).rootComponent = rootComponent;
             } else {
                console.log("Attempt to set rootComponent for non component specific view. Will be ignored");
             }
        }
        // default values for the view
        _view.setDisplayedMetadata([]);
        _view.setLayout('circle');
        _view.setTextSize('20');
        _view.setTextColor('white');
        _view.setBackgroundColor('#999');
        _view.setTextOutlineColor('#999');
        _view.setTextOutlineWidth(1);
        _view.setTextWeight(400);

        _view.setEdgeWidth(2);
        _view.setEdgeLineColor('#999');
        _view.setEdgeArrowColor('#999');
        _view.setEdgeArrowShape('triangle');
        return _view;
    }

    // method for creating views
    public static viewFromJSON(_json:any):E2EView {
        let _type:string = _json["@type"];
        if (_type == undefined || _type.length == 0) {
            throw new Error("Type is not set for the view");
        } else {
            let _view:E2EView;
            switch(_type) {
                case E2EView.CHILD_COMPONENT:
                    _view = new ChildComponentsView();
                    break;
                case E2EView.COMPONENT_MODULES:
                    _view = new ComponentModulesView();
                    break;
                case E2EView.LANDSCAPE:
                    _view = new LandscapeView();
                    break;
                default:
                    throw new Error("Type " + _type + " is unknown and cannot be parsed correctly");
            }

            if (_view instanceof AbstractComponentSpecificView) {
                if (_json["rootComponent"] != undefined) {
                    (<AbstractComponentSpecificView>_view).rootComponent = _json["rootComponent"];
                }
            }

            if (_json["name"] != undefined) {
                _view.name = _json["name"];
            }
            if (_json["preferences"] != undefined) {
                _view.preferences = _json["preferences"];
            }
            console.log("New view has been instantiated from the json data object: ", _view);
            return _view;
        }
    }
}