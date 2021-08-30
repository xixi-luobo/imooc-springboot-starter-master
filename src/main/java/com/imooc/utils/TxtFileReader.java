package com.imooc.utils;

import com.imooc.controller.KongHaoController;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.n3r.idworker.utils.JsonTranscoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


public class TxtFileReader {
    final static Logger logger = LoggerFactory.getLogger(KongHaoController.class);

    private int threadSize;
    private String charset;
    private int bufferSize;
    private FileHandler handle;
    private ExecutorService executorService;
    private long fileLength;
    private RandomAccessFile rAccessFile;
    private Set<StartEndPair> startEndPairs;
    private CyclicBarrier cyclicBarrier;
    private AtomicLong counter = new AtomicLong(0);


    private final char CUT_RETURN = '\n';
    private final char CUT_TAB = '\r';

    /**
     * 创建文件解读器
     *
     * @param file
     * @param handler
     * @param charset
     * @param bufferSize
     * @param threadSize
     */
    private TxtFileReader(File file, FileHandler handler, String charset, int bufferSize, int threadSize) throws IOException {
        this.fileLength = file.length();
        this.handle = handler;
        this.charset = charset;
        this.bufferSize = bufferSize;
        this.threadSize = threadSize;
        try {
            this.rAccessFile = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.executorService = Executors.newFixedThreadPool(threadSize);
        startEndPairs = new HashSet<>();
    }

    public void start() {
        //每次处理的数据大小
        long everySize = this.fileLength / this.threadSize;
        try {
            //计算开始和结束的数据
            calculateStartEnd(0, everySize);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        final long startTime = System.currentTimeMillis();
        //对于分片等待
        cyclicBarrier = new CyclicBarrier(startEndPairs.size(), () -> {
            logger.info("解析执行的数量时间{}ms，解析数量为{} : ", System.currentTimeMillis() - startTime, counter.get());
            //移除对应处理完的文件
            //处理不完就等待处理结果
            shutdown();
        });
        for (StartEndPair pair : startEndPairs) {
            this.executorService.execute(new SliceReaderTask(pair));
        }
    }

    /**
     * 计算分块数据
     *
     * @param start
     * @param size
     * @throws IOException
     */
    private void calculateStartEnd(long start, long size) throws IOException {
        //开始大于文件大小
        if (start > fileLength - 1) {
            return;
        }
        StartEndPair pair = new StartEndPair();
        pair.start = start;
        //获得最后位置的地址
        long endPosition = start + size - 1;
        //如果是最后一块数据设置最后一个字节的下标
        if (endPosition >= fileLength - 1) {
            pair.end = fileLength - 1;
            startEndPairs.add(pair);
            return;
        }
        //标记文件指针的位置读取文件
        rAccessFile.seek(endPosition);
        byte tmp = (byte) rAccessFile.read();
        //如果读取的不是换行,需要继续读取直到读取出换行和tap
        while (tmp != CUT_RETURN && tmp != CUT_TAB) {
            endPosition++;
            if (endPosition >= fileLength - 1) {
                endPosition = fileLength - 1;
                break;
            }
            //再次设定指向的位置
            rAccessFile.seek(endPosition);
            tmp = (byte) rAccessFile.read();
        }
        pair.end = endPosition;
        startEndPairs.add(pair);
        //进行下一个数据块计算
        calculateStartEnd(endPosition + 1, size);
    }


    /**
     * 关闭流文件
     */
    public void shutdown() {
        try {
            this.rAccessFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.executorService.shutdown();
    }

    private void handle(byte[] bytes) throws IOException {
        String line = null;
        if (this.charset == null) {
            line = new String(bytes);
        } else {
            line = new String(bytes, charset);
        }
        if (line != null && !"".equals(line)) {
           this.handle.handler(line);
            counter.incrementAndGet();
        }
    }

    /**
     * 开始到结束的
     */
    private static class StartEndPair {
        public long start;
        public long end;

        @Override
        public String toString() {
            return "star=" + start + ";end=" + end;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (end ^ (end >>> 32));
            result = prime * result + (int) (start ^ (start >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StartEndPair other = (StartEndPair) obj;
            if (end != other.end)
                return false;
            if (start != other.start)
                return false;
            return true;
        }

    }

    private class SliceReaderTask implements Runnable {
        private long start;
        private long sliceSize;
        private byte[] readBuff;

        /**
         * @param pair
         */
        public SliceReaderTask(StartEndPair pair) {
            //读取开始的位置
            this.start = pair.start;
            //读取的大小
            this.sliceSize = pair.end - pair.start + 1;
            this.readBuff = new byte[bufferSize];
        }

        @Override
        public void run() {
            try {
                //这里可以先获得某一个固定的RandomAccessFile文件，截取对应的块数据
                MappedByteBuffer mapBuffer = rAccessFile
                        .getChannel()
                        .map(FileChannel.MapMode.READ_ONLY, start, this.sliceSize);
                //创建绑定输出流
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                //初始偏移量
                for (int offset = 0; offset < sliceSize; offset += bufferSize) {
                    //获得下次读取数据的长度
                    int readLength = nextReadLength(offset, bufferSize, sliceSize);
                    //将读取的长度放入缓存区中
                    mapBuffer.get(readBuff, 0, readLength);
                    //
                    int i = 0;
                    while (i < readLength) {
                        //获取对应的字节
                        byte tmp = readBuff[i];
                        if (tmp == CUT_RETURN || tmp == CUT_TAB) {
                            //输出数据
                            handle(bos.toByteArray());
                            //重置输出流
                            bos.reset();
                        } else {
                            bos.write(tmp);
                        }
                        i++;
                    }
                }
                if (bos.size() > 0) {
                    handle(bos.toByteArray());
                }
                cyclicBarrier.await();//测试性能用和等待处理使用
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 下次读取的长度
     *
     * @param offset
     * @param bufferSize
     * @param dataBlockSize
     * @return
     */
    private int nextReadLength(int offset, int bufferSize, long dataBlockSize) {
        int readLength;
        //偏移量的值和缓存块大于数据块的最大位置将读取的长度赋给下次读取的长度
        if (offset + bufferSize <= dataBlockSize) {
            readLength = bufferSize;
        } else {
            //如果小于最大数据块计算下次读取的长度
            readLength = (int) (dataBlockSize - offset);
        }

        return readLength;
    }

    public static class Builder {
        private int threadSize = Runtime.getRuntime().availableProcessors() * 2;
        private String charset = "utf-8";
        private int bufferSize = 1024 * 1024;
        private FileHandler handler;
        private File file;

        public Builder(File file, FileHandler handler) {
            this.file = file;
            if (!this.file.exists())
                throw new IllegalArgumentException("文件不存在！");
            this.handler = handler;
        }

        public Builder withTreahdSize(int size) {
            this.threadSize = size;
            return this;
        }

        public Builder withCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder withBufferSize(int bufferSize) {
            this.bufferSize = bufferSize;
            return this;
        }

        public TxtFileReader build() throws IOException {
            return new TxtFileReader(this.file, this.handler, this.charset, this.bufferSize, this.threadSize);
        }
    }
}
