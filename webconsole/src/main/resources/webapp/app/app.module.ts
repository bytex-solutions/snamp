import { NgModule }           from '@angular/core';
import { BrowserModule }      from '@angular/platform-browser';
import { AppComponent }       from './app.component';
import { HeaderModule }       from './app.header';
import { AppRoutingModule }   from './app-routing.module';

@NgModule({
  imports:      [
    BrowserModule,
    AppRoutingModule
  ],
  declarations: [ AppComponent, HeaderModule ],
  bootstrap:    [ AppComponent, HeaderModule ]
})
export class AppModule { }