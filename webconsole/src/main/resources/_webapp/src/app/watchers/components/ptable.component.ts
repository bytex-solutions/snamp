import { Component, Input, OnInit, ViewEncapsulation, ViewChildren, QueryList, ChangeDetectorRef } from '@angular/core';
import { Entity, KeyValue } from '../../configuration/model/model.entity';
import { InlineEditComponent } from '../../controls/editor/inline-edit.component';
import { VEXBuiltInThemes, Modal } from 'angular2-modal/plugins/vex';

@Component({
    moduleId: module.id,
    selector: 'ptable',
    templateUrl: './templates/ptable.component.html',
    encapsulation: ViewEncapsulation.None
})
export class PTable implements OnInit {
    @Input() entity: Entity;
    @ViewChildren(InlineEditComponent) editComponents: QueryList<InlineEditComponent>;

    constructor(public modal: Modal, private cd: ChangeDetectorRef) {}

    ngOnInit():void {}

    checkAndRemoveParameter(parameter:KeyValue):void {
        this.modal.confirm()
            .className(<VEXBuiltInThemes>'default')
            .isBlocking(true)
            .keyboard(27)
            .message("Remove parameter " + parameter.key + "?")
            .open()
            .then((resultPromise) => {
                return (<Promise<boolean>>resultPromise.result)
                    .then((response) => {
                        this.entity.removeParameter(parameter.key);
                        return response;
                    })
                    .catch(() =>  false);
            }).catch(() =>  false);
    }

    addNewParameter():void {
        this.entity.setParameter(new KeyValue("newParamKey", "newParamValue"));
        this.cd.detectChanges();
    }

}
