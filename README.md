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
## Performance Test
Search sequence of bytes from big file

### Environment
Core i7-2400 3.4GHz(8-Core) 512MB(heap)<br>

### Results

<table>
<tr><td>10MB</td><td>0.8s</td></tr>
<tr><td>50MB</td><td>4.4s</td></tr>
<tr><td>100MB</td><td>8.7s</td></tr>
<tr><td>250MB</td><td>21.7s</td></tr>
<tr><td>1GB</td><td>109.4s</td></tr>
<tr><td>5GB</td><td>457.3s</td></tr>
<tr><td>10GB</td><td>903.6s</td></tr>
</table>


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
