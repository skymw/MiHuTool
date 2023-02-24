package cn.skywm.mihu.times;




import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * @(#)DateTimeUtil.java
 * 日期时间工具类，进行各种日期时间格式的转化以及格式化
 * Created by april on 2021/11/26.
 * @author april
 * @Version: 1.0
 */
public class DateTimeUtil {

	///
	//定义时间日期显示格式
	///
	public final static String DATE_FORMAT = "yyyy-MM-dd";
	public final static String DATE_FORMAT_CN = "yyyy年MM月dd日";
	public final static String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	public final static String TIME_FORMAT_MM = "yyyy-MM-dd HH:mm";
	public final static String TIME_FORMAT_CN = "yyyy年MM月dd日 HH:mm:ss";
	public final static String MONTH_FORMAT = "yyyy-MM";
	public final static String DATE_FORMAT_EN="yyyy.MM.dd";
	public final static String DATA_FORMAT_ON="yy.MM.dd";
	public final static String DATA_FORMAT_MM="yyyyMMdd";
	public final static String DATA_FORMAT_14 = "yyyyMMddHHmmss";
	public final static String DATA_FORMAT_12 = "yyyyMMddHHmm";
	public final static String DATA_FORMAT_y10= "yyyyMMddHH";
	public final static String DATA_FORMAT_8  = "yyyyMMdd";
	public final static String DATA_FORMAT_6  = "yyyyMM";
	public final static String DATA_FORMAT_10 = "MMddHHmmss";
	public final static String DATA_FORMAT_HOUR="yyyyMMddHH";

	public final static String DATA_FORMAT_HHmmss = "HHmmss";
	public final static String TIME_FORMAT_MILLI = "yyyy-MM-dd HH:mm:ss.SSS";

	public final static String TIME_FORMAT_ora = "MM/dd/yyyy HH:mm:ss";

	/**
	 * 取得前一天时间
	 */
	public static Date getLastDate(){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);    //得到前一天
		Date date = calendar.getTime();
		return date;
	}


	/**
	 * 字符串转为
	 * @param str
	 */
	public static Date toCSt(String str){
		Date date =null;
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//注意月份是MM
		try {
			date = simpleDateFormat.parse(str);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	/**
	 * 返回两个时间相隔的天数
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int differentDays(Date date1,Date date2)
	{
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(date1);

		Calendar cal2 = Calendar.getInstance();
		cal2.setTime(date2);
		int day1= cal1.get(Calendar.DAY_OF_YEAR);
		int day2 = cal2.get(Calendar.DAY_OF_YEAR);

		int year1 = cal1.get(Calendar.YEAR);
		int year2 = cal2.get(Calendar.YEAR);
		if(year1 != year2)   //同一年
		{
			int timeDistance = 0 ;
			for(int i = year1 ; i < year2 ; i ++)
			{
				if(i%4==0 && i%100!=0 || i%400==0)    //闰年
				{
					timeDistance += 366;
				}
				else    //不是闰年
				{
					timeDistance += 365;
				}
			}

			return timeDistance + (day2-day1) ;
		}
		else    //不同年
		{
			System.out.println("判断day2 - day1 : " + (day2-day1));
			return day2-day1;
		}
	}


	/**
	 * 将时间字符串转化为日期格式字符串
	 * @param timeStr 例如：20160325160000
	 * @return String 例如：2016-03-25 16:00:00
	 */
	public static String timeStrToDateStr(String timeStr){
		if (null == timeStr) {
			return null;
		}
		String dateStr = null;
		SimpleDateFormat sdf_input = new SimpleDateFormat("yyyyMMddHHmm");//输入格式
		SimpleDateFormat sdf_target =new SimpleDateFormat("yyyy-MM-dd HH:mm"); //转化成为的目标格式
		try {
			//将20160325160000转化为Fri Mar 25 16:00:00 CST 2016,再转化为2016-03-25 16:00:00
			dateStr = sdf_target.format(sdf_input.parse(timeStr));
		} catch (Exception e) {
		}
		return dateStr;
	}

	/**
	 * 将时间字符串转化为日期格式字符串
	 * @param timeStr 例如：20160325160000
	 * @return String 例如：2016-03-25 16:00:00
	 */
	public static String timeStrToDateStr_day(String timeStr){
		if (null == timeStr) {
			return null;
		}
		String dateStr = null;
		SimpleDateFormat sdf_input = new SimpleDateFormat("yyyyMMdd");//输入格式
		SimpleDateFormat sdf_target =new SimpleDateFormat("yyyy-MM-dd"); //转化成为的目标格式
		try {
			//将20160325160000转化为Fri Mar 25 16:00:00 CST 2016,再转化为2016-03-25 16:00:00
			dateStr = sdf_target.format(sdf_input.parse(timeStr));
		} catch (Exception e) {
		}
		return dateStr;
	}
	/**
	 * 计算时间戳时间段，转换为小时类型
	 * */
	public static double mathTime(String start,String end){
		long dat = Long.parseLong(start);
		long dat2 = Long.parseLong(end);
		long l = dat2-dat;
		long day=l/(24*60*60*1000);
		long hour=(l/(60*60*1000)-day*24);
		long min=((l/(60*1000))-day*24*60-hour*60);
		long s=(l/1000-day*24*60*60-hour*60*60-min*60);
		double a = (double)hour+(double)min/60+(double)s/3600;
		return a;
	}
	public static String getNowDay_yyyy_mm_dd(){
		return getFormatDateTime(new Date(),DATE_FORMAT);
	}

	public static String getNowDay_yyyy_mm_dd_hour(){
		return getFormatDateTime(new Date(),TIME_FORMAT);
	}

	//得到当前时间的前一小时
	public static String getLastDateTimeStrYYYYMMDDHH() {
		return getFormatDateTime(getLastHour(),DATA_FORMAT_12);
	}

	//获取当前时间前半小时
	public static String getLastDateTimeMintueStrYYYYMMDDHH(){
		return getFormatDateTime(getLastmintue(),DATA_FORMAT_12);
	}

	//前一天
	public static String getYesterday(){
		return getFormatDateTime(getLastDate(),DATA_FORMAT_8);
	}

	//前一天
	public static String getYesterday_1(){
		return getFormatDateTime(getLastDate(),DATE_FORMAT);
	}

	public static String getNowDay(){
		return getFormatDateTime(new Date(),DATA_FORMAT_8);
	}

	public static Date getLastHour(){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.HOUR, -1);
		Date date = calendar.getTime();
		return date;
	}

	public static Date getLastmintue(){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, -30);
		Date date = calendar.getTime();
		return date;
	}

	public static String getDefLastmintue(String a){
		String str=null;
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
			Date now = null;
			long time = 30*60*1000;//30分钟
			now=sdf.parse(a);
			Date afterDate = new Date(now .getTime() - time);//30分钟后的时间
			str=(String)getFormatDate(afterDate,DATA_FORMAT_12);;
			System.out.println(afterDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return str;
	}

	/**
	 * 取得当前系统时间，返回java.util.Date类型
	 * @see Date
	 * @return java.util.Date 返回服务器当前系统时间
	 */
	public static Date getCurrDate() {
		return new Date();
	}

	/**
	 * 取得当前系统时间戳
	 * @see java.sql.Timestamp
	 * @return java.sql.Timestamp 系统时间戳
	 */
	public static java.sql.Timestamp getCurrTimestamp() {
		return new java.sql.Timestamp(System.currentTimeMillis());
	}


	/**
	 * 根据格式得到格式化后的日期
	 * @param currDate 要格式化的日期
	 * @param format 日期格式，如yyyy-MM-dd
	 * @see SimpleDateFormat#format(Date)
	 * @return String 返回格式化后的日期，格式由参数<code>format</code>定义，如yyyy-MM-dd，如2006-02-15
	 */
	public static String getFormatDate(Date currDate, String format) {
		SimpleDateFormat dtFormatdB = null;
		try {
			dtFormatdB = new SimpleDateFormat(format);
			return dtFormatdB.format(currDate);
		} catch (Exception e) {
			dtFormatdB = new SimpleDateFormat(DATE_FORMAT);
			return dtFormatdB.format(currDate);
		}
	}



	/**
	 * 得到格式化后的时间，格式为yyyy年MM月dd日 HH:mm:ss，如2006年02月15日 15:23:45
	 * @param currDate 要格式化的时间
	 * @see #getFormatDateTime(String, String)
	 * @return Date 返回格式化后的时间，默认格式为yyyy年MM月dd日 HH:mm:ss，如2006年02月15日 15:23:45
	 */
	public static Date getFormatDateTime_CN(String currDate) {
		return getFormatDateTime(currDate, TIME_FORMAT_CN);
	}

	/**
	 * 根据格式得到格式化后的时间
	 * @param currDate 要格式化的时间
	 * @param format 时间格式，如yyyy-MM-dd HH:mm:ss
	 * @see SimpleDateFormat#format(Date)
	 * @return String 返回格式化后的时间，格式由参数<code>format</code>定义，如yyyy-MM-dd HH:mm:ss
	 */
	public static String getFormatDateTime(Date currDate, String format) {
		SimpleDateFormat dtFormatdB = null;
		try {
			dtFormatdB = new SimpleDateFormat(format);
			return dtFormatdB.format(currDate);
		} catch (Exception e) {
			dtFormatdB = new SimpleDateFormat(TIME_FORMAT);
			return dtFormatdB.format(currDate);
		}
	}

	/**
	 * 根据格式得到格式化后的日期
	 * @param currDate 要格式化的日期
	 * @param format 日期格式，如yyyy-MM-dd
	 * @see SimpleDateFormat#parse(String)
	 * @return Date 返回格式化后的日期，格式由参数<code>format</code>定义，如yyyy-MM-dd，如2006-02-15
	 */
	public static Date getFormatDate(String currDate, String format) {
		SimpleDateFormat dtFormatdB = null;
		try {
			dtFormatdB = new SimpleDateFormat(format);
			return dtFormatdB.parse(currDate);
		} catch (Exception e) {
			dtFormatdB = new SimpleDateFormat(DATE_FORMAT);
			try {
				return dtFormatdB.parse(currDate);
			} catch (Exception ex){}
		}
		return null;
	}

	/**
	 * 根据格式得到格式化后的时间
	 * @param currDate 要格式化的时间
	 * @param format 时间格式，如yyyy-MM-dd HH:mm:ss
	 * @see SimpleDateFormat#parse(String)
	 * @return Date 返回格式化后的时间，格式由参数<code>format</code>定义，如yyyy-MM-dd HH:mm:ss
	 */
	public static Date getFormatDateTime(String currDate, String format) {
		SimpleDateFormat dtFormatdB = null;
		try {
			dtFormatdB = new SimpleDateFormat(format);
			return dtFormatdB.parse(currDate);
		} catch (Exception e) {
			dtFormatdB = new SimpleDateFormat(TIME_FORMAT);
			try {
				return dtFormatdB.parse(currDate);
			} catch (Exception ex){}
		}
		return null;
	}


	/**
	 * 得到格式化后的当前系统时间，格式为yyyyMMddHHmmss，如20060215152345
	 * @return String 返回格式化后的当前服务器系统时间，格式为yyyyMMddHHmmss，如20060215152345
	 */
	public static String getCurrDateTimeStrNew() {
		return getFormatDateTime(getCurrDate(),DATA_FORMAT_14);
	}


	public static String getCurrDateTimeStrHHmmss() {
		return getFormatDateTime(getCurrDate(),DATA_FORMAT_HHmmss);
	}

	public static String getCurrDateTimeStrMMddHHmmss() {
		return getFormatDateTime(getCurrDate(),DATA_FORMAT_10);
	}

	public static String getCurrDateTimeStr() {
		return getFormatDateTime(getCurrDate(),DATA_FORMAT_MM);
	}

	/**
	 * 得到格式化后的当前系统时间，格式为yyyyMM，如200602
	 * @return String 返回格式化后的当前服务器系统时间，格式为yyyyMMd，如200602
	 */
	public static String getCurrDateTimeStr6() {
		return getFormatDateTime(getCurrDate(),DATA_FORMAT_6);
	}

	public static String getCurrDateTimeStr8() {
		return getFormatDateTime(getCurrDate(),DATA_FORMAT_MM);
	}

	/**
	 * 得到格式化后的当前系统时间，格式为yyyyMMddHHmm，如200602151523
	 * @return String 返回格式化后的当前服务器系统时间，格式为yyyyMMddHHmm，如200602151523
	 */
	public static String getCurrDateTimeStr12() {
		return getFormatDateTime(getCurrDate(),DATA_FORMAT_12);
	}

	/**
	 * 得到格式化后的当前系统时间，格式为yyyyMMddHHmmss，如20060215152345
	 * @return String 返回格式化后的当前服务器系统时间，格式为yyyyMMddHHmmss，如20060215152345
	 */
	public static String getCurrDateTimeStrNew(Date date) {
		return getFormatDateTime(date,DATA_FORMAT_14);
	}


	public static Date getFormatDateNew(String dateStr){
		return getFormatDateTime(dateStr, DATA_FORMAT_14);
	}

	public static String getCurrDateTimeStr(Date date) {
		return getFormatDateTime(date,DATA_FORMAT_MM);
	}

	/**
	 * 得到格式化后的当前系统时间，格式为yyyy年MM月dd日 HH:mm:ss，如2006年02月15日 15:23:45
	 * @see #getFormatDateTime(Date, String)
	 * @return String 返回格式化后的当前服务器系统时间，格式为yyyy年MM月dd日 HH:mm:ss，如2006年02月15日 15:23:45
	 */
	public static String getCurrDateTimeStr_CN() {
		return getFormatDateTime(getCurrDate(), TIME_FORMAT_CN);
	}



	public static String getMonthBeginStr(){
		Calendar nowTime =  Calendar.getInstance();
		nowTime.set(Calendar.DAY_OF_MONTH, 1);
		nowTime.set(Calendar.HOUR_OF_DAY, 0);
		nowTime.set(Calendar.MINUTE, 0);
		nowTime.set(Calendar.SECOND, 0);

		return getFormatDate(nowTime.getTime(), DATA_FORMAT_14);
	}

	/**
	 * 得到本月末的时间的字符串，格式：yyyyMMddHHmmss
	 * @return
	 */
	public static String getMonthEndStr(){
		Calendar now = Calendar.getInstance();
		int endDay = now.getActualMaximum(Calendar.DAY_OF_MONTH);
		now.set(Calendar.DAY_OF_MONTH, endDay);
		now.set(Calendar.HOUR_OF_DAY, 23);
		now.set(Calendar.MINUTE, 59);
		now.set(Calendar.SECOND, 59);

		return getFormatDate(now.getTime(), DATA_FORMAT_14);
	}

	public static Date  getLastWeekSunday(Date date) {

		Date a = DateUtils.addDays(date, -1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(a);
		cal.set(Calendar.DAY_OF_WEEK, 1);

		return cal.getTime();
	}


	public static Date getLastWeekMonday(Date date) {
		Date a = DateUtils.addDays(date, -1);
		Calendar cal = Calendar.getInstance();
		cal.setTime(a);
		cal.add(Calendar.WEEK_OF_YEAR, -1);// 一周
		cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

		return cal.getTime();
	}

	public static String transTime(long second){
		String day = (second / (24 * 60 * 60)) + "天";
		String hour = (second % (24 * 60 * 60) / (60 * 60)) + "小时";
		String min = (second % (24 * 60 * 60) % (60 * 60) / 60) + "分钟";
		String sec = (second % (24 * 60 * 60) % (60 * 60) % 60) + "秒";

		return day+hour+min+sec;
	}

	/**
	 * 获取某天的周一
	 * @param date
	 * @return
	 */
	public static Date getFirstDayOfWeek(Date date) {
		Calendar c = new GregorianCalendar();
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek()); // Monday
		return c.getTime();
	}

	/**
	 * 获取某天的周日
	 * @param date
	 * @return
	 */
	public static Date getLastDayOfWeek(Date date) {
		Calendar c = new GregorianCalendar();
		c.setFirstDayOfWeek(Calendar.MONDAY);
		c.setTime(date);
		c.set(Calendar.DAY_OF_WEEK, c.getFirstDayOfWeek() + 6); // Sunday
		return c.getTime();
	}

	public static String getBeforeHour(){
		Calendar  cal   =   Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY,   -1);
		String hour = new SimpleDateFormat(DATA_FORMAT_HOUR).format(cal.getTime());
		return hour;
	}

	/**
	 * 计算两个时间得差值
	 * @param date1 开始
	 * @param date2 结束
	 * @return
	 */
	public static long diffsec(Date date1,Date date2){
		Long data_1=date1.getTime();
		Long data_2=date2.getTime();
		long c = (int)((data_1 - data_2) / 1000);
		return c;
	}


	public static String getBefore10Minute(){
		Calendar   cal   =   Calendar.getInstance();
		cal.add(Calendar.MINUTE,   -10);
		String hour = new SimpleDateFormat(DATA_FORMAT_12).format(cal.getTime());
		return hour;
	}

	public static String getBefore5Minute(){
		Calendar   cal   =   Calendar.getInstance();
		cal.add(Calendar.MINUTE,   -5);
		String hour = new SimpleDateFormat(DATA_FORMAT_12).format(cal.getTime());
		return hour;
	}

	/**
	 * 获得当前日期
	 */
	public static String getNowDate () {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(System.currentTimeMillis());
	}

	/**
	 * 获得当前日期时间
	 */
	public static String getNowDateTime () {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(System.currentTimeMillis());
	}


//	public static String getCustomMinute(){
//		Calendar   cal   =   Calendar.getInstance();
//		cal.add(Calendar.MINUTE, Config.custom_time);
//		String hour = new SimpleDateFormat(DATA_FORMAT_12).format(cal.getTime());
//		return hour;
//	}

	public static void main(String[] args){
		System.out.println(getNowDate());
		System.out.println(getNowDateTime());

	}


}

