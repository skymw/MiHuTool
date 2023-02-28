package cn.skywm.mihu.msutils;

import com.jcraft.jsch.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;

public class SftpClient implements FileClient {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private ChannelSftp sftp;
    private Session session;

    private OutputStream outputStream;
    private InputStream inputStream;
    private BufferedReader reader;

    private static final int BUFFER_SIZE = 4096;
    private static final int SESSION_TIMEOUT = 60000;
    private static final int MAX_CONNECT_TRIES = 5;
    private static final String TMP_FILE_EXT = ".tmp";

    private String tmpPutFileName = null;        // 临时文件名
    private String fullFileName = null;
    private int totalWriteSize = 0;              // 累计写字节数

    private SftpClient(ChannelSftp sftp, Session session) throws Exception {
        this.sftp = sftp;
        this.session = session;
    }

    public static SftpClient build(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();

        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
//        session.setHostKeyAlias(UUID.randomUUID().toString());

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
//        session.setSocketFactory(new SocketFactoryImpl());

        int tries = 0;
        while (true) {
            try {
                session.connect();
            } catch (Exception e) {
                if (tries < MAX_CONNECT_TRIES) {
//                    logger.warn("session connect[tries:" + tries++ + "] failed.", e);
                    continue;
                }
                throw e;
            }
            break;
        }

        Channel channel = session.openChannel("sftp");
        channel.connect();

        ChannelSftp sftp = (ChannelSftp) channel;
        sftp.setFilenameEncoding("UTF-8");

        return new SftpClient(sftp, session);
    }

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

        sftp.ls(path, new ChannelSftp.LsEntrySelector() {
            public int select(ChannelSftp.LsEntry entry) {
                String filename = entry.getFilename();
                if (".".equals(filename) || "..".equals(filename)) return CONTINUE;

                if (entry.getAttrs().isDir()) {
                    paths.add(path + "/" + filename);
                } else {
                    if (!p.matcher(filename).matches()) return CONTINUE;

                    FileEntry fileInfo = new FileEntry(filename,
                            entry.getAttrs().getMTime() * 1000L,
                            path + "/" + filename);
                    files.add(fileInfo);
                }
                return CONTINUE;
            }
        });
    }

    @Override
    public FileClient get(String path) throws Exception {
        try {
            inputStream = sftp.get(path);
            reader = new BufferedReader(new InputStreamReader(this.inputStream));
        } catch (SftpException e) {
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
            outputStream = sftp.put(tmpPutFileName, ChannelSftp.OVERWRITE);
            totalWriteSize = 0;
        } catch (SftpException e) {
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

        sftp.rename(oldpath, newpath);
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
        try {
            sftp.rm(path);
        } catch (Exception e) {
            logger.error("", e);
        }
        return this;
    }

    @Override
    public FileClient mkdir(String path) {
        if (isDirExist(path))
            return this;

        String[] ss = path.split("/");

        String dir = "";
        for (String s : ss) {
            if (StringUtils.isEmpty(s))
                continue;

            dir += "/" + s;
            if (!isDirExist(dir)) {
                try {
                    sftp.mkdir(dir);
                } catch (Exception ignored) {
                    logger.info("Target file directory has been made");
                }
            }
        }

        return this;
    }

    @Override
    public boolean isDirExist(String path) {
        try {
            SftpATTRS attrs = sftp.lstat(path);
            return attrs.isDir();
        } catch (SftpException e) {
            return false;
        }
    }

    @Override
    public boolean isFileExist(String path) {
        try {
            SftpATTRS attrs = sftp.lstat(path);
            return !attrs.isDir();
        } catch (SftpException e) {
            return false;
        }
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

    public FileClient upload(String path, String filename, InputStream input) throws Exception {
        path = path.replace("\\", "/");
        if (!path.endsWith("/"))
            path += "/";

        mkdir(path);

        // 先写入临时文件
        String tmpFileName = path + filename + TMP_FILE_EXT;

        try {
            sftp.put(input, tmpFileName);
            rename(tmpFileName, path + filename);
        } catch (SftpException e) {
            logger.error(String.format("upload file name: %s", tmpFileName), e);
            throw e;
        }
        return this;
    }

    @Override
    public void dispose() {
        close();

        if (sftp != null
                && sftp.isConnected()) {
            sftp.disconnect();
        }
        if (session != null
                && session.isConnected()) {
            session.disconnect();
        }
    }

    public static class SocketFactoryImpl implements SocketFactory {

        @Override
        public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), SESSION_TIMEOUT);
            return socket;
        }

        @Override
        public InputStream getInputStream(Socket socket) throws IOException {
            return socket.getInputStream();
        }

        @Override
        public OutputStream getOutputStream(Socket socket) throws IOException {
            return socket.getOutputStream();
        }
    }
}
