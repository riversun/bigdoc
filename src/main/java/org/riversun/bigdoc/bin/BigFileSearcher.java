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
package org.riversun.bigdoc.bin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.riversun.finbin.BigBinarySearcher;

/**
 * Search sequence of bytes from BIG file<br>
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class BigFileSearcher {

	private static final boolean USE_NIO = true;

	private boolean mLoopInprogress = true;
	private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024;

	private int mBufferSize = DEFAULT_BUFFER_SIZE;

	public void setBufferSize(int bufferSize) {
		this.mBufferSize = bufferSize;
	}

	public void read(File f, byte[] searchBytes) {

		final long startPos = 0;

		// -1 means read until the end
		final long readSize = -1;

		searchPartially(f, searchBytes, startPos, readSize);
	}

	public List<Long> search(File f, byte[] searchBytes) {
		return searchPartially(f, searchBytes, 0, -1);
	}

	public List<Long> searchPartially(File f, byte[] searchBytes, long startPos, long readSize) {

		final List<Long> pointerList = new ArrayList<Long>();

		mLoopInprogress = true;

		final BigBinarySearcher bbs = new BigBinarySearcher();

		final boolean hasReadingLimit = (readSize > 0);

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "r");

			final long targetFileSize = raf.length();

			if (startPos < 0 || startPos > targetFileSize) {
				throw new RuntimeException("StartPos is invalid.");
			}

			final long endPos;

			if (hasReadingLimit) {
				if (startPos + readSize > targetFileSize) {
					endPos = targetFileSize - 1;
				} else {
					endPos = startPos + readSize - 1;
				}

			} else {
				endPos = targetFileSize - 1;
			}

			byte byteBuf[] = null;

			long offsetPos = startPos;

			ByteBuffer nioByteBuf = null;

			if (USE_NIO) {
				nioByteBuf = ByteBuffer.allocate(mBufferSize);
			}

			if (searchBytes.length > mBufferSize) {
				throw new RuntimeException("The length of the target bytes is less than bufferSize.Please set more bigger bufferSize.");
			}

			final int byteShiftForSearch = (searchBytes.length - 1);

			while (mLoopInprogress) {

				raf.seek(offsetPos);

				final int actualBytesRead;

				if (USE_NIO) {
					FileChannel inChannel = raf.getChannel();

					nioByteBuf.clear();
					actualBytesRead = inChannel.read(nioByteBuf);

					if (nioByteBuf.hasArray()) {
						byteBuf = nioByteBuf.array();
					} else {
						byteBuf = new byte[mBufferSize];
						// transfer bytes from nioByteBuf into byteBuf
						nioByteBuf.get(byteBuf);
					}

				} else {
					byteBuf = new byte[mBufferSize];
					actualBytesRead = raf.read(byteBuf);

				}

				final byte[] bufForSearch;

				final int bytesRead;

				if (hasReadingLimit && ((offsetPos + actualBytesRead) >= endPos + 1)) {

					// When reading is over compared with the set readingLimit

					final long lValidReadingSize = (endPos + 1) - offsetPos;

					final int iValidReadingSize = (int) lValidReadingSize;

					bufForSearch = new byte[iValidReadingSize];

					bytesRead = iValidReadingSize;

					if (USE_NIO) {

						// set pos to first
						nioByteBuf.rewind();

						// transfer bytes from nioByteBuf into bufForSearch
						nioByteBuf.get(bufForSearch, 0, iValidReadingSize);

					} else {
						System.arraycopy(byteBuf, 0, bufForSearch, 0, iValidReadingSize);

					}
				}

				else {
					if (actualBytesRead != mBufferSize) {

						bufForSearch = new byte[actualBytesRead];
						if (USE_NIO) {

							// set pos to first,set limit to pointer of
							// bytesRead
							nioByteBuf.flip();

							// transfer bytes from nioByteBuf into bufForSearch
							nioByteBuf.get(bufForSearch);

						} else {
							System.arraycopy(byteBuf, 0, bufForSearch, 0, actualBytesRead);

						}
					} else {
						bufForSearch = byteBuf;
					}
					bytesRead = actualBytesRead;
				}

				final List<Integer> relPointerList = bbs.searchBigBytes(bufForSearch, searchBytes);

				for (Integer relPointer : relPointerList) {
					long absolutePointer = (long) relPointer.intValue() + offsetPos;
					pointerList.add((Long) absolutePointer);
				}

				// The reason of "- byteShiftForSearch".Read followings.
				// In order to read the value which straddles between the buffer
				// and buffer.
				offsetPos += bytesRead - byteShiftForSearch;

				long bytesRemain = (endPos + 1) - offsetPos;

				if (bytesRemain == byteShiftForSearch) {
					break;
				}
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
				}
			}

		}
		return pointerList;
	}
}
