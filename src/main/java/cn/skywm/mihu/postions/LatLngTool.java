package cn.skywm.mihu.postions;
/**
 * @ProjectName: OSM2TKGGENERAL
 * @Package: cn.netcommander.mdfs.osm.util
 * @ClassName: LatLngTool
 * @Author: april
 * @CreateDate: 2021/9/27 17:05
 * @Version: 1.0
 * Copyright: Copyright (c) 2021
 */
public class LatLngTool {

    public static double GetJiaoDu(double lat1, double lng1, double lat2, double lng2) {
        double x1 = lng1;
        double y1 = lat1;
        double x2 = lng2;
        double y2 = lat2;
        double pi = Math.PI;
        double w1 = y1 / 180 * pi;
        double j1 = x1 / 180 * pi;
        double w2 = y2 / 180 * pi;
        double j2 = x2 / 180 * pi;
        double ret;
        if (j1 == j2) {
            if (w1 > w2) return 270; //北半球的情况，南半球忽略
            else if (w1 < w2) return 90;
            else return -1;//位置完全相同
        }
        ret = 4 * Math.pow(Math.sin((w1 - w2) / 2), 2) - Math.pow(Math.sin((j1 - j2) / 2) * (Math.cos(w1) - Math.cos(w2)), 2);
        ret = Math.sqrt(ret);
        double temp = (Math.sin(Math.abs(j1 - j2) / 2) * (Math.cos(w1) + Math.cos(w2)));
        ret = ret / temp;
        ret = Math.atan(ret) / pi * 180;
        if (j1 > j2) // 1为参考点坐标
        {
            if (w1 > w2) ret += 180;
            else ret = 180 - ret;
        } else if (w1 > w2) ret = 360 - ret;
        return ret;
    }

    /// <summary>
    /// 计算方位 按360计算
    /// </summary>
    /// <param name="lat1">参照物纬度</param>
    /// <param name="lng1">参照物经度</param>
    /// <param name="lat2">目标纬度</param>
    /// <param name="lng2">目标经度</param>
    /// <returns></returns>
    public static String GetDirection(double lat1, double lng1, double lat2, double lng2) {
        double jiaodu = GetJiaoDu(lat1, lng1, lat2, lng2);
        if ((jiaodu <= 10) || (jiaodu > 350)) return "东";
        if ((jiaodu > 10) && (jiaodu <= 80)) return "东北";
        if ((jiaodu > 80) && (jiaodu <= 100)) return "北";
        if ((jiaodu > 100) && (jiaodu <= 170)) return "西北";
        if ((jiaodu > 170) && (jiaodu <= 190)) return "西";
        if ((jiaodu > 190) && (jiaodu <= 260)) return "西南";
        if ((jiaodu > 260) && (jiaodu <= 280)) return "南";
        if ((jiaodu > 280) && (jiaodu <= 350)) return "东南";
        return "";

    }

    public static void main(String[] args) {
        System.out.println(GetDirection(26.6105605,106.612376,26.609352,106.6045316));
    }
}


