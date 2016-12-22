import { Component, Input, AfterViewInit } from '@angular/core';

@Component({
    moduleId: module.id,
    selector: 'panel',
    templateUrl: './panel.component.html',
})
export class PanelComponent {
   @Input() public header: string = 'Panel header';
   @Input() public column: string = '2';
   @Input() public showCloseButton:boolean = false;

    closeClicked(event: MouseEvent) {
        $(event.srcElement).closest('.x_panel').remove();
    }

    collapseClicked(event: MouseEvent) {
       var target = event.srcElement;
       var $BOX_PANEL = $(target).closest('.x_panel'),
           $ICON = $(target).find('i'),
           $BOX_CONTENT = $BOX_PANEL.find('.x_content');

       // fix for some div with hardcoded fix class
       if ($BOX_PANEL.attr('style')) {
           $BOX_CONTENT.slideToggle(200, function(){
               $BOX_PANEL.removeAttr('style');
           });
       } else {
           $BOX_CONTENT.slideToggle(200);
           $BOX_PANEL.css('height', 'auto');
       }

       $ICON.toggleClass('fa-chevron-up fa-chevron-down');
    }
}
