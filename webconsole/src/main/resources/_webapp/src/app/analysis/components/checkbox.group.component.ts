import { Component, Input, OnInit } from '@angular/core';

@Component({
  moduleId: module.id,
  selector: 'checkboxGroup',
  templateUrl: './templates/checkbox.group.html',
  styles: ['.flatbar { width: 100% !important; text-align: left !important; margin-left: -15px !important; }']
})
export class CheckboxGroupView {
    @Input() formName:string= "";
    @Input() id:string = "";
    @Input() title:string = "";
}

