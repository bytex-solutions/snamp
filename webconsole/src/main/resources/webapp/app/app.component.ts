import { Component } from '@angular/core';

@Component({
  selector: 'angular2-application',
  template: `
   <ul class="nav nav-tabs">
       <li role="presentation" routerLinkActive="active"><a routerLink="charts">Charts</a></li>
       <li role="presentation" routerLinkActive="active"><a routerLink="analysis">Analysis</a></li>
       <li role="presentation" routerLinkActive="active"><a routerLink="watchers">Watchers</a></li>
       <li role="presentation" routerLinkActive="active"><a routerLink="configuration">Configuration</a></li>
   </ul>

   <!-- Tab panes -->
   <div class="tab-content">
        <router-outlet></router-outlet>
   </div>
  `
})
export class AppComponent {
}