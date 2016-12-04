import { Pipe, PipeTransform } from '@angular/core';

import { ParamDescriptor } from './model/model.paramDescriptor';

@Pipe({name: 'keys'})
export class KeysPipe implements PipeTransform {
  transform(value, args:string[]) : any {
    let keys = [];
    for (let key in value) {
      keys.push({key: key, value: value[key]});
    }
    return keys;
  }
}

@Pipe({
    name: 'required',
    pure: false
})
export class RequriedParametersFilter implements PipeTransform {
    transform(items: ParamDescriptor[], args: any[]): ParamDescriptor[] {
        if (items == null) {
            return null;
        } else {
            return items.filter(item => item.required == true);
        }
    }
}

@Pipe({
    name: 'optional',
    pure: false
})
export class OptionalParametersFilter implements PipeTransform {
    transform(items: ParamDescriptor[], args: any[]): ParamDescriptor[] {
        if (items == null) {
            return null;
        } else {
            return items.filter(item => item.required == false);
        }
    }
}