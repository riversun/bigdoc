/*  bigdoc Java lib for easy to read/search from a big document
 *
 *  Copyright (c) 2006-2016 Tom Misawa, riversun.org@gmail.com
 *  
 *  Permission is hereby granted, free of charge, to any person obtaining a
 *  copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation
 *  the rights to use, copy, modify, merge, publish, distribute, sublicense,
 *  and/or sell copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following conditions:
 *  
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *  
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 *  DEALINGS IN THE SOFTWARE.
 *  
 */
package org.riversun.bigdoc;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.riversun.bigdoc.bin.BigFileSearcher;
import org.riversun.bigdoc.bin.BigFileSearcher.OnProgressListener;
import org.riversun.bigdoc.bin.BigFileSearcher.OnRealtimeResultListener;

/**
 * test for BigFileSearcher
 *
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class TestBigFileSearcher extends TestBase {
	@Rule
	public TestName name = new TestName();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test_search() {

		final String searchText = "hello world";

		final byte[] searchBytes = getFromUTF8(searchText);

		final BigFileSearcher obj = new BigFileSearcher();

		final File srcFile = getFileFromResource("bigdoc_bigfile_test_5mbyte.bin");

		final List<Long> result = obj.searchBigFile(srcFile, searchBytes);

		System.out.println("[" + name.getMethodName() + "] Profile Information");
		obj._showProfile();

		assertThat(result, contains(0L, 1022976L, 2045952L, 3068928L, 4091904L, 5114880L,5242863L));
		System.out.println("[" + name.getMethodName() + "] ellapsed " + String.format("%.1f sec", ((float) obj.getEllapsedMillis() / (float) 1024)) +
				" for " + srcFile.length() / (1024 * 1024) + " mbytes");
		System.out.println();
	}

	@Test
	public void test_search_with_progress_callback() {

		final String searchText = "hello world";

		final byte[] searchBytes = getFromUTF8(searchText);

		final BigFileSearcher obj = new BigFileSearcher();

		final File srcFile = getFileFromResource("bigdoc_bigfile_test_5mbyte.bin");

		System.out.println("[" + name.getMethodName() + "] Profile Information");

		final List<Long> result = obj.searchBigFile(srcFile, searchBytes, new OnProgressListener() {

			@Override
			public void onProgress(float progress) {
				System.out.println("[" + name.getMethodName() + "]" + " progress " + (int) (progress * 100f) + "% done");

			}
		});
		obj._showProfile();
		assertThat(result, contains(0L, 1022976L, 2045952L, 3068928L, 4091904L, 5114880L,5242863L));
		System.out.println("[" + name.getMethodName() + "] ellapsed " + String.format("%.1f sec", ((float) obj.getEllapsedMillis() / (float) 1024)) +
				" for " + srcFile.length() / (1024 * 1024) + " mbytes");
		System.out.println();

	}

	@Test
	public void test_searchBigFileRealtime() {

		final String searchText = "hello world";

		final byte[] searchBytes = getFromUTF8(searchText);

		final BigFileSearcher obj = new BigFileSearcher();

		final File srcFile = getFileFromResource("bigdoc_bigfile_test_5mbyte.bin");

		System.out.println("[" + name.getMethodName() + "] Profile Information");

		final List<Long> result = obj.searchBigFileRealtime(srcFile, searchBytes, new OnRealtimeResultListener() {

			@Override
			public void onRealtimeResultListener(float progress, List<Long> pointerList) {
				System.out.println("[" + name.getMethodName() + "]" + " progress " + (int) (progress * 100f) + "% done result=" + pointerList);

			}
		});
		obj._showProfile();
		assertThat(result, contains(0L, 1022976L, 2045952L, 3068928L, 4091904L, 5114880L,5242863L));
		System.out.println("[" + name.getMethodName() + "] ellapsed " + String.format("%.1f sec", ((float) obj.getEllapsedMillis() / (float) 1024)) +
				" for " + srcFile.length() / (1024 * 1024) + " mbytes");
		System.out.println();
	}

}
