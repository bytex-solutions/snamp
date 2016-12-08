import { NgModule }           from '@angular/core';
import { BrowserModule }      from '@angular/platform-browser';
/* App Root */
import { AppComponent }       from './app.component';
import { Header }             from './app.header';

/* Routing Module */
import { AppRoutingModule }   from './app-routing.module';

@NgModule({
  imports:      [
    BrowserModule,
    AppRoutingModule
  ],
  declarations: [ AppComponent, Header ],
  bootstrap:    [ AppComponent ]
})
export class AppModule { }