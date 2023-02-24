package cn.skywm.mihu.postions;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.List;
/**
 * @(#)Gps.java
 * 坐标工具类
 * Created by april on 2021/11/26.
 *
 * @author april
 * @Version: 1.0
 */
public class Gps {

    private double lat;
    private double lon;

    /**
     *
     * @param lat 纬度
     * @param lon 经度
     */
    public Gps(double lat, double lon) {
        setLat(lat);
        setLon(lon);
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String mkString(){
        return lon + "," + lat;
    }

    public String mkString(String sep){
        return lon + sep + lat;
    }

	public static boolean checkWithJdkGeneralPath_bak(Point2D.Double point,List<Point2D.Double> polygon){
        GeneralPath p = new GeneralPath();
        Point2D.Double first = polygon.get(0);
        p.moveTo(first.x, first.y);
        for (Point2D.Double d : polygon) {
            p.lineTo(d.x, d.y);
        }
        p.lineTo(first.x, first.y);
        p.closePath();
        return p.contains(point);
	}


    public static boolean checkWithJdkGeneralPath(Point2D.Double point, List<Point2D.Double> polygon) {
        GeneralPath p = new GeneralPath();

        Point2D.Double first = polygon.get(0);
        p.moveTo(first.x, first.y);

        for (Point2D.Double d : polygon) {
            p.lineTo(d.x, d.y);
        }

        p.lineTo(first.x, first.y);
        p.closePath();

        return p.contains(point);
    }
    // 方法二
    public static boolean checkWithJdkPolygon(Point2D.Double point, List<Point2D.Double> polygon) {
        Polygon p = new Polygon();
        // java.awt.geom.GeneralPath
        final int TIMES = 1000;
        for (Point2D.Double d : polygon) {
            int x = (int) d.x * TIMES;
            int y = (int) d.y * TIMES;
            p.addPoint(x, y);
        }
        int x = (int) point.x * TIMES;
        int y = (int) point.y * TIMES;
        return p.contains(x, y);
    }
}