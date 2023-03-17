package cn.skywm.mihu.common;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

/**
 * @Classname CsvWriter
 * @Description TODO
 * @Version 1.0.0
 * @Date 2023/3/1 10:48
 * @Created by april
 */
public class CsvWriter {
    private Writer outputStream = null;
    private String fileName = null;
    private boolean firstColumn = true;
    private boolean useCustomRecordDelimiter = false;
    private Charset charset = null;
    private UserSettings userSettings = new UserSettings();
    private boolean initialized = false;
    private boolean closed = false;
    private String systemRecordDelimiter = System.getProperty("line.separator");
    public static final int ESCAPE_MODE_DOUBLED = 1;
    public static final int ESCAPE_MODE_BACKSLASH = 2;

    public CsvWriter(String paramString, char paramChar, Charset paramCharset) {
        if (paramString == null) {
            throw new IllegalArgumentException("Parameter fileName can not be null.");
        }
        if (paramCharset == null) {
            throw new IllegalArgumentException("Parameter charset can not be null.");
        }
        this.fileName = paramString;
        this.userSettings.Delimiter = paramChar;
        this.charset = paramCharset;
    }

    public CsvWriter(String paramString) {
        this(paramString, ',', Charset.forName("ISO-8859-1"));
    }

    public CsvWriter(Writer paramWriter, char paramChar) {
        if (paramWriter == null) {
            throw new IllegalArgumentException("Parameter outputStream can not be null.");
        }
        this.outputStream = paramWriter;
        this.userSettings.Delimiter = paramChar;
        this.initialized = true;
    }

    public CsvWriter(OutputStream paramOutputStream, char paramChar, Charset paramCharset) {
        this(new OutputStreamWriter(paramOutputStream, paramCharset), paramChar);
    }

    public char getDelimiter() {
        return this.userSettings.Delimiter;
    }

    public void setDelimiter(char paramChar) {
        this.userSettings.Delimiter = paramChar;
    }

    public char getRecordDelimiter() {
        return this.userSettings.RecordDelimiter;
    }

    public void setRecordDelimiter(char paramChar) {
        this.useCustomRecordDelimiter = true;
        this.userSettings.RecordDelimiter = paramChar;
    }

    public char getTextQualifier() {
        return this.userSettings.TextQualifier;
    }

    public void setTextQualifier(char paramChar) {
        this.userSettings.TextQualifier = paramChar;
    }

    public boolean getUseTextQualifier() {
        return this.userSettings.UseTextQualifier;
    }

    public void setUseTextQualifier(boolean paramBoolean) {
        this.userSettings.UseTextQualifier = paramBoolean;
    }

    public int getEscapeMode() {
        return this.userSettings.EscapeMode;
    }

    public void setEscapeMode(int paramInt) {
        this.userSettings.EscapeMode = paramInt;
    }

    public void setComment(char paramChar) {
        this.userSettings.Comment = paramChar;
    }

    public char getComment() {
        return this.userSettings.Comment;
    }

    public boolean getForceQualifier() {
        return this.userSettings.ForceQualifier;
    }

    public void setForceQualifier(boolean paramBoolean) {
        this.userSettings.ForceQualifier = paramBoolean;
    }

    public void write(String paramString, boolean paramBoolean)
            throws IOException {
        checkClosed();
        checkInit();
        if (paramString == null) {
            paramString = "";
        }
        if (!this.firstColumn) {
            this.outputStream.write(this.userSettings.Delimiter);
        }
        boolean bool = this.userSettings.ForceQualifier;
        if ((!paramBoolean) && (paramString.length() > 0)) {
            paramString = paramString.trim();
        }
        if ((!bool) && (this.userSettings.UseTextQualifier) && ((paramString.indexOf(this.userSettings.TextQualifier) > -1) || (paramString.indexOf(this.userSettings.Delimiter) > -1) || ((!this.useCustomRecordDelimiter) && ((paramString.indexOf('\n') > -1) || (paramString.indexOf('\r') > -1))) || ((this.useCustomRecordDelimiter) && (paramString.indexOf(this.userSettings.RecordDelimiter) > -1)) || ((this.firstColumn) && (paramString.length() > 0) && (paramString.charAt(0) == this.userSettings.Comment)) || ((this.firstColumn) && (paramString.length() == 0)))) {
            bool = true;
        }
        if ((this.userSettings.UseTextQualifier) && (!bool) && (paramString.length() > 0) && (paramBoolean)) {
            int i = paramString.charAt(0);
            if ((i == 32) || (i == 9)) {
                bool = true;
            }
            if ((!bool) && (paramString.length() > 1)) {
                int j = paramString.charAt(paramString.length() - 1);
                if ((j == 32) || (j == 9)) {
                    bool = true;
                }
            }
        }
        if (bool) {
            this.outputStream.write(this.userSettings.TextQualifier);
            if (this.userSettings.EscapeMode == 2) {
                paramString = replace(paramString, "\\", "\\\\");
                paramString = replace(paramString, "" + this.userSettings.TextQualifier, "\\" + this.userSettings.TextQualifier);
            } else {
                paramString = replace(paramString, "" + this.userSettings.TextQualifier, "" + this.userSettings.TextQualifier + this.userSettings.TextQualifier);
            }
        } else if (this.userSettings.EscapeMode == 2) {
            paramString = replace(paramString, "\\", "\\\\");
            paramString = replace(paramString, "" + this.userSettings.Delimiter, "\\" + this.userSettings.Delimiter);
            if (this.useCustomRecordDelimiter) {
                paramString = replace(paramString, "" + this.userSettings.RecordDelimiter, "\\" + this.userSettings.RecordDelimiter);
            } else {
                paramString = replace(paramString, "\r", "\\\r");
                paramString = replace(paramString, "\n", "\\\n");
            }
            if ((this.firstColumn) && (paramString.length() > 0) && (paramString.charAt(0) == this.userSettings.Comment)) {
                if (paramString.length() > 1) {
                    paramString = "\\" + this.userSettings.Comment + paramString.substring(1);
                } else {
                    paramString = "\\" + this.userSettings.Comment;
                }
            }
        }
        this.outputStream.write(paramString);
        if (bool) {
            this.outputStream.write(this.userSettings.TextQualifier);
        }
        this.firstColumn = false;
    }

    public void write(String paramString)
            throws IOException {
        write(paramString, false);
    }

    public void writeComment(String paramString)
            throws IOException {
        checkClosed();
        checkInit();
        this.outputStream.write(this.userSettings.Comment);
        this.outputStream.write(paramString);
        if (this.useCustomRecordDelimiter) {
            this.outputStream.write(this.userSettings.RecordDelimiter);
        } else {
            this.outputStream.write(this.systemRecordDelimiter);
        }
        this.firstColumn = true;
    }

    public void writeRecord(String[] paramArrayOfString, boolean paramBoolean)
            throws IOException {
        if ((paramArrayOfString != null) && (paramArrayOfString.length > 0)) {
            for (int i = 0; i < paramArrayOfString.length; i++) {
                write(paramArrayOfString[i], paramBoolean);
            }
            endRecord();
        }
    }

    public void writeRecord(String[] paramArrayOfString)
            throws IOException {
        writeRecord(paramArrayOfString, false);
    }

    public void endRecord()
            throws IOException {
        checkClosed();
        checkInit();
        if (this.useCustomRecordDelimiter) {
            this.outputStream.write(this.userSettings.RecordDelimiter);
        } else {
            this.outputStream.write(this.systemRecordDelimiter);
        }
        this.firstColumn = true;
    }

    private void checkInit()
            throws IOException {
        if (!this.initialized) {
            if (this.fileName != null) {
                this.outputStream = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(this.fileName), this.charset));
            }
            this.initialized = true;
        }
    }

    public void flush()
            throws IOException {
        this.outputStream.flush();
    }

    public void close() {
        if (!this.closed) {
            close(true);
            this.closed = true;
        }
    }

    private void close(boolean paramBoolean) {
        if (!this.closed) {
            if (paramBoolean) {
                this.charset = null;
            }
            try {
                if (this.initialized) {
                    this.outputStream.close();
                }
            } catch (Exception localException) {
            }
            this.outputStream = null;
            this.closed = true;
        }
    }

    private void checkClosed()
            throws IOException {
        if (this.closed) {
            throw new IOException("This instance of the CsvWriter class has already been closed.");
        }
    }

    protected void finalize() {
        close(false);
    }

    public static String replace(String paramString1, String paramString2, String paramString3) {
        int i = paramString2.length();
        int j = paramString1.indexOf(paramString2);
        if (j > -1) {
            StringBuffer localStringBuffer = new StringBuffer();
            int k = 0;
            while (j != -1) {
                localStringBuffer.append(paramString1.substring(k, j));
                localStringBuffer.append(paramString3);
                k = j + i;
                j = paramString1.indexOf(paramString2, k);
            }
            localStringBuffer.append(paramString1.substring(k));
            return localStringBuffer.toString();
        }
        return paramString1;
    }

    private class UserSettings {
        public char TextQualifier = '"';
        public boolean UseTextQualifier = true;
        public char Delimiter = ',';
        public char RecordDelimiter = '\000';
        public char Comment = '#';
        public int EscapeMode = 1;
        public boolean ForceQualifier = false;

        public UserSettings() {
        }
    }

    private class Letters {
        public static final char LF = '\n';
        public static final char CR = '\r';
        public static final char QUOTE = '"';
        public static final char COMMA = ',';
        public static final char SPACE = ' ';
        public static final char TAB = '\t';
        public static final char POUND = '#';
        public static final char BACKSLASH = '\\';
        public static final char NULL = '\000';

        private Letters() {
        }
    }

}
