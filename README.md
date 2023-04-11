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

### 4. Blockers


### x. Samples
