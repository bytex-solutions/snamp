import { Component } from '@angular/core';
import { CookieService } from 'angular2-cookie/core';

@Component({
  selector: 'topnav-bar',
  providers: [CookieService],
  styleUrls: [ '../app.style.css' ],
  templateUrl: './topnavbar.component.html'
})
export class TopNavBar {
    // TypeScript public modifier
    constructor(private _cookieService:CookieService) {

    }

    public clearCookie() {
      this._cookieService.removeAll();
    }

    toggleClicked(event: MouseEvent)
    {
        var target = event.srcElement.id;
        var body = $('body');
        var menu = $('#sidebar-menu');

        // toggle small or large menu
        if (body.hasClass('nav-md')) {
            menu.find('li.active ul').hide();
            menu.find('li.active').addClass('active-sm').removeClass('active');
        } else {
            menu.find('li.active-sm ul').show();
            menu.find('li.active-sm').addClass('active').removeClass('active-sm');
        }
        body.toggleClass('nav-md nav-sm');

    }


  ngOnInit() {
    console.log('hello `topnavbar` component');
  }

  ngAfterViewInit(){

  }

}
