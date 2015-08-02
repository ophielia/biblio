package meg.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {


	public static Integer getSchoolYearBeginForDate(Date currentdate) {
		// make calendar
		Calendar cal = Calendar.getInstance();
		cal.setTime(currentdate);
		// get month, year
		int currentmonth = cal.get(Calendar.MONTH);
		int currentyear = cal.get(Calendar.YEAR);
		// if month between January and June, the begin year is the previous
		// year
		if (currentmonth < Calendar.JULY) {
			return new Integer(currentyear - 1);
		} else {
			// if month after June, the begin year is the current
			return new Integer(currentyear);
		}

	}
	
	public static Date getCurrentWeekSunday(Date currentdate) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(currentdate);
		// get day of week
		Integer dow = cal.get(Calendar.DAY_OF_WEEK);
		// if greater than 1 (Sunday) set day of week to day of week
		// minus 1
		if (dow > 1) {
			Integer tosubtract = (dow-1)*-1;
			cal.add(Calendar.DAY_OF_MONTH, tosubtract);
			return cal.getTime();
		} else {
		// otherwise, return current time
			return currentdate;
		}
	}
	
	public static Date getFirstDayCurrentMonth(Date currentdate) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(currentdate);
		// set day of month to 1
		cal.set(Calendar.DAY_OF_MONTH,1);
		
		// return date
		return cal.getTime();
	}
	
	public static Date getFirstDayThreeMonthsAgo(Date currentdate) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(currentdate);
		// set day of month to 15
		cal.set(Calendar.DAY_OF_MONTH,15);
		// subtract three months
		cal.add(Calendar.MONTH,-3);
		// set day of month to 1
		cal.set(Calendar.DAY_OF_MONTH,1);
		// return date
		return cal.getTime();
	}	
	
	public static Date getFirstDayOfSchoolYear(Date currentdate) {
		Calendar cal = new GregorianCalendar();
		cal.setTime(currentdate);
		
		// set year to schoolyear begin
		Integer year = getSchoolYearBeginForDate(currentdate);
		cal.set(Calendar.YEAR, year);
		// set month to september
		cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
		// set day to 1
		cal.set(Calendar.DAY_OF_MONTH, 1);
		
		// return date
		return cal.getTime();
	}	
	
	
	
}
