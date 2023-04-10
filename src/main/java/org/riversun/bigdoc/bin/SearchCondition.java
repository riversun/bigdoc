package org.riversun.bigdoc.bin;

import java.io.File;

import org.riversun.bigdoc.bin.BigFileSearcher.OnProgressListener;
import org.riversun.bigdoc.bin.BigFileSearcher.OnRealtimeResultListener;

public class SearchCondition {
  public File srcFile;
  public byte[] searchBytes;
  public int numOfThreads;
  public boolean useOptimization;
  public long startPosition;
  public long endPosition = -1;// not applicable now
  public OnRealtimeResultListener onRealtimeResultListener;
  public OnProgressListener onProgressListener;
}
