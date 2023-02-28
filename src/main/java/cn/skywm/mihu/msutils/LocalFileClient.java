package cn.skywm.mihu.msutils;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.regex.Pattern;

public class LocalFileClient implements FileClient {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedReader reader;

    private static final int BUFFER_SIZE = 4096;
    private static final String TMP_FILE_EXT = ".tmp";

    private String tmpPutFileName = null;        // 临时文件名
    private String fullFileName = null;
    private int totalWriteSize = 0;

    @Override
    public List<FileEntry> listFiles(String path, String pattern, boolean recursive) throws Exception {
        // 存放结果集
        List<FileEntry> files = new ArrayList<>();
        // 存放路径队列
        Queue<String> paths = new LinkedList<>();
        paths.add(path);

        if (!recursive) {
            listFiles(path, pattern, files, paths);
            return files;
        }

        while (!paths.isEmpty()) {
            path = paths.poll();
            listFiles(path, pattern, files, paths);
        }

        return files;
    }

    private void listFiles(String path, String pattern, List<FileEntry> files, Queue<String> paths) throws Exception {
        Pattern p = Pattern.compile(pattern);

        new File(path).listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                String filename = file.getName();
                if (".".equals(filename) || "..".equals(filename)) return false;

                if (file.isDirectory()) {
                    paths.add(file.getName());
                } else {
                    if (!p.matcher(file.getName()).matches()) return false;

                    FileEntry fileInfo = new FileEntry(file.getName(), file.lastModified(), file.getPath());
                    files.add(fileInfo);
                }
                return true;
            }
        });
    }

    @Override
    public FileClient get(String path) throws Exception {
        try {
            inputStream = new FileInputStream(path);
            reader = new BufferedReader(new InputStreamReader(this.inputStream));
        } catch (FileNotFoundException e) {
            logger.error(String.format("get full file name: %s", path), e);
            throw e;
        }
        return this;
    }

    @Override
    public FileClient put(String path, String filename) throws Exception {
        path = path.replace("\\", "/");
        if (!path.endsWith("/"))
            path += "/";

        mkdir(path);

        // 先写入临时文件，在close方法中将文件改为正式名称
        fullFileName = path + filename;
        tmpPutFileName = fullFileName + TMP_FILE_EXT;

        try {
            outputStream = new FileOutputStream(tmpPutFileName, false);
        } catch (FileNotFoundException e) {
            logger.error(String.format("put full file name: %s", tmpPutFileName), e);
            throw e;
        }
        return this;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public String readln() throws Exception {
        return reader.readLine();
    }

    @Override
    public FileClient write(byte[] bytes) throws Exception {
        outputStream.write(bytes);

        totalWriteSize += bytes.length;
        if (totalWriteSize > BUFFER_SIZE) {
            outputStream.flush();
            totalWriteSize = 0;
        }
        return this;
    }

    @Override
    public FileClient writeln(byte[] bytes) throws Exception {
        write(bytes);
        write(System.lineSeparator().getBytes());
        return this;
    }

    @Override
    public FileClient rename(String oldpath, String newpath) throws Exception {
        if (isFileExist(newpath))
            rm(newpath);

        mkdir(new File(newpath).getParent());

        File file = new File(oldpath);
        boolean succeed = file.renameTo(new File(newpath));
        if (!succeed)
            throw new Exception("failed to rename file " + oldpath + " to " + newpath);
        return this;
    }

    @Override
    public FileClient rm() {
        if (!StringUtils.isEmpty(tmpPutFileName)
                && isFileExist(tmpPutFileName)) {
            rm(tmpPutFileName);
        }
        return this;
    }

    @Override
    public FileClient rm(String path) {
        File file = new File(path);
        boolean succeed = file.delete();
        if (!succeed)
            logger.warn("failed to delete file " + path);
        return this;
    }

    @Override
    public FileClient mkdir(String path) {
        File file = new File(path);
        if (!file.exists())
            file.mkdirs();

        return this;
    }

    @Override
    public boolean isDirExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    @Override
    public boolean isFileExist(String path) {
        File file = new File(path);
        return file.exists();
    }

    @Override
    public String getFullFileName() {
        return fullFileName;
    }

    @Override
    public void close() {
        try {
            if (reader != null)
                reader.close();

            if (outputStream != null)
                outputStream.close();

            if (inputStream != null)
                inputStream.close();


            if (!StringUtils.isEmpty(tmpPutFileName)
                    && isFileExist(tmpPutFileName)) {
                String fileName = tmpPutFileName.substring(0, tmpPutFileName.lastIndexOf(TMP_FILE_EXT));
                rename(tmpPutFileName, fileName);
            }
        } catch (Exception e) {
            logger.error("", e);
            rm();
        }
    }

    @Override
    public void dispose() {
        close();
    }
}
