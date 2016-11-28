import { NgModule }      from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { ApiClient }     from './app.restClient';
import { Gateways }  from './app.gateways';
import { HttpModule } from '@angular/http';
import {CookieService} from 'angular2-cookie/core';
import { KeysPipe } from './app.pipes'


@NgModule({
  imports:      [ BrowserModule, HttpModule],
  declarations: [ Gateways, KeysPipe],
  bootstrap:    [ Gateways ],
  providers: [ApiClient, CookieService]
})
export class AppModule { }