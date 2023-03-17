package cn.skywm.mihu.files;

import cn.skywm.mihu.common.CsvWriter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname CsvFileUtil
 * @Description TODO
 * @Version 1.0.0
 * @Date 2023/3/1 10:53
 * @Created by april
 */
public class CsvFileUtil {
    /**
     * 写入csv文件
     * @param outfile  文件路径
     * @param headers 文件头 样例['row1','row2','row3']
     * @param contentlist 数据集合
     */
    public static void write_csv(String outfile, String[] headers, List<String> contentlist){
        CsvWriter csvWriter=new CsvWriter(outfile, ',', Charset.forName("UTF-8"));
        //构建表头
        try {
            csvWriter.writeRecord(headers);
            //构建csv内容
            for(String line:contentlist){
                csvWriter.writeRecord(line.split(","));
            }
            csvWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param path 文件路径
     * @return List<Map<String,String>>
     * @throws IOException
     */
    public static List<Map<String,String>> readCsv(String path) throws IOException {
        List<Map<String,String>> resmap=new ArrayList<>();
        BufferedReader br = null;
        String sCurrentLine;
        BufferedReader in = new BufferedReader(new FileReader(path));
        // 读取表格第一行作为map中的key
        String key = in.readLine();
        List<String> keyList = Arrays.stream(key.split(","))
                .filter(Objects::nonNull)
                .filter(string -> !string.isEmpty())
                .collect(Collectors.toList());

        String s = null;
        int line = 1;
        while ((s = in.readLine()) != null) {
            // 从第二行开始读取数据作为value
            List<String> param = Arrays.stream(s.split(","))
                    .filter(Objects::nonNull)
                    .filter(string -> !string.isEmpty())
                    .collect(Collectors.toList());
            line++;
            // 方法二
            Map<String, String> keyParam = keyList.stream()
                    .collect(Collectors.toMap(keys -> keys, keys -> param.get(keyList.indexOf(keys))));
//            System.out.println(keyParam);
            resmap.add(keyParam);
        }
        return resmap;
    }
}
