# OptionJS

## Examples

### Basics

Option can be used to make code shorter and more readable. It can help eliminate convoluted trees of `if`s and get rid of hordes of testing for null/undefined/NaN.

```javascript
const greet = (input) => {
    console.log(input, '->',
            Option(input).map(x => `Hello ${x}!`).getOrElse('Who are you?')
    );
};
greet('Martin');  // Martin -> Hello Martin!
greet(undefined); // undefined -> Who are you?
greet(null);      // null -> Who are you?
greet(NaN);       // NaN -> Who are you?
```

It can easily supply default values and be nicely chained.

```javascript
// TODO
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
                    Option(x.middleName).orElse(Option(x.nickname)).map(y => `'${y}'`).get,
                    Option(x.surname).getOrElse('Doe')
                ].filter(x => x).join(' ')
        ).get;

function testCompileName(person) {
    console.log(person, '->', compileName(person));
}

[jim, spock, bones].forEach(testCompileName);
```

Output:

```
Object { name: "James", nickname: "Jim", middleName: "Tiberius", surname: "Kirk" } -> James 'Tiberius' Kirk
Object { name: "Spock" } -> Spock Doeindex-dev.html:50:5
Object { name: "Leonard", nickname: "Bones", middleName: null, surname: "McCoy" } -> Leonard 'Bones' McCoy
```
