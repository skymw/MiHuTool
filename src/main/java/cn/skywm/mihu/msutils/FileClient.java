package cn.skywm.mihu.msutils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface FileClient {
    /**
     * 获取指定目录下符合要求的文件列表
     * @param path
     * @param pattern
     * @param recursive 是否包含子目录
     * @return
     * @throws Exception
     */
    List<FileEntry> listFiles(String path, String pattern, boolean recursive) throws Exception;

    /**
     * 下载文件：读取文件的全路径
     * @param path
     * @return
     * @throws Exception
     */
    FileClient get(String path) throws Exception;

    /**
     * 上传文件：写入文件的全路径
     * @param path
     * @param filename
     * @return
     */
    FileClient put(String path, String filename) throws Exception;

    /**
     * 获得输入流
     */
    InputStream getInputStream();

    /**
     * 获得输出流
     */
    OutputStream getOutputStream();

    /**
     * 按行读取数据
     * @return
     * @throws Exception
     */
    String readln() throws Exception;

    /**
     * 上传文件：写入数据
     * @param bytes
     * @return
     */
    FileClient write(byte[] bytes) throws Exception;

    /**
     * 上传文件：写入数据并换行
     * @param bytes
     * @return
     */
    FileClient writeln(byte[] bytes) throws Exception;

    /**
     * 重命名文件
     * @param oldpath
     * @param newpath
     * @return
     * @throws Exception
     */
    FileClient rename(String oldpath, String newpath) throws Exception;

    /**
     * 删除临时文件
     * @return
     * @throws Exception
     */
    FileClient rm();

    /**
     * 删除文件
     * @param path
     * @return
     * @throws Exception
     */
    FileClient rm(String path);

    /**
     * 创建目录
     * @param path
     * @return
     * @throws Exception
     */
    FileClient mkdir(String path);

    /**
     * 判断路径是否存在
     * @param path
     * @return
     */
    boolean isDirExist(String path);

    /**
     * 判断文件是否存在
     * @param path
     * @return
     */
    boolean isFileExist(String path);

    String getFullFileName();

    /**
     * 关闭文件流
     * @throws Exception
     */
    void close();

    /**
     * 释放资源
     * @return
     */
    void dispose();

    class FileEntry {
        private String filename;
        private String fullFilename;
        private long mTime;

        public FileEntry(String filename, long mTime, String fullFilename) {
            this.filename = filename;
            this.mTime = mTime;
            this.fullFilename = fullFilename;
        }

        public String getFilename() {
            return this.filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public long getMTime() {
            return this.mTime;
        }

        public void setMTime(long mTime) {
            this.mTime = mTime;
        }

        public String getFullFilename() {
            return this.fullFilename;
        }

        public void setFullFilename(String fullFilename) {
            this.fullFilename = fullFilename;
        }
    }
}
