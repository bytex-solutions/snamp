import { Component, Input, OnInit } from '@angular/core';

@Component({
  moduleId: module.id,
  selector: 'timeInterval',
  templateUrl: './templates/time.intervals.html',
  styles: ['.flatbar { width: 100% !important; text-align: left !important; margin-left: -15px !important; }']
})
export class TimeIntervalsView {
    @Input() jsonObject: any = {};
    @Input() id:string = "";
    @Input() title:string = "";
}

