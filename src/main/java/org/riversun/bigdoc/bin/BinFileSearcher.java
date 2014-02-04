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
import java.util.Comparator;
import java.util.List;

import org.riversun.finbin.BigBinarySearcher;

/**
 * Search sequence of bytes from Binary file<br>
 * 
 * @author Tom Misawa (riversun.org@gmail.com)
 *
 */
public class BinFileSearcher {

	private static final boolean USE_NIO = true;

	public static interface BigFileProgressListener {
		public void onProgress(List<Long> pointerList, float progress, float currentPosition, float startPosition, long maxSizeToRead);
	}

	private boolean mLoopInprogress = true;

	// DEFAULT 1 mega bytes
	private static final int DEFAULT_BUFFER_SIZE = 1 * 1024 * 1024;

	private BigFileProgressListener mBigFileProgressListener;

	private int mBufferSize = DEFAULT_BUFFER_SIZE;

	public void setBufferSize(int bufferSize) {
		this.mBufferSize = bufferSize;
	}

	/**
	 * Set the listener that callbacks the search-progress
	 * 
	 * @param listener
	 */
	public void setBigFileProgressListener(BigFileProgressListener listener) {
		mBigFileProgressListener = listener;
	}

	/**
	 * Returns the index within this file of the first occurrence of the
	 * specified substring.
	 * 
	 * @param f
	 * @param searchBytes
	 * @return
	 */
	public Long indexOf(File f, byte[] searchBytes) {
		return indexOf(f, searchBytes, 0);
	}

	/**
	 * Returns the index within this file of the first occurrence of the
	 * specified substring, starting at the specified position.
	 * 
	 * @param f
	 *            target file
	 * @param searchBytes
	 *            a sequence of bytes you want to find
	 * @param fromPosition
	 *            "0" means the beginning of the file
	 * @return position of the first occurence. '-1' means that it was not
	 *         found.
	 */
	public Long indexOf(File f, byte[] searchBytes, long fromPosition) {

		final List<Long> result = searchPartially(f, searchBytes, fromPosition, -1, new BigFileProgressListener() {

			@Override
			public void onProgress(List<Long> pointerList, float progress, float currentPosition, float startPosition, long maxSizeToRead) {

				if (mBigFileProgressListener != null) {
					mBigFileProgressListener.onProgress(pointerList, progress, currentPosition, startPosition, maxSizeToRead);
				}

				if (pointerList.size() > 0) {
					BinFileSearcher.this.stop();
				}
			}
		});

		if (result.size() > 0) {
			return result.get(0);
		} else {
			return -1L;
		}
	}

	/**
	 * 
	 * @param f
	 * @param searchBytes
	 * @return
	 */
	public List<Long> search(File f, byte[] searchBytes) {
		final long startPosition = 0;

		// -1 means read until the end
		final long maxSizeToRead = -1;
		return searchPartially(f, searchBytes, startPosition, maxSizeToRead);
	}

	/**
	 * Search for a sequence of bytes from the file within the specified size
	 * range starting at the specified position .
	 * 
	 * @param f
	 * @param searchBytes
	 *            a sequence of bytes you want to find
	 * @param startPosition
	 *            '0' means the beginning of the file
	 * @param maxSizeToRead
	 *            max size to read.'-1' means read until the end.
	 * @return
	 */
	public List<Long> searchPartially(File f, byte[] searchBytes, long startPosition, long maxSizeToRead) {
		return searchPartially(f, searchBytes, startPosition, maxSizeToRead, null);
	}

	private List<Long> searchPartially(File f, byte[] searchBytes, long startPosition, long maxSizeToRead, BigFileProgressListener listener) {

		final List<Long> pointerList = new ArrayList<Long>();

		mLoopInprogress = true;

		final BigBinarySearcher bbs = new BigBinarySearcher();

		final boolean hasReadingLimit = (maxSizeToRead > 0);

		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(f, "r");

			final long targetFileSize = raf.length();

			if (startPosition < 0 || startPosition > targetFileSize) {
				throw new RuntimeException("StartPos is invalid.");
			}

			final long endPosition;

			if (hasReadingLimit) {
				if (startPosition + maxSizeToRead > targetFileSize) {
					endPosition = targetFileSize - 1;
				} else {
					endPosition = startPosition + maxSizeToRead - 1;
				}

			} else {
				endPosition = targetFileSize - 1;
			}

			byte byteBuf[] = null;

			long offsetPos = startPosition;

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

				if (hasReadingLimit && ((offsetPos + actualBytesRead) >= endPosition + 1)) {

					// When reading is over compared with the set readingLimit

					final long lValidReadingSize = (endPosition + 1) - offsetPos;

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

				long bytesRemain = (endPosition + 1) - offsetPos;

				if (listener == null) {
					listener = mBigFileProgressListener;
				}
				if (listener != null) {
					float progress = (float) offsetPos / (float) endPosition;
					listener.onProgress(pointerList, progress, offsetPos, startPosition, endPosition);
				}

				if (bytesRemain == byteShiftForSearch) {

					if (listener == null) {
						listener = mBigFileProgressListener;
					}
					if (listener != null) {
						float progress = 1.0f;
						listener.onProgress(pointerList, progress, offsetPos, startPosition, endPosition);
					}

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

		sort(pointerList);

		return pointerList;
	}

	protected void sort(List<Long> list) {

		list.sort(new Comparator<Long>() {

			public int compare(Long num1, Long num2) {
				if (num1 > num2) {
					return 1;
				} else if (num1 < num2) {
					return -1;
				}
				return 0;
			}
		});
	}

	/**
	 * Stop searching bytes
	 */
	public void stop() {
		mLoopInprogress = false;
	}
}
