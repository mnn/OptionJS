# OptionJS

**WIP**

## What is it?

If you don't know what [`Option` (`Maybe`)](https://en.wikipedia.org/wiki/Option_type) is you can visualize it as a box with one slot. It can be empty or filled with one item, like an array with size of 0 or 1. (If you worked with Lodash this concept must feel very familiar to you.)

You can create an `Option` like this:

```javascript
const option = Option('data');
```

then you do some magic on it and after you are done you can obtain what is inside:
```javascript
console.log(option.orNull); // prints 'data'
```

When you call `Option` you get either an instance of `Some` or `None`. `Some` represents a box with value in it, `None` is an empty box. They both have same interface. The beauty of `Option` is that when you wrap some value with it, the value gets normalized - if it is `null`, `undefined` or `NaN` you always get back `null`. And you can safely, without need to test for those bad values, operate over `Option` instance using for example `map` method. Tha `map` won't do a thing on `None`, just return the same object. But on `Some` it will apply a passed function and then return new instance of `Some`.

Let's have an example:
```javascript
Option(5)          // creates Some(5)
  .map(x => x + 1) // returns Some(6)
  .orNull          // unpacks Some, return 6
```
This is trivial, just like `5 + 1`, right?

Things got more complicated when input might be `null` or `undefined`.

```javascript
Option(null)       // creates None()
  .map(x => x + 1) // still None()
  .orNull          // unpacks None(), which is null
```
Now, when a mad server returns `null` instead of number we propagate this `null`. If we did just `null + 1` we would get `1` which isn't very useful, is it?

You probably still don't believe in value of using `Option`s, because one could simply do `input ? input + 1 : null`. You will see for yourself that in more complex scenarios it can help a lot.

## Examples

### Basics

Option can be used to make code shorter and more readable. It can help eliminate convoluted trees of `if`s and get rid of hordes of testing for `null`/`undefined`/`NaN`.

Let's start with obligatory hello world.

```javascript
Option('Hello world!').foreach(x => console.log(x)); // prints 'Hello world!' to the console
```

Well, this most likely did not amaze you. Let's try something a bit more practical - processing values which can be possibly `undefined`, `null` or `NaN`.

```javascript
const greet = input => {
    const result = Option(input).map(x => `Hello ${x}!`).getOrElse('Who are you?');
    console.log(input, '->', result);
};
greet('Martin');  // Martin -> Hello Martin!
greet(undefined); // undefined -> Who are you?
greet(null);      // null -> Who are you?
greet();          // undefined -> Who are you?
greet(NaN);       // NaN -> Who are you?
```

`Option` can easily supply default values and can be comfortably chained.

```javascript
const reverseString = x => x.split('').reverse().join('');
const makeItRude = x => x.toUpperCase();
const process = input => Option(input).map(reverseString).orElse(Option('default value')).map(makeItRude).orNull;
process('input A') // returns 'A TUPNI'
process(null)      // return 'DEFAULT VALUE'
```

Compared to Scala version there are some extra methods like `mapIf` or `match`.

```javascript
const haltVehicle = () => {};
const launch = fuel => `whee, that ${fuel} burns so hot`;
const postpone = () => {
    haltVehicle();
    return 'Wait! I got no fuel!';
};
const tryLaunch = maybeFuel => Option(maybeFuel).match(launch, postpone);
tryLaunch('log'); // returns 'whee, that log burns so hot'
tryLaunch(null);  // calls haltVehicle and returns 'Wait! I got no fuel!'
```

Of course you can chain it freely until you call end of `Option` via e.g. `get`, `orNull` or `getOrElse`. There are many more.

```javascript
const isAccessAllowed = user => Option(user)
        .filter(user => Option(user.age).exists(x => x >= 18))
        .exists(user => Option(user.name).exists(name => name.length > 2 && user.name[1] === 'e'));
isAccessAllowed(); // false
isAccessAllowed(null);  // false
isAccessAllowed({});  // false
isAccessAllowed({age: undefined}); // false
isAccessAllowed({name: null}); // false
isAccessAllowed({name: ''}); // false
isAccessAllowed({name: 'Jean-Luc', age: 58}); // true
isAccessAllowed({name: 'Wesley', age: 15}); // false
```

#### Option() versus Some()

Please note that they are not interchangeable. When you execute `Some(...)` you will always get an instance of `Some`. So you can effectively construct `Some(null)` or `Some(undefined)` which is usually not desirable - it nulls all advantages `Option` provides.

```javascript
const option = Option(null);
const some = Some(null);
option.isDefined // false
some.isDefined // true
option.map(x => x.length).orNull // null
some.map(x => x.length).orNull // crashed with: TypeError: Cannot read property 'length' of null
```

**TODO**: mapIf

#### Flatting

When composing `Option`s we will eventually hit a case of multiple wrapped `Some`s like here.
```javascript
Option(Option('double some')).toString() // "Some(Some(double some))"
```
Fear not, there is a simple fix - `flatten`.
```javascript
Option(Option('double some')).flatten.orNull // "double some"
```
Let's say we have some kind of complex computation which might fail. We don't want those ugly `null`s, so we naturaly use `Option`.
```javascript
const calculate = y => Option(y === 1 ? null : y + 100);
```
But, when want to chain some optional value with our computation, we are getting this multi-layered object:
```javascript
Option(0).map(calculate).toString() // "Some(Some(100))"
```
We could use the `flatten` mentioned earlier,
```javascript
Option(0).map(calculate).flatten.toString() // "Some(100)"
```
but there is a better way. Method `flatMap` is a combination of `map` and `flatten` (in this order) so we smoothly chain it like this:
```javascript
Option(0).flatMap(calculate).toString() // "Some(100)"
Option(1).flatMap(calculate).toString() // "None"
```
It is also a bit safer then `map` + `flatten`:
```javascript
Option(null).flatMap(calculate).toString() // "None"
Option(null).map(calculate).flatten.toString() // crashed with: "Cannot flatten None."
```

#### Conversions
```javascript
Option('Enterprise').toArray() // ["Enterprise"]
Option(null).toArray() // []
Option('Enterprise').toObject() // {"value":"Enterprise"}
Option('Enterprise').toObject('ship') // {"ship":"Enterprise"}
Option(null).toObject() // {}
```


### More complex usage

```javascript
const jim = {
    name: 'James',
    nickname: 'Jim',
    middleName: 'Tiberius',
    surname: 'Kirk'
};
const spock = {
    name: 'Spock'
};
const bones = {
    name: 'Leonard',
    nickname: 'Bones',
    middleName: null,
    surname: 'McCoy'
};

const compileName = person =>
        Option(person).map(x => [
                    Option(x.name).getOrElse('John'),
                    Option(x.middleName).orElse(Option(x.nickname)).map(y => `'${y}'`).orNull,
                    Option(x.surname).getOrElse('Doe')
                ].filter(x => x).join(' ')
        ).orNull;

function testCompileName(person) {
    console.log(person, '->', compileName(person));
}

[jim, spock, bones].forEach(testCompileName);
```

Output:

```
Object { name: "James", nickname: "Jim", middleName: "Tiberius", surname: "Kirk" } -> James 'Tiberius' Kirk
Object { name: "Spock" } -> Spock Doe
Object { name: "Leonard", nickname: "Bones", middleName: null, surname: "McCoy" } -> Leonard 'Bones' McCoy
```

## Development

[SBT](http://www.scala-sbt.org/) is required for build. [npm](https://www.npmjs.com/) is necessary for testing.

### Setup

After cloning repository or downloading a zip you need to install npm dependencies:

```
npm install
```

### Development
Build:
```
sbt fastOptJS
```
Or with watch functionality:
```
sbt ~fastOptJS
```
Scala tests can be run via (NodeJS with source maps is required):
```
sbt test
```

JavaScript tests can be run as follows:
```
npm run test-js
```

To run both test sets execute this:
```
npm test
```


### Optimized
To build an optimized (minimized) version:
```
sbt fullOptJS
```
