import { ColoredAttributePredicate } from './colored.predicate';

export class ColoredAttributeChecker {
    public green:ColoredAttributePredicate = undefined;
    public yellow:ColoredAttributePredicate = undefined;

    public toJSON():any {
        let _value:any = {};
        _value["green"] = this.green.toJSON();
        _value["yellow"] = this.yellow.toJSON();
        return _value;
    }
}