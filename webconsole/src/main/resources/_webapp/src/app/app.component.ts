/*
 * Angular 2 decorators and services
 */
import { Component, ViewEncapsulation, AfterViewInit } from '@angular/core';
import 'style!css!less!font-awesome-webpack/font-awesome-styles.loader!font-awesome-webpack/font-awesome.config.js';
import { AppState } from './app.service';

/*
 * App Component
 * Top Level Component
 */
@Component({
  selector: 'app',
  encapsulation: ViewEncapsulation.None,
  styleUrls: [
    './app.style.css'
  ],
  templateUrl: './app.component.html'
})
export class App implements AfterViewInit {

  constructor(
    public appState: AppState) {

  }

  ngOnInit() {
    console.log('Initial App State', this.appState.state);
  }

   ngAfterViewInit() {
      $(document).ready(function(){

         $('.collapse-link').on('click', function() {
             var $BOX_PANEL = $(this).closest('.x_panel'),
                 $ICON = $(this).find('i'),
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
         });

         $('.close-link').click(function () {
             $(this).closest('.x_panel').remove();
         });

      });
  }

}
