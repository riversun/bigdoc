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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import org.riversun.bigdoc.bin.BigFileSearcher;
import org.riversun.finbin.BinaryUtil;

/**
 * Performance Test for BigFileSearcher<br>
 * Tom Misawa (riversun.org@gmail.com) <br>
 * <br>
 * <p>
 * Reference:<br>
 * 0.2 sec for 5 mbytes<br>
 * 0.8 sec for 10 mbytes<br>
 * 4.5 sec for 50 mbytes<br>
 * 8.8 sec for 100 mbytes<br>
 * <br>
 * <br>
 * blockSize=1.0(MB) worker buffer Size=1.0(MB), max num of thread=4, sub buffer
 * size=256(B), sub thread size=32,<br>
 * On Core i7-2400 3.4GHz 8Core CPU <br>
 * </p>
 */
public class GenPerformanceReport {

	public static void main(String[] args) {
		GenPerformanceReport obj = new GenPerformanceReport();
		obj.createDataFiles();
		obj.execPerformanceTest();
	}

	// PLEASE EDIT FOR YOUR ENVIRONMENT
	public static final String TEMP_PATH = "c:/temp";// "/var/tmp";

	private static final String TEXT = "hello world.";

	private static final int[] TEST_SIZE_MEGA_BYTES_ARRAY = new int[] { 5, 10, 50, 100, 250, 1*1024,5*1024,10*1024 };

	public void execPerformanceTest() {

		final String searchText = TEXT;

		final byte[] searchBytes = getFromUTF8(searchText);

		final BigFileSearcher obj = new BigFileSearcher();
		log("--------------------");

		for (int sizeMB : TEST_SIZE_MEGA_BYTES_ARRAY) {

			final File srcFile = new File(getTestDataFilePath(sizeMB));

			log("Testing... " + sizeMB + "MB " + srcFile);
			List<Long> searchBigFile = obj.searchBigFile(srcFile, searchBytes);
			boolean is_result_correct = searchBigFile.size() == (1 + sizeMB * 1024 * 1024 / (5 * 1024 * 1024));
			log("success =" + is_result_correct);
			obj.setUseOptimization(true);
			log("Condition");
			obj._showProfile();
			log("Done! Result, ellapsed " + String.format("%.1f sec", ((float) obj.getEllapsedMillis() / (float) 1024)) +
					" for " + sizeMB + " mbytes");
			System.out.println();

		}

	}

	public void createDataFiles() {

		log("BigFileSearcher Performance Test temporary directory=" + TEMP_PATH);
		log("--------------------");
		log("creating files...");
		for (int sizeMB : TEST_SIZE_MEGA_BYTES_ARRAY) {
			prepareTestDataFile(sizeMB);
		}
		log("Done. The data files created.");
	}

	private void prepareTestDataFile(int fileSizeMB) {
		final byte[] textBytes = BinaryUtil.getBytes(TEXT);
		final File file = new File(getTestDataFilePath(fileSizeMB));
		log("creating test data file=" + file);
		createTestDataFile(file, fileSizeMB, textBytes);
	}

	private String getTestDataFilePath(int fileSizeMB) {
		return TEMP_PATH + "/" + "org.riversun.bigdoc.big_file_test_" + fileSizeMB + ".bin";
	}

	private void createTestDataFile(File file, int fileSizeMB, byte[] testBinary) {
		if (file.isFile() && file.exists()) {
			return;
		}
		final long fileSize = (long) fileSizeMB * 1024L * 1024L;

		final byte[] buffer;

		if (fileSizeMB < 10) {
			buffer = new byte[fileSizeMB * 1024 * 1024];
		} else {
			buffer = new byte[10 * 1024 * 1024];
		}

		BinaryUtil.memcopy(buffer, testBinary, 0);

		FileChannel writeChannel = null;
		try {

			writeChannel = FileChannel.open(Paths.get(file.getAbsolutePath()), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

			for (long i = 0; i < fileSize; i += buffer.length) {

				// place text at last
				if (i + buffer.length == fileSize) {
					BinaryUtil.memcopy(buffer, testBinary, buffer.length - testBinary.length);
				}

				ByteBuffer bb = ByteBuffer.wrap(buffer);
				bb.rewind();
				writeChannel.write(bb);

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();

		} finally {

			if (writeChannel != null) {
				try {
					writeChannel.close();
				} catch (IOException e) {
				}
			}

		}
	}

	private byte[] getFromUTF8(String text) {
		return getBytesFromText(text, "UTF-8");
	}

	private byte[] getBytesFromText(String text, String charset) {

		byte[] searchBytes = null;
		try {
			searchBytes = text.getBytes(charset);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return searchBytes;
	}

	private void log(String msg) {
		System.out.println("[" + GenPerformanceReport.class.getSimpleName() + "] " + msg);
	}
}
