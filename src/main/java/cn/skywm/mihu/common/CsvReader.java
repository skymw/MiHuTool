package cn.skywm.mihu.common;

import java.io.*;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.HashMap;

/**
 * @Classname CsvReader
 * @Description TODO
 * @Version 1.0.0
 * @Date 2023/3/1 10:50
 * @Created by april
 */
public class CsvReader {
    private Reader inputStream = null;
    private String fileName = null;
    private UserSettings userSettings = new UserSettings();
    private Charset charset = null;
    private boolean useCustomRecordDelimiter = false;
    private DataBuffer dataBuffer = new DataBuffer();
    private ColumnBuffer columnBuffer = new ColumnBuffer();
    private RawRecordBuffer rawBuffer = new RawRecordBuffer();
    private boolean[] isQualified = null;
    private String rawRecord = "";
    private HeadersHolder headersHolder = new HeadersHolder();
    private boolean startedColumn = false;
    private boolean startedWithQualifier = false;
    private boolean hasMoreData = true;
    private char lastLetter = '\000';
    private boolean hasReadNextLine = false;
    private int columnsCount = 0;
    private long currentRecord = 0L;
    private String[] values = new String[10];
    private boolean initialized = false;
    private boolean closed = false;
    public static final int ESCAPE_MODE_DOUBLED = 1;
    public static final int ESCAPE_MODE_BACKSLASH = 2;

    public CsvReader(String paramString, char paramChar, Charset paramCharset)
            throws FileNotFoundException
    {
        if (paramString == null) {
            throw new IllegalArgumentException("Parameter fileName can not be null.");
        }
        if (paramCharset == null) {
            throw new IllegalArgumentException("Parameter charset can not be null.");
        }
        if (!new File(paramString).exists()) {
            throw new FileNotFoundException("File " + paramString + " does not exist.");
        }
        this.fileName = paramString;
        this.userSettings.Delimiter = paramChar;
        this.charset = paramCharset;
        this.isQualified = new boolean[this.values.length];
    }

    public CsvReader(String paramString, char paramChar)
            throws FileNotFoundException
    {
        this(paramString, paramChar, Charset.forName("ISO-8859-1"));
    }

    public CsvReader(String paramString)
            throws FileNotFoundException
    {
        this(paramString, ',');
    }

    public CsvReader(Reader paramReader, char paramChar)
    {
        if (paramReader == null) {
            throw new IllegalArgumentException("Parameter inputStream can not be null.");
        }
        this.inputStream = paramReader;
        this.userSettings.Delimiter = paramChar;
        this.initialized = true;
        this.isQualified = new boolean[this.values.length];
    }

    public CsvReader(Reader paramReader)
    {
        this(paramReader, ',');
    }

    public CsvReader(InputStream paramInputStream, char paramChar, Charset paramCharset)
    {
        this(new InputStreamReader(paramInputStream, paramCharset), paramChar);
    }

    public CsvReader(InputStream paramInputStream, Charset paramCharset)
    {
        this(new InputStreamReader(paramInputStream, paramCharset));
    }

    public boolean getCaptureRawRecord()
    {
        return this.userSettings.CaptureRawRecord;
    }

    public void setCaptureRawRecord(boolean paramBoolean)
    {
        this.userSettings.CaptureRawRecord = paramBoolean;
    }

    public String getRawRecord()
    {
        return this.rawRecord;
    }

    public boolean getTrimWhitespace()
    {
        return this.userSettings.TrimWhitespace;
    }

    public void setTrimWhitespace(boolean paramBoolean)
    {
        this.userSettings.TrimWhitespace = paramBoolean;
    }

    public char getDelimiter()
    {
        return this.userSettings.Delimiter;
    }

    public void setDelimiter(char paramChar)
    {
        this.userSettings.Delimiter = paramChar;
    }

    public char getRecordDelimiter()
    {
        return this.userSettings.RecordDelimiter;
    }

    public void setRecordDelimiter(char paramChar)
    {
        this.useCustomRecordDelimiter = true;
        this.userSettings.RecordDelimiter = paramChar;
    }

    public char getTextQualifier()
    {
        return this.userSettings.TextQualifier;
    }

    public void setTextQualifier(char paramChar)
    {
        this.userSettings.TextQualifier = paramChar;
    }

    public boolean getUseTextQualifier()
    {
        return this.userSettings.UseTextQualifier;
    }

    public void setUseTextQualifier(boolean paramBoolean)
    {
        this.userSettings.UseTextQualifier = paramBoolean;
    }

    public char getComment()
    {
        return this.userSettings.Comment;
    }

    public void setComment(char paramChar)
    {
        this.userSettings.Comment = paramChar;
    }

    public boolean getUseComments()
    {
        return this.userSettings.UseComments;
    }

    public void setUseComments(boolean paramBoolean)
    {
        this.userSettings.UseComments = paramBoolean;
    }

    public int getEscapeMode()
    {
        return this.userSettings.EscapeMode;
    }

    public void setEscapeMode(int paramInt)
            throws IllegalArgumentException
    {
        if ((paramInt != 1) && (paramInt != 2)) {
            throw new IllegalArgumentException("Parameter escapeMode must be a valid value.");
        }
        this.userSettings.EscapeMode = paramInt;
    }

    public boolean getSkipEmptyRecords()
    {
        return this.userSettings.SkipEmptyRecords;
    }

    public void setSkipEmptyRecords(boolean paramBoolean)
    {
        this.userSettings.SkipEmptyRecords = paramBoolean;
    }

    public boolean getSafetySwitch()
    {
        return this.userSettings.SafetySwitch;
    }

    public void setSafetySwitch(boolean paramBoolean)
    {
        this.userSettings.SafetySwitch = paramBoolean;
    }

    public int getColumnCount()
    {
        return this.columnsCount;
    }

    public long getCurrentRecord()
    {
        return this.currentRecord - 1L;
    }

    public int getHeaderCount()
    {
        return this.headersHolder.Length;
    }

    public String[] getHeaders()
            throws IOException
    {
        checkClosed();
        if (this.headersHolder.Headers == null) {
            return null;
        }
        String[] arrayOfString = new String[this.headersHolder.Length];
        System.arraycopy(this.headersHolder.Headers, 0, arrayOfString, 0, this.headersHolder.Length);
        return arrayOfString;
    }

    public void setHeaders(String[] paramArrayOfString)
    {
        this.headersHolder.Headers = paramArrayOfString;
        this.headersHolder.IndexByName.clear();
        if (paramArrayOfString != null) {
            this.headersHolder.Length = paramArrayOfString.length;
        } else {
            this.headersHolder.Length = 0;
        }
        for (int i = 0; i < this.headersHolder.Length; i++) {
            this.headersHolder.IndexByName.put(paramArrayOfString[i], new Integer(i));
        }
    }

    public String[] getValues()
            throws IOException
    {
        checkClosed();
        String[] arrayOfString = new String[this.columnsCount];
        System.arraycopy(this.values, 0, arrayOfString, 0, this.columnsCount);
        return arrayOfString;
    }

    public String get(int paramInt)
            throws IOException
    {
        checkClosed();
        if ((paramInt > -1) && (paramInt < this.columnsCount)) {
            return this.values[paramInt];
        }
        return "";
    }

    public String get(String paramString)
            throws IOException
    {
        checkClosed();
        return get(getIndex(paramString));
    }

    public static CsvReader parse(String paramString)
    {
        if (paramString == null) {
            throw new IllegalArgumentException("Parameter data can not be null.");
        }
        return new CsvReader(new StringReader(paramString));
    }

    public boolean readRecord()
            throws IOException
    {
        checkClosed();
        this.columnsCount = 0;
        this.rawBuffer.Position = 0;
        this.dataBuffer.LineStart = this.dataBuffer.Position;
        this.hasReadNextLine = false;
        if (this.hasMoreData)
        {
            do
            {
                if (this.dataBuffer.Position == this.dataBuffer.Count)
                {
                    checkDataLength();
                }
                else
                {
                    this.startedWithQualifier = false;
                    char c1 = this.dataBuffer.Buffer[this.dataBuffer.Position];
                    int i;
                    char c2;
                    int j;
                    int k;
                    char c3;
                    int m;
                    if ((this.userSettings.UseTextQualifier) && (c1 == this.userSettings.TextQualifier))
                    {
                        this.lastLetter = c1;
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                        this.startedWithQualifier = true;
                        i = 0;
                        c2 = this.userSettings.TextQualifier;
                        if (this.userSettings.EscapeMode == 2) {
                            c2 = '\\';
                        }
                        j = 0;
                        k = 0;
                        c3 = '\000';
                        m = 1;
                        int n = 0;
                        char c4 = '\000';
                        this.dataBuffer.Position += 1;
                        do
                        {
                            if (this.dataBuffer.Position == this.dataBuffer.Count)
                            {
                                checkDataLength();
                            }
                            else
                            {
                                c1 = this.dataBuffer.Buffer[this.dataBuffer.Position];
                                if (j != 0)
                                {
                                    this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                                    if (c1 == this.userSettings.Delimiter)
                                    {
                                        endColumn();
                                    }
                                    else if (((!this.useCustomRecordDelimiter) && ((c1 == '\r') || (c1 == '\n'))) || ((this.useCustomRecordDelimiter) && (c1 == this.userSettings.RecordDelimiter)))
                                    {
                                        endColumn();
                                        endRecord();
                                    }
                                }
                                else if (c3 != 0)
                                {
                                    n++;
                                    switch (m)
                                    {
                                        case 1:
                                            c4 = (char)(c4 * '\020');
                                            c4 = (char)(c4 + hexToDec(c1));
                                            if (n == 4) {
                                                c3 = '\000';
                                            }
                                            break;
                                        case 2:
                                            c4 = (char)(c4 * '\b');
                                            c4 = (char)(c4 + (char)(c1 - '0'));
                                            if (n == 3) {
                                                c3 = '\000';
                                            }
                                            break;
                                        case 3:
                                            c4 = (char)(c4 * '\n');
                                            c4 = (char)(c4 + (char)(c1 - '0'));
                                            if (n == 3) {
                                                c3 = '\000';
                                            }
                                            break;
                                        case 4:
                                            c4 = (char)(c4 * '\020');
                                            c4 = (char)(c4 + hexToDec(c1));
                                            if (n == 2) {
                                                c3 = '\000';
                                            }
                                            break;
                                    }
                                    if (c3 == 0) {
                                        appendLetter(c4);
                                    } else {
                                        this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                                    }
                                }
                                else if (c1 == this.userSettings.TextQualifier)
                                {
                                    if (k != 0)
                                    {
                                        k = 0;
                                        i = 0;
                                    }
                                    else
                                    {
                                        updateCurrentValue();
                                        if (this.userSettings.EscapeMode == 1) {
                                            k = 1;
                                        }
                                        i = 1;
                                    }
                                }
                                else if ((this.userSettings.EscapeMode == 2) && (k != 0))
                                {
                                    switch (c1)
                                    {
                                        case 'n':
                                            appendLetter('\n');
                                            break;
                                        case 'r':
                                            appendLetter('\r');
                                            break;
                                        case 't':
                                            appendLetter('\t');
                                            break;
                                        case 'b':
                                            appendLetter('\b');
                                            break;
                                        case 'f':
                                            appendLetter('\f');
                                            break;
                                        case 'e':
                                            appendLetter('\033');
                                            break;
                                        case 'v':
                                            appendLetter('\013');
                                            break;
                                        case 'a':
                                            appendLetter('\007');
                                            break;
                                        case '0':
                                        case '1':
                                        case '2':
                                        case '3':
                                        case '4':
                                        case '5':
                                        case '6':
                                        case '7':
                                            m = 2;
                                            c3 = '\001';
                                            n = 1;
                                            c4 = (char)(c1 - '0');
                                            this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                                            break;
                                        case 'D':
                                        case 'O':
                                        case 'U':
                                        case 'X':
                                        case 'd':
                                        case 'o':
                                        case 'u':
                                        case 'x':
                                            switch (c1)
                                            {
                                                case 'U':
                                                case 'u':
                                                    m = 1;
                                                    break;
                                                case 'X':
                                                case 'x':
                                                    m = 4;
                                                    break;
                                                case 'O':
                                                case 'o':
                                                    m = 2;
                                                    break;
                                                case 'D':
                                                case 'd':
                                                    m = 3;
                                            }
                                            c3 = '\001';
                                            n = 0;
                                            c4 = '\000';
                                            this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                                            break;
                                    }
                                    k = 0;
                                }
                                else if (c1 == c2)
                                {
                                    updateCurrentValue();
                                    k = 1;
                                }
                                else if (i != 0)
                                {
                                    if (c1 == this.userSettings.Delimiter)
                                    {
                                        endColumn();
                                    }
                                    else if (((!this.useCustomRecordDelimiter) && ((c1 == '\r') || (c1 == '\n'))) || ((this.useCustomRecordDelimiter) && (c1 == this.userSettings.RecordDelimiter)))
                                    {
                                        endColumn();
                                        endRecord();
                                    }
                                    else
                                    {
                                        this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                                        j = 1;
                                    }
                                    i = 0;
                                }
                                this.lastLetter = c1;
                                if (this.startedColumn)
                                {
                                    this.dataBuffer.Position += 1;
                                    if ((this.userSettings.SafetySwitch) && (this.dataBuffer.Position - this.dataBuffer.ColumnStart + this.columnBuffer.Position > 100000))
                                    {
                                        close();
                                        throw new IOException("Maximum column length of 100,000 exceeded in column " + NumberFormat.getIntegerInstance().format(this.columnsCount) + " in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting column lengths greater than 100,000 characters to" + " avoid this error.");
                                    }
                                }
                            }
                        } while ((this.hasMoreData) && (this.startedColumn));
                    }
                    else if (c1 == this.userSettings.Delimiter)
                    {
                        this.lastLetter = c1;
                        endColumn();
                    }
                    else if ((this.useCustomRecordDelimiter) && (c1 == this.userSettings.RecordDelimiter))
                    {
                        if ((this.startedColumn) || (this.columnsCount > 0) || (!this.userSettings.SkipEmptyRecords))
                        {
                            endColumn();
                            endRecord();
                        }
                        else
                        {
                            this.dataBuffer.LineStart = (this.dataBuffer.Position + 1);
                        }
                        this.lastLetter = c1;
                    }
                    else if ((!this.useCustomRecordDelimiter) && ((c1 == '\r') || (c1 == '\n')))
                    {
                        if ((this.startedColumn) || (this.columnsCount > 0) || ((!this.userSettings.SkipEmptyRecords) && ((c1 == '\r') || (this.lastLetter != '\r'))))
                        {
                            endColumn();
                            endRecord();
                        }
                        else
                        {
                            this.dataBuffer.LineStart = (this.dataBuffer.Position + 1);
                        }
                        this.lastLetter = c1;
                    }
                    else if ((this.userSettings.UseComments) && (this.columnsCount == 0) && (c1 == this.userSettings.Comment))
                    {
                        this.lastLetter = c1;
                        skipLine();
                    }
                    else if ((this.userSettings.TrimWhitespace) && ((c1 == ' ') || (c1 == '\t')))
                    {
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                    }
                    else
                    {
                        this.startedColumn = true;
                        this.dataBuffer.ColumnStart = this.dataBuffer.Position;
                        i = 0;
                        c2 = '\000';
                        j = 1;
                        k = 0;
                        c3 = '\000';
                        m = 1;
                        do
                        {
                            if ((m == 0) && (this.dataBuffer.Position == this.dataBuffer.Count))
                            {
                                checkDataLength();
                            }
                            else
                            {
                                if (m == 0) {
                                    c1 = this.dataBuffer.Buffer[this.dataBuffer.Position];
                                }
                                if ((!this.userSettings.UseTextQualifier) && (this.userSettings.EscapeMode == 2) && (c1 == '\\'))
                                {
                                    if (i != 0)
                                    {
                                        i = 0;
                                    }
                                    else
                                    {
                                        updateCurrentValue();
                                        i = 1;
                                    }
                                }
                                else if (c2 != 0)
                                {
                                    k++;
                                    switch (j)
                                    {
                                        case 1:
                                            c3 = (char)(c3 * '\020');
                                            c3 = (char)(c3 + hexToDec(c1));
                                            if (k == 4) {
                                                c2 = '\000';
                                            }
                                            break;
                                        case 2:
                                            c3 = (char)(c3 * '\b');
                                            c3 = (char)(c3 + (char)(c1 - '0'));
                                            if (k == 3) {
                                                c2 = '\000';
                                            }
                                            break;
                                        case 3:
                                            c3 = (char)(c3 * '\n');
                                            c3 = (char)(c3 + (char)(c1 - '0'));
                                            if (k == 3) {
                                                c2 = '\000';
                                            }
                                            break;
                                        case 4:
                                            c3 = (char)(c3 * '\020');
                                            c3 = (char)(c3 + hexToDec(c1));
                                            if (k == 2) {
                                                c2 = '\000';
                                            }
                                            break;
                                    }
                                    if (c2 == 0) {
                                        appendLetter(c3);
                                    } else {
                                        this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                                    }
                                }
                                else if ((this.userSettings.EscapeMode == 2) && (i != 0))
                                {
                                    switch (c1)
                                    {
                                        case 'n':
                                            appendLetter('\n');
                                            break;
                                        case 'r':
                                            appendLetter('\r');
                                            break;
                                        case 't':
                                            appendLetter('\t');
                                            break;
                                        case 'b':
                                            appendLetter('\b');
                                            break;
                                        case 'f':
                                            appendLetter('\f');
                                            break;
                                        case 'e':
                                            appendLetter('\033');
                                            break;
                                        case 'v':
                                            appendLetter('\013');
                                            break;
                                        case 'a':
                                            appendLetter('\007');
                                            break;
                                        case '0':
                                        case '1':
                                        case '2':
                                        case '3':
                                        case '4':
                                        case '5':
                                        case '6':
                                        case '7':
                                            j = 2;
                                            c2 = '\001';
                                            k = 1;
                                            c3 = (char)(c1 - '0');
                                            this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                                            break;
                                        case 'D':
                                        case 'O':
                                        case 'U':
                                        case 'X':
                                        case 'd':
                                        case 'o':
                                        case 'u':
                                        case 'x':
                                            switch (c1)
                                            {
                                                case 'U':
                                                case 'u':
                                                    j = 1;
                                                    break;
                                                case 'X':
                                                case 'x':
                                                    j = 4;
                                                    break;
                                                case 'O':
                                                case 'o':
                                                    j = 2;
                                                    break;
                                                case 'D':
                                                case 'd':
                                                    j = 3;
                                            }
                                            c2 = '\001';
                                            k = 0;
                                            c3 = '\000';
                                            this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
                                            break;
                                    }
                                    i = 0;
                                }
                                else if (c1 == this.userSettings.Delimiter)
                                {
                                    endColumn();
                                }
                                else if (((!this.useCustomRecordDelimiter) && ((c1 == '\r') || (c1 == '\n'))) || ((this.useCustomRecordDelimiter) && (c1 == this.userSettings.RecordDelimiter)))
                                {
                                    endColumn();
                                    endRecord();
                                }
                                this.lastLetter = c1;
                                m = 0;
                                if (this.startedColumn)
                                {
                                    this.dataBuffer.Position += 1;
                                    if ((this.userSettings.SafetySwitch) && (this.dataBuffer.Position - this.dataBuffer.ColumnStart + this.columnBuffer.Position > 100000))
                                    {
                                        close();
                                        throw new IOException("Maximum column length of 100,000 exceeded in column " + NumberFormat.getIntegerInstance().format(this.columnsCount) + " in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting column lengths greater than 100,000 characters to" + " avoid this error.");
                                    }
                                }
                            }
                        } while ((this.hasMoreData) && (this.startedColumn));
                    }
                    if (this.hasMoreData) {
                        this.dataBuffer.Position += 1;
                    }
                }
            } while ((this.hasMoreData) && (!this.hasReadNextLine));
            if ((this.startedColumn) || (this.lastLetter == this.userSettings.Delimiter))
            {
                endColumn();
                endRecord();
            }
        }
        if (this.userSettings.CaptureRawRecord)
        {
            if (this.hasMoreData)
            {
                if (this.rawBuffer.Position == 0) {
                    this.rawRecord = new String(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.dataBuffer.Position - this.dataBuffer.LineStart - 1);
                } else {
                    this.rawRecord = (new String(this.rawBuffer.Buffer, 0, this.rawBuffer.Position) + new String(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.dataBuffer.Position - this.dataBuffer.LineStart - 1));
                }
            }
            else {
                this.rawRecord = new String(this.rawBuffer.Buffer, 0, this.rawBuffer.Position);
            }
        }
        else {
            this.rawRecord = "";
        }
        return this.hasReadNextLine;
    }

    private void checkDataLength()
            throws IOException
    {
        if (!this.initialized)
        {
            if (this.fileName != null) {
                this.inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(this.fileName), this.charset), 4096);
            }
            this.charset = null;
            this.initialized = true;
        }
        updateCurrentValue();
        if ((this.userSettings.CaptureRawRecord) && (this.dataBuffer.Count > 0))
        {
            if (this.rawBuffer.Buffer.length - this.rawBuffer.Position < this.dataBuffer.Count - this.dataBuffer.LineStart)
            {
                int i = this.rawBuffer.Buffer.length + Math.max(this.dataBuffer.Count - this.dataBuffer.LineStart, this.rawBuffer.Buffer.length);
                char[] arrayOfChar = new char[i];
                System.arraycopy(this.rawBuffer.Buffer, 0, arrayOfChar, 0, this.rawBuffer.Position);
                this.rawBuffer.Buffer = arrayOfChar;
            }
            System.arraycopy(this.dataBuffer.Buffer, this.dataBuffer.LineStart, this.rawBuffer.Buffer, this.rawBuffer.Position, this.dataBuffer.Count - this.dataBuffer.LineStart);
            this.rawBuffer.Position += this.dataBuffer.Count - this.dataBuffer.LineStart;
        }
        try
        {
            this.dataBuffer.Count = this.inputStream.read(this.dataBuffer.Buffer, 0, this.dataBuffer.Buffer.length);
        }
        catch (IOException localIOException)
        {
            close();
            throw localIOException;
        }
        if (this.dataBuffer.Count == -1) {
            this.hasMoreData = false;
        }
        this.dataBuffer.Position = 0;
        this.dataBuffer.LineStart = 0;
        this.dataBuffer.ColumnStart = 0;
    }

    public boolean readHeaders()
            throws IOException
    {
        boolean bool = readRecord();
        this.headersHolder.Length = this.columnsCount;
        this.headersHolder.Headers = new String[this.columnsCount];
        for (int i = 0; i < this.headersHolder.Length; i++)
        {
            String str = get(i);
            this.headersHolder.Headers[i] = str;
            this.headersHolder.IndexByName.put(str, new Integer(i));
        }
        if (bool) {
            this.currentRecord -= 1L;
        }
        this.columnsCount = 0;
        return bool;
    }

    public String getHeader(int paramInt)
            throws IOException
    {
        checkClosed();
        if ((paramInt > -1) && (paramInt < this.headersHolder.Length)) {
            return this.headersHolder.Headers[paramInt];
        }
        return "";
    }

    public boolean isQualified(int paramInt)
            throws IOException
    {
        checkClosed();
        if ((paramInt < this.columnsCount) && (paramInt > -1)) {
            return this.isQualified[paramInt];
        }
        return false;
    }

    private void endColumn()
            throws IOException
    {
        String str = "";
        int i;
        if (this.startedColumn) {
            if (this.columnBuffer.Position == 0)
            {
                if (this.dataBuffer.ColumnStart < this.dataBuffer.Position)
                {
                    i = this.dataBuffer.Position - 1;
                    if ((this.userSettings.TrimWhitespace) && (!this.startedWithQualifier)) {
                        while ((i >= this.dataBuffer.ColumnStart) && ((this.dataBuffer.Buffer[i] == ' ') || (this.dataBuffer.Buffer[i] == '\t'))) {
                            i--;
                        }
                    }
                    str = new String(this.dataBuffer.Buffer, this.dataBuffer.ColumnStart, i - this.dataBuffer.ColumnStart + 1);
                }
            }
            else
            {
                updateCurrentValue();
                i = this.columnBuffer.Position - 1;
                if ((this.userSettings.TrimWhitespace) && (!this.startedWithQualifier)) {
                    while ((i >= 0) && ((this.columnBuffer.Buffer[i] == ' ') || (this.columnBuffer.Buffer[i] == ' '))) {
                        i--;
                    }
                }
                str = new String(this.columnBuffer.Buffer, 0, i + 1);
            }
        }
        this.columnBuffer.Position = 0;
        this.startedColumn = false;
        if ((this.columnsCount >= 100000) && (this.userSettings.SafetySwitch))
        {
            close();
            throw new IOException("Maximum column count of 100,000 exceeded in record " + NumberFormat.getIntegerInstance().format(this.currentRecord) + ". Set the SafetySwitch property to false" + " if you're expecting more than 100,000 columns per record to" + " avoid this error.");
        }
        if (this.columnsCount == this.values.length)
        {
            i = this.values.length * 2;
            String[] arrayOfString = new String[i];
            System.arraycopy(this.values, 0, arrayOfString, 0, this.values.length);
            this.values = arrayOfString;
            boolean[] arrayOfBoolean = new boolean[i];
            System.arraycopy(this.isQualified, 0, arrayOfBoolean, 0, this.isQualified.length);
            this.isQualified = arrayOfBoolean;
        }
        this.values[this.columnsCount] = str;
        this.isQualified[this.columnsCount] = this.startedWithQualifier;
        str = "";
        this.columnsCount += 1;
    }

    private void appendLetter(char paramChar)
    {
        if (this.columnBuffer.Position == this.columnBuffer.Buffer.length)
        {
            int i = this.columnBuffer.Buffer.length * 2;
            char[] arrayOfChar = new char[i];
            System.arraycopy(this.columnBuffer.Buffer, 0, arrayOfChar, 0, this.columnBuffer.Position);
            this.columnBuffer.Buffer = arrayOfChar;
        }
        this.columnBuffer.Buffer[(this.columnBuffer.Position++)] = paramChar;
        this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
    }

    private void updateCurrentValue()
    {
        if ((this.startedColumn) && (this.dataBuffer.ColumnStart < this.dataBuffer.Position))
        {
            if (this.columnBuffer.Buffer.length - this.columnBuffer.Position < this.dataBuffer.Position - this.dataBuffer.ColumnStart)
            {
                int i = this.columnBuffer.Buffer.length + Math.max(this.dataBuffer.Position - this.dataBuffer.ColumnStart, this.columnBuffer.Buffer.length);
                char[] arrayOfChar = new char[i];
                System.arraycopy(this.columnBuffer.Buffer, 0, arrayOfChar, 0, this.columnBuffer.Position);
                this.columnBuffer.Buffer = arrayOfChar;
            }
            System.arraycopy(this.dataBuffer.Buffer, this.dataBuffer.ColumnStart, this.columnBuffer.Buffer, this.columnBuffer.Position, this.dataBuffer.Position - this.dataBuffer.ColumnStart);
            this.columnBuffer.Position += this.dataBuffer.Position - this.dataBuffer.ColumnStart;
        }
        this.dataBuffer.ColumnStart = (this.dataBuffer.Position + 1);
    }

    private void endRecord()
            throws IOException
    {
        this.hasReadNextLine = true;
        this.currentRecord += 1L;
    }

    public int getIndex(String paramString)
            throws IOException
    {
        checkClosed();
        Object localObject = this.headersHolder.IndexByName.get(paramString);
        if (localObject != null) {
            return ((Integer)localObject).intValue();
        }
        return -1;
    }

    public boolean skipRecord()
            throws IOException
    {
        checkClosed();
        boolean bool = false;
        if (this.hasMoreData)
        {
            bool = readRecord();
            if (bool) {
                this.currentRecord -= 1L;
            }
        }
        return bool;
    }

    public boolean skipLine()
            throws IOException
    {
        checkClosed();
        this.columnsCount = 0;
        boolean bool = false;
        if (this.hasMoreData)
        {
            int i = 0;
            do
            {
                if (this.dataBuffer.Position == this.dataBuffer.Count)
                {
                    checkDataLength();
                }
                else
                {
                    bool = true;
                    char c = this.dataBuffer.Buffer[this.dataBuffer.Position];
                    if ((c == '\r') || (c == '\n')) {
                        i = 1;
                    }
                    this.lastLetter = c;
                    if (i == 0) {
                        this.dataBuffer.Position += 1;
                    }
                }
            } while ((this.hasMoreData) && (i == 0));
            this.columnBuffer.Position = 0;
            this.dataBuffer.LineStart = (this.dataBuffer.Position + 1);
        }
        this.rawBuffer.Position = 0;
        this.rawRecord = "";
        return bool;
    }

    public void close()
    {
        if (!this.closed)
        {
            close(true);
            this.closed = true;
        }
    }

    private void close(boolean paramBoolean)
    {
        if (!this.closed)
        {
            if (paramBoolean)
            {
                this.charset = null;
                this.headersHolder.Headers = null;
                this.headersHolder.IndexByName = null;
                this.dataBuffer.Buffer = null;
                this.columnBuffer.Buffer = null;
                this.rawBuffer.Buffer = null;
            }
            try
            {
                if (this.initialized) {
                    this.inputStream.close();
                }
            }
            catch (Exception localException) {}
            this.inputStream = null;
            this.closed = true;
        }
    }

    private void checkClosed()
            throws IOException
    {
        if (this.closed) {
            throw new IOException("This instance of the CsvReader class has already been closed.");
        }
    }

    protected void finalize()
    {
        close(false);
    }

    private static char hexToDec(char paramChar)
    {
        char c;
        if (paramChar >= 'a') {
            c = (char)(paramChar - 'a' + 10);
        } else if (paramChar >= 'A') {
            c = (char)(paramChar - 'A' + 10);
        } else {
            c = (char)(paramChar - '0');
        }
        return c;
    }

    private class StaticSettings
    {
        public static final int MAX_BUFFER_SIZE = 1024;
        public static final int MAX_FILE_BUFFER_SIZE = 4096;
        public static final int INITIAL_COLUMN_COUNT = 10;
        public static final int INITIAL_COLUMN_BUFFER_SIZE = 50;

        private StaticSettings() {}
    }

    private class HeadersHolder
    {
        public String[] Headers = null;
        public int Length = 0;
        public HashMap IndexByName = new HashMap();

        public HeadersHolder() {}
    }

    private class UserSettings
    {
        public boolean CaseSensitive = true;
        public char TextQualifier = '"';
        public boolean TrimWhitespace = true;
        public boolean UseTextQualifier = true;
        public char Delimiter = ',';
        public char RecordDelimiter = '\000';
        public char Comment = '#';
        public boolean UseComments = false;
        public int EscapeMode = 1;
        public boolean SafetySwitch = true;
        public boolean SkipEmptyRecords = true;
        public boolean CaptureRawRecord = true;

        public UserSettings() {}
    }

    private class Letters
    {
        public static final char LF = '\n';
        public static final char CR = '\r';
        public static final char QUOTE = '"';
        public static final char COMMA = ',';
        public static final char SPACE = ' ';
        public static final char TAB = '\t';
        public static final char POUND = '#';
        public static final char BACKSLASH = '\\';
        public static final char NULL = '\000';
        public static final char BACKSPACE = '\b';
        public static final char FORM_FEED = '\f';
        public static final char ESCAPE = '\033';
        public static final char VERTICAL_TAB = '\013';
        public static final char ALERT = '\007';

        private Letters() {}
    }

    private class RawRecordBuffer
    {
        public char[] Buffer = new char[500];
        public int Position = 0;

        public RawRecordBuffer() {}
    }

    private class ColumnBuffer
    {
        public char[] Buffer = new char[50];
        public int Position = 0;

        public ColumnBuffer() {}
    }

    private class DataBuffer
    {
        public char[] Buffer = new char[1024];
        public int Position = 0;
        public int Count = 0;
        public int ColumnStart = 0;
        public int LineStart = 0;

        public DataBuffer() {}
    }

    private class ComplexEscape
    {
        private static final int UNICODE = 1;
        private static final int OCTAL = 2;
        private static final int DECIMAL = 3;
        private static final int HEX = 4;

        private ComplexEscape() {}
    }
}
