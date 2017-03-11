import { Component, Input, OnInit } from '@angular/core';
import { Watcher } from '../model/watcher';

@Component({
  moduleId: module.id,
  selector: 'watcherControl',
  templateUrl: './templates/watcher.html',
  styleUrls: ['./templates/css/watcher.css']
})
export class WatcherControlComponent {
    @Input() entity: Watcher = undefined;
    @Input() isNewEntity:boolean = true;

    public getPanelHeader():string {
        return this.isNewEntity ? "Add new watcher" : ("Edit watcher " + this.entity.name);
    }
}

