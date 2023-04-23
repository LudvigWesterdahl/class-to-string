# Class To String
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ludvigwesterdahl/class-to-string.svg?label=Maven%20Central&logo=apachemaven)](https://central.sonatype.com/artifact/io.github.ludvigwesterdahl/class-to-string/)

## General
This library is used to iterate over a class or record structure.

It is built using java reflection API.

Supports java version 11 and above.

**Maven dependency**
```
<dependency>
    <groupId>io.github.ludvigwesterdahl</groupId>
    <artifactId>class-to-string</artifactId>
    <version>1.0.0</version>
</dependency>
```

Library can be found on
[MVN Repository](https://mvnrepository.com/artifact/io.github.ludvigwesterdahl/class-to-string)
and [Maven Central](https://central.sonatype.com/artifact/io.github.ludvigwesterdahl/class-to-string/).

## Documentation

### 1. Introductory samples
First, let's ensure that everything works and a quick teaser of what the library
can do with two examples.

More samples can be found towards the end of the documentation.

#### 1.1. Hello World
Your standard hello world.

See sample
[HelloWorld.java](src/main/java/se/ludvigwesterdahl/samples/HelloWorld.java)

#### 1.2. Select & Expand
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

In the documentation and library there will be references to nodes and leaf. The difference is that a node
can be entered and left whereas a leaf can only be consumed.

### 3. Observer
This type listens to the generator as it iterates over the class structure. The generator will notify all added
[Observer.java](src/main/java/se/ludvigwesterdahl/lib/cts/Observer.java)
from `ClassToStringGenerator#addObserver(Observer)` once a node has been entered, leaf found or a node has
been left.

The following sections explains the different observer types and included implementations.

#### 3.1 Generation strategy
This is a type of observer that implements
[GenerationStrategy.java](src/main/java/se/ludvigwesterdahl/lib/cts/strategy/GenerationStrategy.java).

A generation strategy is used to listen to the notifications produced by the generator and then produce 
a string from it. This library includes one such implementation
[FlatGenerationStrategy.java](src/main/java/se/ludvigwesterdahl/lib/cts/strategy/FlatGenerationStrategy.java).
This strategy is presented below.

This observer has to be added to the generator using `ClassToStringGenerator#addObserver(Observer)`
and will be returned in the order when calling `ClassToStringGenerator#iterate()`.

However, there is no requirement to actually use the result from `ClassToStringGenerator#iterate()`. Instead, one
could add the strategy as an observer and keep the reference to it. And once a notification that the root node has
been left, perform any actions they see fit.

##### 3.1.1 Flat generation strategy
This generation strategy produces the results as presented in all the samples in this documentation.
It can be configured with the following properties.
- pathSeparator (defaults to `,`): the delimiter between leaf
- levelMarker (defaults to `/`): the delimiter between the node level 
- nodes (defaults to `false`): if the generator should include nodes on their own
- leaf (defaults to `true`): if the generator should include leaf

Assume the previous configuration resulting in the following 
string `person/ssn,person/adress/zip,person/address/city,person/contact/phone`.

Now assume nodes were set to true, the following string would be returned 
instead `person,person/ssn,person/address,person/address/zip,person/address/city,person/contact,person/contact/phone`.

Finally, assume leaf were set to false and nodes to true, the following string would be returned
instead `person,person/address,person/contact`.

**Note** that same result could be achieved with a blocker, as explained in the next section, however,
the difference is that the blocker would affect all observers. Because with a blocker, the notifications would not
be sent.

#### 3.2 Blocker
This is a type of observer that implements
[Blocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/Blocker.java).

A blocker is used to limit the traversal of the generator. Before a node or leaf is processed, the generator will
check with all blockers to see if it is blocked. If it is, then the iteration will skip that leaf or node and
a notification will not be sent to any observer. **Note** that a blocker cannot block the leaving of a node, once
a node has been entered, it will always result in a leave notification.

This observer has to be added to the generator using `ClassToStringGenerator#addBlocker(Blocker)`.
**Note** that if a blocker is added with `ClassToStringGenerator#addObserver(Observer)` it will not be used as
a blocker, instead it will only receive notifications as any other observer.

To create a blocker, without having to implement all methods in
[Observer.java](src/main/java/se/ludvigwesterdahl/lib/cts/Observer.java),
the abstract class
[AbstractBlocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/AbstractBlocker.java)
can be used which implements all observer methods as no-ops and only requires the method in
[Blocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/Blocker.java).

##### 3.2.1 Leaf blocker
This blocker will block all leaf.

See [LeafBlocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/LeafBlocker.java).

##### 3.2.2 Loop blocker
This blocker is used to ensure that a given node or leaf will not be entered/consumed more than X number of times.

See [LoopBlocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/LoopBlocker.java).

##### 3.2.3 Max Depth blocker
This blocker is used to limit the number of nodes that can be entered after a given node.

A typical use case could be that you don't have access to the class being iterated over and after a certain point
iteration should stop due to unnecessary references to other nodes.

See [MaxDepthBlocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/MaxDepthBlocker.java).

##### 3.2.4 Simple blocker
This is used to block a given node or leaf from being entered/consumed. 

See [SimpleBlocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/SimpleBlocker.java).

##### 3.2.5 Static blocker
This blocker will block any node or leaf with a `static` modifier.

See [StaticBlocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/StaticBlocker.java).

##### 3.2.6 Transient blocker
This blocker will block any node or leaf with a `transient` modifier.

A use case for this blocker is to block the following field added by intellij when running tests with coverage.
```java
private static transient int[] __$lineHits$__;
```
Although the static blocker presented under section [3.2.5. Static blocker](#325-static-blocker) would also
block that field.

See [TransientBlocker.java](src/main/java/se/ludvigwesterdahl/lib/cts/blocker/TransientBlocker.java).

### 4. Samples
Two samples were already presented under [1. Introductory samples](#1-introductory-samples), this sections includes
more samples to try and explain what the library can do.

#### 4.1. JSON template
This sample shows how to produce a json template from a class structure, similar to a very basic
version of [Swagger](https://swagger.io/).
This is done by implementing a custom generation strategy.

See [JsonTemplate.java](src/main/java/se/ludvigwesterdahl/samples/JsonTemplate.java).

#### 4.2. Blocking public fields
Assume that public fields should not be included in the generation. This sample shows how this can be achieved.

See [BlockingPublicField.java](src/main/java/se/ludvigwesterdahl/samples/BlockingPublicField.java).

#### 4.3. Skipping iteration result
As explained under [3.1. Generation Strategy](#31-generation-strategy), it is not required to actually
use the returned list from the iteration. This sample shows a simple example how one can determine in an observer
when the iteration is finished.

See [SkipIterationResult.java](src/main/java/se/ludvigwesterdahl/samples/SkipIterationResult.java).

#### 4.4. Tests
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

### 5. Further reading
Most of the classes in this library is well documented. So please refer to the javadoc for more
method specific documentation and help.

### 6. Contact
My name is Ludvig Westerdahl and I work as a software engineer, you can reach me on email or linkedin.
- **E-mail**: ludvigwesterdahl@gmail.com 
- **LinkedIn**: https://linkedin.com/in/ludvigwesterdahl

![gmail-logo](https://img.shields.io/badge/Gmail-D14836?style=for-the-badge&logo=gmail&logoColor=white)
![linkedin-logo](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)
