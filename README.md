# Overview
'bigdoc' allows you to handle gigabyte order files easily with high performance.
You can search bytes or words / read data/text from huge files.

It is licensed under [MIT license](https://opensource.org/licenses/MIT).

# Quick start
## Search sequence of bytes from a big file quickly.

Search mega-bytes,giga-bytes order file.

```java
package org.example;

import java.io.File;
import java.util.List;

import org.riversun.bigdoc.bin.BigFileSearcher;

public class Example {

	public static void main(String[] args) throws Exception {

		byte[] searchBytes = "hello world.".getBytes("UTF-8");

		File file = new File("/var/tmp/yourBigfile.bin");

		BigFileSearcher searcher = new BigFileSearcher();

		List<Long> findList = searcher.searchBigFile(file, searchBytes);

		System.out.println("positions = " + findList);
	}
}
```


- Please note

The result is different depending on the environment of the Java ,Java version and compiler or runtime optimization.


# Downloads
## maven
- You can add dependencies to maven pom.xml file.
```xml
<dependency>
  <groupId>org.riversun</groupId>
  <artifactId>bigdoc</artifactId>
  <version>0.3.0</version>
</dependency>
```
