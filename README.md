![Image of Yaktocat](doc/logo.png)

Pebble is a project with the vision of building a key value store that allows the storage and retrieval of big data
in main memory, instead of distributed systems on disk. Making simpler, faster and cheaper to access and process
this data.

Pebble is based on the work made by Paolo Boldi and Sebastiano Vigna with WebGraph. Reference to their work can be
found in this paper

[Paolo Boldi and Sebastiano Vigna. The WebGraph framework I: Compression techniques. In Proc.of the Thirteenth International World Wide Web Conference (WWW 2004), pages 595−601, Manhattan, USA, 2004. ACM Press](http://vigna.di.unimi.it/ftp/papers/WebGraphI.pdf)

and the [Web Graph page](http://webgraph.di.unimi.it/).

The Pebble projects extends their algorithms to store data tables with an extended set of supported data types and
expects to provide an implementation better suited for production systems.

The project is on its early stage of implementation and work remains to be done to achieve its vision.

Quick Start
-----------

### 1. Compile and build package
```bash
mvn package
```

### 2. If desired build jar containing all dependencies.
```bash
mvn assembly:single
```
This could take around 5 minutes.

Pebble Core API Description
---------------------------
The current implementation supports the compression and decompression of lists of positive integers and longs. These
lists can be of three types: **strictly incremental lists**, **incremental lists** and **unsorted lists**. For details
regarding the library api, refer to the
[Api Documentation](//groupon.github.io/pebble/index.html).
On following sections the basics around encoding and decoding lists is explained.

### Encoding Lists
The core pebble API exposes the compression methods through the
[OutputSuccinctStream](//groupon.github.io/pebble/org/pebble/core/encoding/OutputSuccinctStream.html)
class. This class exposes three main methods:
* [writeStrictlyIncrementalList](//groupon.github.io/pebble/org/pebble/core/encoding/OutputSuccinctStream.html#writeStrictlyIncrementalList(it.unimi.dsi.fastutil.ints.IntList, int, int, org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore))
  used to compress strictly incremental lists, such as:
  ```java
  {4, 6, 7, 8, 11, 12}
  ```

* [writeIncrementalList](//groupon.github.io/pebble/org/pebble/core/encoding/OutputSuccinctStream.html#writeIncrementalList(it.unimi.dsi.fastutil.ints.IntList, int, int, org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore))
  used to compress incremental lists, such as:
  ```java
  {4, 6, 6, 6, 6, 7, 8, 8, 8, 11, 11, 12, 12, 12, 12, 12}
  ```

* [writeList](//groupon.github.io/pebble/org/pebble/core/encoding/OutputSuccinctStream.html#writeList(it.unimi.dsi.fastutil.ints.IntList, int, int, org.pebble.core.encoding.ints.datastructures.IntReferenceListsStore))
  used to compress lists with none order restrictions, such as:
  ```java
  {6, 12, 11, 8, 12, 8, 6, 12, 8, 12, 6, 11, 6, 4, 12, 7}
  ```

These three methods receives four arguments: **list**, **index**, **bits size** and a **reference lists store**,
each explained below. These methods returns the numbers of bits used to encode the input list.

#### List
This is the list to be encoded. The lists needs to be stored in a an instance of
[IntList](http://fastutil.di.unimi.it/docs/it/unimi/dsi/fastutil/ints/IntList.html)
class . For example:
```java
IntList list = new IntArrayList(new int[] {4, 6, 7, 8, 11, 12});
```
**WARNING** the encoding process it might modify the input list. So in case the lists needs to be kept unchanged, a copy of
the input list should be passed to the encoding function.

#### Index
Index of the input list. This is a correlative number of the passed list. The first list, should be passed with
index `0`, second list should be passed with index `1` and so on...

#### Bits Size
Number of bits required for the binary representation of the expected biggest value on the list. Is important to
highlight that this is not the maximum number of the input list, instead correspond to the expected maximum number
of the whole lists collection to be encoded. In case the values can be any integer, the value should be `31` given
that only positive numbers are supported.

#### Reference Lists Store
Collection of previously compressed lists. This collection is used internally for Pebble's compression algorithm,
to store and find reference lists candidates for the input list. To obtain an instance of
[IntReferenceListsStore](//groupon.github.io/pebble/org/pebble/core/encoding/small/datastructures/IntReferenceListsStore.html)
for parameters needs to be passed:
* **size** - maximum numbers of lists to be stored. When the number of lists stored exceeds size. Oldest stored lists
  will be replaced by newer lists.
* **maxRecursiveReferences** - maximum number of allowed recursive references. This limits the maximum number of
  recursive references a list can have in order to be used as a reference lists. As this parameters gets bigger,
  the likelihood of finding better reference candidates in terms of compression increases, but with the trade of
  of reading speed when decoding lists.
* **minListSize** - minimum required size of a list to be stored on the reference lists collection.
* **referenceListIndex** - index is used to find the best reference list candidate. This must be an instance of any class
  which implements the
  [IntReferenceListsIndex](//groupon.github.io/pebble/org/pebble/core/encoding/small/datastructures/IntReferenceListsIndex.html)
  interface. The pebble core library provides the class
  [InvertedListIntReferenceListsIndex](//groupon.github.io/pebble/org/pebble/core/encoding/small/datastructures/InvertedListIntReferenceListsIndex.html)
  as an implementing class of this interface.
A reference lists store can be instantiated as:
```java
final int size = 10000;
final int maxRecursiveReferences = 3;
final int minListSize = 1;
final IntReferenceListsIndex referenceListsIndex = new InvertedListIntReferenceListsIndex();
final IntReferenceListsStore referenceListsStore = new IntReferenceListsStore(
    size,
    maxRecursiveReferences,
    minListSize,
    referenceListsIndex
);
```

Finally to encode the example set of lists `{4, 6, 7, 8, 11, 12}` and `{4, 5, 6, 7, 8, 9, 10, 11, 12}` a possible
code would be:
```java
IntList list1 = new IntArrayList(new int[] {4, 6, 7, 8, 11, 12});
IntList list2 = new IntArrayList(new int[] {4, 5, 6, 7, 8, 9, 10, 11, 12});
OutputStream outputStream = new FileOutputStream("example.pz");
final OutputSuccinctStream outputSuccinctStream = new OutputSuccinctStream(outputStream);
final long[] offsets = new long[2];
int offset = 0;
offset += outputSuccinctStream.writeStrictlyIncrementalList(list1, 0, 31, referenceListsStore);
offsets[1] = offset;
offset += outputSuccinctStream.writeStrictlyIncrementalList(list2, 1, 31, referenceListsStore);
outputSuccinctStream.close();
```
At the end of this code the `offset` value will contains the total number of bits used to compress the collection.

### Decoding Lists
The core pebble API exposes three types of iterators used to iterate over the compressed representation of the
three types of supported lists.
* [StrictlyIncrementalListIterator](//groupon.github.io/pebble/org/pebble/core/decoding/iterators/small/StrictlyIncrementalListIterator.html)
* [IncrementalListIterator](//groupon.github.io/pebble/org/pebble/core/decoding/iterators/small/IncrementalListIterator.html)
* [ListIterator](//groupon.github.io/pebble/org/pebble/core/decoding/iterators/small/ListIterator.html)

Each of these tree iterators has a `build` method that returns an instance of the iterator starting on the beginning
of the compressed list. This method receives four arguments: **index**, **bits size** and
a **bytes store**, each explained below.

#### Index
Index of the input list to be retrieved.

#### Bits Size
Number of bits required for the binary representation of the expected biggest value on the compressed collection of
lists.

#### Bytes Store
This must be an instance of any class which implements the
  [PebbleBytesStore](//groupon.github.io/pebble/org/pebble/core/decoding/PebbleBytesStore.html)
  interface. This abstracts the specifics on how the compressed data is stored and retrieved, adding flexibility in
  the way the compressed data is handled. The pebble core library provides a basic class that implements this
  interface
  [BytesArrayPebbleBytesStore](//groupon.github.io/pebble/org/pebble/utils/decoding/BytesArrayPebbleBytesStore.html)
  as an implementing class of this interface. This class is a simple wrapper of a single byte array.

Finally to decode the first list used on the encoding example on [Encoding](#encoding-lists) section, a
possible code would be:
```java
byte[] data = Files.readAllBytes(Paths.get("example.pz"));
PebbleBytesStore bytesStore = new BytesArrayPebbleBytesStore(data, offsets);
IntIterator iterator = StrictlyIncrementalListIterator.build(0, 31, bytesStore);
while (iterator.hasNext()) {
    System.out.println(iterator.nextInt());
}
```
