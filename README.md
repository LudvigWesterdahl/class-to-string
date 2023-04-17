# Class To String

## General
This library is used to iterate over a class structure.

It is built using java reflection API.

Supports java version 11 and above.

## Documentation

### 1. Introductory samples
First, let's ensure that everything works and a quick teaser of what the library
can do with two examples.

More samples can be found towards the end of the documentation.

#### Hello World
Your standard hello world.

See sample
[HelloWorld.java](src/main/java/se/ludvigwesterdahl/samples/HelloWorld.java)

#### Select & Expand
Create OData `$select` and `$expand` query parameters using the response class.

Here we have a `PersonResponse` containing some fields and some nested objects. 
Imagine that this is the JSON response from an OData API. Instead of maintaining
separate query parameters from the response object, this library can take the response class
and generate the query parameters for you.

See sample
[SelectExpand.java](src/main/java/se/ludvigwesterdahl/samples/SelectExpand.java)

### 2. Generator 
The core of the library is located in
[ClassToStringGenerator.java](src/main/java/se/ludvigwesterdahl/lib/cts/ClassToStringGenerator.java).

This class serves the purpose of iterating over the given class structure.

### 3. Observers

#### 3.1 Generation strategy
This is a type of observer that implements
[GenerationStrategy.java](src/main/java/se/ludvigwesterdahl/lib/cts/strategy/GenerationStrategy.java).
This observer has to be added to the generator using `ClassToStringGenerator#addObserver(Observer)`
and will be returned in the order when calling `ClassToStringGenerator#iterate()`.

However, there is no requirement to actually use the result from `ClassToStringGenerator#iterate()`. Instead, one
could add the strategy as an observer and keep the reference to it. And once a notification that the root node has
been left, perform any actions they see fit.

#### 3.2 Blocker



### x. Samples


#### x.y Tests
The tests for
[ClassToStringGenerator.java](src/main/java/se/ludvigwesterdahl/lib/cts/ClassToStringGenerator.java)
contains some useful samples albeit less commented.

- [AddRemoveNameWithCode.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/AddRemoveNameWithCode.java)
shows how to add or remove a name with code.
- [EmbedBeforeBlocker.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/EmbedBeforeBlocker.java)
shows how to embed and add a blocker with code. It should also clarify that embeddings happens
before a blocker.
- [FixEmbedAnnotationCycle.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/FixEmbedAnnotationCycle.java)
shows how to deal with an invalid embed cycle with code.
- [ListGenericRename.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/ListGenericRename.java)
  shows how names work for generic lists with code.
- [ListGenericRenameEmbedWithAnnotation.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/ListGenericRenameEmbedWithAnnotation.java)
  shows how names work for generic lists with annotations.
- [RemoveRenameAnnotation.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/RemoveRenameAnnotation.java)
  shows how to remove effect of annotations with code.
- [SimpleListSameErasure.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/SimpleListSameErasure.java)
  shows how to deal with fields that would have the same erasure and if a list was missing a name.
- [SimpleStructureWithCode.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/SimpleStructureWithCode.java)
  shows a "hello world" like example.
- [TypicalSelectExpandQuery.java](src/test/java/se/ludvigwesterdahl/lib/fixture/ctstestcases/TypicalSelectExpandQuery.java)
  shows a bigger example, similar to the sample
  [SelectExpand.java](src/main/java/se/ludvigwesterdahl/samples/SelectExpand.java)
