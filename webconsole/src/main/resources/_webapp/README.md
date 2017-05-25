### Snamp Webconsole
Snamp webconsole's main target - is to provide powerful and user-friendly web UI for set SNAMP's configuration, interactive viewing of current SNAMP resource's stated, E2E views, charts etc.

### JQuery and Bootstrap 3
**JQuery and Bootstrap have been added to this project to support requirements of Gentelella**
JQuery is imported globally, and the $ references are just typed in IDE by the globally imported typings jquery.d.ts file.  I do not do an import of JQuery on any pages as this has proven quite problematic for me, and a number of people in regards to both typings issues and JS module issues.

### Other dependencies (font-awesome)

**font-awesome**
I used font-awesome-webpack to bring in the font-awesome components.  There is an ugly hack in the import for this in the app modulke, mainly because of an issue with webpack2 <a href="https://github.com/gowravshekar/font-awesome-webpack/issues/24">outlined here </a> - there is a fork with the fix included, but I didn't use.  I include ng2-fontawesome, and use in one place, but most is just left as styles in HTML.   

### Quick start
**Make sure you have Node version >= 5.0 and NPM >= 3**
**Make sure you have typings installed globally**
I have compiled this typescript project with node 6.10.2 and npm 3.10.10. Please note that npm of version >= 4.x is not complied as well as node of version >= 7.x.

# installing behind the proxy
*Set npm proxy:*
npm config set proxy http://proxy.company.com:8080
npm config set https-proxy http://proxy.company.com:8080

*Set typings proxy:*
see [Typings FAQ](https://github.com/typings/typings/blob/master/docs/faq.md#configuration).

# install the repo with npm
npm install typings --global (*In case you don't have typings installed*)

npm install

# install typings with typings
typings install


# Getting Started
## Dependencies
What you need to run this app:
* `node` and `npm`and `typings` 
* Ensure you're running the latest versions Node `v4.x.x`+ (or `v5.x.x`) and NPM `3.x.x`+

> If you have `nvm` installed, which is highly recommended (`brew install nvm`) you can do a `nvm install --lts && nvm use` in `$` to run with the latest Node LTS. You can also have this `zsh` done for you [automatically](https://github.com/creationix/nvm#calling-nvm-use-automatically-in-a-directory-with-a-nvmrc-file) 

Once you have those, you should install these globals with `npm install --global`:
* `webpack` (`npm install --global webpack`)
* `webpack-dev-server` (`npm install --global webpack-dev-server`)
* `karma` (`npm install --global karma-cli`)
* `protractor` (`npm install --global protractor`)
* `typescript` (`npm install --global typescript`)
* `typings` (`npm install --global typings`)

## Installing
* `fork` this repo
* `clone` your fork
* `npm install webpack-dev-server rimraf webpack -g` to install required global dependencies
* `npm install` to install all dependencies
* `npm run server` to start the dev server in another tab

## Additional debug info
In case you receive non informative exceptions such as "Cannot set property stack of [object Object] which has only a getter" - you 
may open node_modules/zone.js/dist/long-stack-trace-zone.js and change 135 line to:
`Object.defineProperty(error, 'stack', renderLongStackTrace(parentTask.data && parentTask.data[creationTrace], error.stack));`

# Configuration
Configuration files live in `config/` we are currently using webpack, karma, and protractor for different stages of your application

# Contributing
You can include more examples as components but they must introduce a new concept such as `Home` component (separate folders), and Todo (services). I'll accept pretty much everything so feel free to open a Pull-Request

# TypeScript
> To take full advantage of TypeScript with autocomplete you would have to install it globally and use an editor with the correct TypeScript plugins.

## Use latest TypeScript compiler
TypeScript 1.7.x includes everything you need. Make sure to upgrade, even if you installed TypeScript previously. (Note I use typescript 2.0.1)

```
npm install --global typescript
```
# Types
> When you include a module that doesn't include Type Definitions inside of the module you can include external Type Definitions with @types

i.e, to have youtube api support, run this command in terminal: 
```shell
npm i @types/youtube @types/gapi @types/gapi.youtube
``` 
In some cases where your code editor doesn't support Typescript 2 yet or these types weren't listed in ```tsconfig.json```, add these to **"src/custom-typings.d.ts"** to make peace with the compile check: 
```es6
import '@types/gapi.youtube';
import '@types/gapi';
import '@types/youtube';
```

## Custom Type Definitions
When including 3rd party modules you also need to include the type definition for the module
if they don't provide one within the module. You can try to install it with @types

```
npm install @types/node
npm install @types/lodash
```

If you can't find the type definition in the registry we can make an ambient definition in
this file for now. For example

```typescript
declare module "my-module" {
  export function doesSomething(value: string): string;
}
```


If you're prototyping and you will fix the types later you can also declare it as type any

```typescript
declare var assert: any;
declare var _: any;
declare var $: any;
```

If you're importing a module that uses Node.js modules which are CommonJS you need to import as

```typescript
import * as _ from 'lodash';
```
