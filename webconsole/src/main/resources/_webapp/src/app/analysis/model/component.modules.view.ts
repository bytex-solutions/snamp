import { AbstractComponentSpecificView } from './abstract.component.specific.view';
import { E2EView } from './abstract.e2e.view';

export class ComponentModulesView extends AbstractComponentSpecificView {
    public type:string = E2EView.COMPONENT_MODULES;
}