package cn.skywm.mihu.common;/**
 * @ProjectName: mdfs-kee
 * @Package: cn.netcommander.mdfs.kee.utils
 * @ClassName:
 * @Author: maowei
 * @CreateDate: 2021/9/14 16:32
 * @Version: 1.0
 * Copyright: Copyright (c) 2021
 */

/**
 * @ProjectName: mdfs-kee
 * @Package: cn.netcommander.mdfs.kee.utils
 * @ClassName:
 * @Author: maowei
 * @CreateDate: 2021/9/14 16:32
 * @Version: 1.0
 * Copyright: Copyright (c) 2021
 */
public class ShortcutUtil {
    /**
     * 数组翻倍
     * 例：
     * {1, 2, 3} --> {1, 2, 3, 1, 2, 3}
     */
    public static Object[] array_doubling (Object[] array) {

        int old_length = array.length;
        int new_length = old_length * 2;
        Object[] new_array = new Object[new_length];

        for (int i = 0; i < new_length; i ++) {

            if (i < old_length) {
                new_array[i] = array[i];
            } else {
                new_array[i] = array[i - old_length];
            }
        }
        return new_array;
    }

    /**
     * 判断一个字符串是否不由某些模式串开头（子串中不许输入空串）
     */
    public static boolean judgmentStringNotStartWith (String string, String... targets) {
        if (string.length() == 0) {
            return true;
        }
        boolean flag = true;
        for (String single : targets) {
            if (single == null || single.length() == 0) {
                continue;
            }
            flag &= string.split(single, -1)[0].length() > 0;
        }
        return flag;
    }

    /**
     * 换行符转换（原则：数据在程序内输出至文件时进行转换）
     */
    public static String lineBreakConversion (String line) {
        line = line.replace("\r", "\\r");                    // MAC OS : 回车符CR : \r
        line = line.replace("\n", "\\n");                    // unix : 0x0A : \n
        line = line.replace("\r\n", "\\r\\n");               // Windows : 0x0D 和 0x0A : \r\n
        return line;
    }

    /**
     * 换行符反转换（原则：mysql 作为接收方情况下对入库前进行反转换）
     */
    public static String lineBreakReConversion (String line) {
        line = line.replace("\\r", "\r");
        line = line.replace("\\n", "\n");
        line = line.replace("\\r\\n", "\r\n");
        return line;
    }

    public static void main(String[] args) {

        String str = "a\r\nb\r\nc";
        System.out.println(lineBreakConversion(str));

    }

}
