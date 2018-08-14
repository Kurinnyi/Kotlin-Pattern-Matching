## Pattern matching for kotlin

This simple library brings functionality of pattern matching to Kotlin.
It uses some complex Kotlin features and combine them in easy to use way.
It is just a library, it does not do any byte code manipulation and it does not
require you to install any plugins to ide or your build tool. 
Just add it as a dependency to your project and you are ready to go.
It is also possible to enhance the lib with your custom matchers.

### Usage
Best shown with examples. 
You can also open tests in this repository to see some more examples. 
```kotlin
import ua.kurinnyi.kotlin.patternmatching.PatternMatching.match

val result = objectToMatch.match<Int> {
    case<Int>().then { 2 + 2 }                          //Matches any Int, returns 4
    case("Hello, world!").then { 5 }                    //Matches exact string "Hello world!"
    case("a", "b", null, "c").then { 6 }                //Matches any of the set "a", "b", "c" or null. Yes you can call match method on null
    case<String>().and { contains("Hello") }.then { 7 } //Matches any string with additional guard that it contains word "Hello"
    case<String>().then { it.length }                   //Matches any string that didn't matched before. Returns the size of this string
    otherwise { 42 }                                    //In all other cases
}
```
Evaluation is completed from top to bottom. 
And stops once any case clause matches.<br>
You should always provide `otherwise` handler or ensure you covered 
all possible cases. Otherwise `MatchError` is thrown if nothing matches the object.
<br>
The generic parameter of method `match<RESULT>` is the type of returned value.
It can be `Unit` in case if you do not expect any value to be returned.
It can also be nullable type.
<br>
Each `case` usage can be followed by `and` part. 
It takes predicate for more precise matching.
<br>
`then` and `and` methods take functions which receive matched objects 
as both `this` and `it` values. It gives ability 
to write concise code with objects, like bellow:
```kotlin
import ua.kurinnyi.kotlin.patternmatching.PatternMatching.match

data class SomeClass(val someString:String, val someInt:Int);
data class SomeOtherClass(val anotherString:String, val anotherInt:Int);

val objectToMatch = SomeClass("Hello", 15);
objectToMatch.match<Unit> {
    case<SomeClass>().and{ someInt < 15}.then{ print(someString + " < ") }
    case<SomeOtherClass>().and{ anotherInt < 15}.then{ print(anotherString + " other class") }
    case<SomeClass>().and{ someInt > 15}.then{ print(someString + " > ") }
    case<SomeClass>().then{ print(someString + " == ") }
}
//prints "Hello =="
```
<br>

#### Matching of lists
There is some special methods for lists.
Use next available object, or actual values to define patterns over list: 
* `head` - for a first item in list
* `mid` - for a second and next items 
* `tail` - for a list of remaining items, may be empty list
* `nil`, `end` - for the end of list

See example bellow:
```
import ua.kurinnyi.kotlin.patternmatching.ListPatterMatching.matchList
import ua.kurinnyi.kotlin.patternmatching.end
import ua.kurinnyi.kotlin.patternmatching.head
import ua.kurinnyi.kotlin.patternmatching.mid
import ua.kurinnyi.kotlin.patternmatching.tail

val list:List<String> = ... 
val resultString: String = list.matchList {                                                                                          //Bellow patterns match this examples:
    case(end).then { "empty list" }                                                                                                  // []
    case("It is head", end).then { head -> "Exact match of head for list with one element" }                                         // ["It is head"]
    case(head, end).and{ head -> head == "and part" }.then { head -> "Any list with one element with aditional guard" }              // ["and part"]
    case(head, end).then { head -> "Any lists with one element that left" }                                                          // ["whatever"]
    case(head, "this is mid", tail).then { head, mid, tail -> "Exect math of second element for list with at least two elements" }   // ["this is head", "this is mid", "whatever else"]
    case(head, mid, end).then { head, mid -> "Any list with two elements" }                                                          // ["two", "elements"]
    case(head, tail).then { head, tail -> "Any list with at least one element" }                                                     // ["a", "b", "c", "d"]
}
```
Function matchList has two generic parameters `matchList<LIST_TYPE, RESULT>`. 
First one is the type of the list and second is returned type which can be `Unit`. 
In most cases this parameters can be derived automatically and omitted as in example above.  

### Adding custom matchers:
There is two ways to add some custom logic for matching functionality:
* Simple one. Use match on `Any` or some specific type and additional predicate:
```kotlin
objectToMatch.match<Unit> {
    case<Any>().and{ yourPredicate(this)}.then { ... }                         
    case<Any>().and{ yourOtherPredicate(this)}.then { ... }                
}
```
* Enhance the lib:
```kotlin
import ua.kurinnyi.kotlin.patternmatching.PatternMatching
import ua.kurinnyi.kotlin.patternmatching.PatternMatching.match

//Write code like this
fun <RESULT> PatternMatching.MatchContext<RESULT>.caseStarsInRightPosition():PatternMatching.MatcherBuilder<Any, RESULT>{
    return PatternMatching.MatcherBuilder(this) {
        //Here goes your custom matching logic. The object is accessible via value variable.
        isStarsInRightPosition(value)
    }
}    

//And use it like this
objectToMatch.match<Unit>{
    //Some out of the box matcher ... 
    case<String>().then { ... }
    //work along with your custom matcher
    caseStarsInRightPosition().then { ... }
}
```
Your matching function should return `ua.kurinnyi.kotlin.patternmatching.PatternMatching.MatcherBuilder` or its child. 
As you can extend it too.  

### Adding to your project
Currently this library is not yet published to any repository.
You can download pre-built library [from here](http://TODO add link).
Or download this sources and build it yourself with command `gradlew jar`.
<br>
After you got the jar, it is up to you how to manage this dependency. 
You can add it to your repository manager, if you have one.
Or link as a local file in your build tool configuration.
<br> 
Information for build tools:
* group id      `ua.kurinnyi.kotlin` 
* artifact name `pattern-matching` 
* version        `0.1` 
 