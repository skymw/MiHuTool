package cn.skywm.mihu.postions;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * @ProjectName: OSM2TKGGENERAL
 * @Package: cn.netcommander.mdfs.osm.util
 * @ClassName: 获取经纬度中心点
 * @Author: maowei
 * @CreateDate: 2021/10/19 16:06
 * @Version: 1.0
 * Copyright: Copyright (c) 2021
 */
public class Position {
    /**
     * 纬度
     */
    private Double latitude;

    /**
     * 经度
     */
    private Double longitude;

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Position(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }



    /**
     *  取几个经纬度的中心点
     * @param postionList 经纬度的集合
     */
    public static String getCenterPoint(LinkedList<Position> postionList) {
        int total = postionList.size();
        double X = 0, Y = 0, Z = 0;

        while(!postionList.isEmpty()) {
            Position g = postionList.pollFirst();
            if(g != null) {
                double lat, lon, x, y, z;
                lat = g.getLatitude() * Math.PI / 180;
                lon = g.getLongitude() * Math.PI / 180;
                x = Math.cos(lat) * Math.cos(lon);
                y = Math.cos(lat) * Math.sin(lon);
                z = Math.sin(lat);
                X += x;
                Y += y;
                Z += z;
            }
        }


        X = X / total;
        Y = Y / total;
        Z = Z / total;
        double Lon = Math.atan2(Y, X);
        double Hyp = Math.sqrt(X * X + Y * Y);
        double Lat = Math.atan2(Z, Hyp);
        double longitude = Lon * 180 / Math.PI;
        double latitude = Lat * 180 / Math.PI;

//        return gps84_To_Gcj02(latitude,longitude).getLon()+"#"+gps84_To_Gcj02(latitude,longitude).getLat();
//        return GCJ2WGSUtils.WGSLon(latitude,longitude)+"#"+GCJ2WGSUtils.WGSLat(latitude,longitude);
        return longitude+"#"+latitude;
    }



    public static final String BAIDU_LBS_TYPE = "bd09ll";


    private static double pi = 3.1415926535897932384626;

    /**
     * 84 to 火星坐标系 (GCJ-02) World Geodetic System ==> Mars Geodetic System
     *
     * @param lat wgs84纬度
     * @param lon wgs84经度
     * @return GCJ-02坐标
     */
    public static Gps gps84_To_Gcj02(double lat, double lon) {
        return transform(lat, lon);
    }

    /**
     * 火星坐标系 (GCJ-02) to 84
     *
     * @param lon GCJ-02纬度
     * @param lat GCJ-02经度
     * @return wgs84坐标
     */
    public static Gps gcj02_To_Gps84(double lat, double lon) {
        Gps gps = transform(lat, lon);
        double longitude = lon * 2 - gps.getLon();
        double latitude = lat * 2 - gps.getLat();
        return new Gps(latitude, longitude);
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法 将 GCJ-02 坐标转换成 BD-09 坐标
     *
     * @param gg_lat GCJ-02纬度
     * @param gg_lon GCJ-02经度
     * @return BD-09坐标
     */
    public static Gps gcj02_To_Bd09(double gg_lat, double gg_lon) {
        double z = Math.sqrt(gg_lon * gg_lon + gg_lat * gg_lat) + 0.00002 * Math.sin(gg_lat * pi);
        double theta = Math.atan2(gg_lat, gg_lon) + 0.000003 * Math.cos(gg_lon * pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
        return new Gps(bd_lat, bd_lon);
    }

    /**
     * 火星坐标系 (GCJ-02) 与百度坐标系 (BD-09) 的转换算法
     * 将 BD-09 坐标转换成GCJ-02 坐标
     *
     * @param bd_lat BD-09纬度
     * @param bd_lon BD-09经度
     * @return GCJ-02坐标
     */
    public static Gps bd09_To_Gcj02(double bd_lat, double bd_lon) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return new Gps(gg_lat, gg_lon);
    }

    /**
     * (BD-09)-->84
     *
     * @param bd_lat BD-09纬度
     * @param bd_lon BD-09经度
     * @return wgs84坐标
     */
    public static Gps bd09_To_Gps84(double bd_lat, double bd_lon) {
        Gps gcj02 = bd09_To_Gcj02(bd_lat, bd_lon);
        return gcj02_To_Gps84(gcj02.getLat(), gcj02.getLon());
    }

    public static Gps gps84_To_bd09(double bd_lat, double bd_lon) {
        Gps gcj02 = gps84_To_Gcj02(bd_lat, bd_lon);
        return gcj02_To_Bd09(gcj02.getLat(), gcj02.getLon());
    }

    public static boolean outOfChina(double lat, double lon) {
        return lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }




    private static Gps transform(double lat, double lon) {
        if (outOfChina(lat, lon)) {
            return new Gps(lat, lon);
        }
        double dLat = transformLat(lon - 105.0, lat - 35.0);
        double dLon = transformLon(lon - 105.0, lat - 35.0);
        double radLat = lat / 180.0 * pi;
        double magic = Math.sin(radLat);
        double ee = 0.00669342162296594323;
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        double a = 6378245.0;
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * pi);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * pi);
        double mgLat = lat + dLat;
        double mgLon = lon + dLon;
        return new Gps(mgLat, mgLon);
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(y * pi) + 40.0 * Math.sin(y / 3.0 * pi)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(y / 12.0 * pi) + 320 * Math.sin(y * pi / 30.0)) * 2.0 / 3.0;
        return ret;
    }

    private static double transformLon(double x, double y) {
        double ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        ret += (20.0 * Math.sin(6.0 * x * pi) + 20.0 * Math.sin(2.0 * x * pi)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(x * pi) + 40.0 * Math.sin(x / 3.0 * pi)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(x / 12.0 * pi) + 300.0 * Math.sin(x / 30.0
                * pi)) * 2.0 / 3.0;
        return ret;
    }

    public static double degreeToRad(double d) {
        return d * Math.PI / 180.0;
    }

    static double x_pi = 3.14159265358979324 * 3000.0 / 180.0;
    //高德转百度
    public static String bdEncrypt(double gg_lat, double gg_lon){
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * x_pi);
        double bd_lon = z * Math.cos(theta) + 0.0065;
        double bd_lat = z * Math.sin(theta) + 0.006;
//        System.out.println(bd_lon+","+bd_lat);
//        System.out.println(new BigDecimal(String.valueOf(bd_lon)).floatValue()+","+new BigDecimal(String.valueOf(bd_lat)).floatValue());
        return new BigDecimal(String.valueOf(bd_lon)).floatValue()+"#"+new BigDecimal(String.valueOf(bd_lat)).floatValue();
    }

    public static String bd_decrypt(double bd_lat, double bd_lon){
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * x_pi);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * x_pi);
        double gg_lon = z * Math.cos(theta);
        double gg_lat = z * Math.sin(theta);
        return gg_lon+"#"+gg_lat;
    }

    /**
     * 通过经纬度获取距离(单位：米)
     *
     * @param lat1 纬度1
     * @param lng1 经度1
     * @param lat2 纬度2
     * @param lng2 经度2
     * @return 距离(米)
     */
    public static double getDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = degreeToRad(lat1);
        double radLat2 = degreeToRad(lat2);
        double a = radLat1 - radLat2;
        double b = degreeToRad(lng1) - degreeToRad(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2)
                * Math.pow(Math.sin(b / 2), 2)));
        double EARTH_RADIUS = 6378137.0;
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000d;
        return s;
    }

    public static double getDistance(String lat1s, String lng1s, String lat2s, String lng2s) {
        double lat1 = Double.parseDouble(lat1s);
        double lng1 = Double.parseDouble(lng1s);
        double lat2 = Double.parseDouble(lat2s);
        double lng2 = Double.parseDouble(lng2s);
        return getDistance(lat1, lng1, lat2, lng2);
    }

    public static void main(String[] args) {
        System.out.println(Position.transform(26.597300000008396,106.71475999954559).getLon()+","+Position.transform(26.597300000008396,106.71475999954559).getLat());
    }
}
