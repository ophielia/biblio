package meg.tools;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(locations = "classpath:/META-INF/spring/applicationContext*.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class DateUtilsTest {

	@Test
	public void testGetSchoolYearBegin() {
		// Calendar for tests
		Calendar cal = Calendar.getInstance();

		// test for september 1970 - result should be 1970
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
		Integer beginyear = DateUtils.getSchoolYearBeginForDate(cal.getTime());
		Assert.assertEquals(new Integer(1970), beginyear);

		// test for december 1970 - result should be 1970
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		beginyear = DateUtils.getSchoolYearBeginForDate(cal.getTime());
		Assert.assertEquals(new Integer(1970), beginyear);

		// test for july 1970 - result should be 1970
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.JULY);
		beginyear = DateUtils.getSchoolYearBeginForDate(cal.getTime());
		Assert.assertEquals(new Integer(1970), beginyear);

		// test for july 1970 - result should be 1969
		cal.set(Calendar.YEAR, 1970);
		cal.set(Calendar.MONTH, Calendar.MARCH);
		beginyear = DateUtils.getSchoolYearBeginForDate(cal.getTime());
		Assert.assertEquals(new Integer(1969), beginyear);

	}

	@Test
	public void testGetCurrentWeekSunday() {
		// Calendar for tests
		Calendar cal = Calendar.getInstance();

		// test for july 17, 2015 - should be july 12, 2015
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 17);
		Date beginsun = DateUtils.getCurrentWeekSunday(cal.getTime());
		Calendar checkcal = Calendar.getInstance();
		checkcal.setTime(beginsun);
		Assert.assertEquals(new Integer(12),
				(Integer) checkcal.get(Calendar.DAY_OF_MONTH));

		// test for dec 12, 2014 - should be dec 7, 2014
		cal.set(Calendar.YEAR, 2014);
		cal.set(Calendar.MONTH, Calendar.DECEMBER);
		cal.set(Calendar.DAY_OF_MONTH, 12);
		beginsun = DateUtils.getCurrentWeekSunday(cal.getTime());
		checkcal = Calendar.getInstance();
		checkcal.setTime(beginsun);
		Assert.assertEquals(new Integer(7),
		(Integer) checkcal.get(Calendar.DAY_OF_MONTH));

		// test for july 4, 2015 - should be june 28, 2015
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 4);
		beginsun = DateUtils.getCurrentWeekSunday(cal.getTime());
		checkcal = Calendar.getInstance();
		checkcal.setTime(beginsun);
		Assert.assertEquals(new Integer(28),
		(Integer) checkcal.get(Calendar.DAY_OF_MONTH));	
		Assert.assertEquals((Integer)Calendar.JUNE,
		(Integer) checkcal.get(Calendar.MONTH));	
		
		// test for january 2, 2015 - should be dec 28, 2014
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 2);
		beginsun = DateUtils.getCurrentWeekSunday(cal.getTime());
		checkcal = Calendar.getInstance();
		checkcal.setTime(beginsun);
		Assert.assertEquals(new Integer(28),
		(Integer) checkcal.get(Calendar.DAY_OF_MONTH));	
		Assert.assertEquals((Integer)Calendar.DECEMBER,
		(Integer) checkcal.get(Calendar.MONTH));			
		Assert.assertEquals(new Integer(2014),
		(Integer) checkcal.get(Calendar.YEAR));	
	}
	
	@Test
	public void testGetFirstDayCurrentMonth() {
		// Calendar for tests
		Calendar cal = Calendar.getInstance();

		// test for july 17, 2015 - should be july 1, 2015
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 17);
		Date beginsun = DateUtils.getFirstDayCurrentMonth(cal.getTime());
		Calendar checkcal = Calendar.getInstance();
		checkcal.setTime(beginsun);
		Assert.assertEquals(new Integer(1),
				(Integer) checkcal.get(Calendar.DAY_OF_MONTH));

	}	
	
	@Test
	public void testGetFirstDayThreeMonthsAgo() {
		// Calendar for tests
		Calendar cal = Calendar.getInstance();

		// test for july 17, 2015 - should be april 1, 2015
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, Calendar.JULY);
		cal.set(Calendar.DAY_OF_MONTH, 17);
		Date beginsun = DateUtils.getFirstDayThreeMonthsAgo(cal.getTime());
		Calendar checkcal = Calendar.getInstance();
		checkcal.setTime(beginsun);
		Assert.assertEquals(new Integer(1),
				(Integer) checkcal.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals((Integer)Calendar.APRIL,
				(Integer) checkcal.get(Calendar.MONTH));

		// test for january 2, 2015 - should be oct 1, 2014
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, 2);
		beginsun = DateUtils.getFirstDayThreeMonthsAgo(cal.getTime());
		checkcal = Calendar.getInstance();
		checkcal.setTime(beginsun);
		Assert.assertEquals(new Integer(1),
		(Integer) checkcal.get(Calendar.DAY_OF_MONTH));	
		Assert.assertEquals((Integer)Calendar.OCTOBER,
		(Integer) checkcal.get(Calendar.MONTH));			
		Assert.assertEquals(new Integer(2014),
		(Integer) checkcal.get(Calendar.YEAR));	
	}	
	
	@Test
	public void testGetFirstDayOfSchoolYear() {
		// Calendar for tests
		Calendar cal = Calendar.getInstance();
		
		// test for june 17, 2015 - should be september 1, 2014
		cal.set(Calendar.YEAR, 2015);
		cal.set(Calendar.MONTH, Calendar.JUNE);
		cal.set(Calendar.DAY_OF_MONTH, 17);
		Date beginsun = DateUtils.getFirstDayOfSchoolYear(cal.getTime());
		Calendar checkcal = Calendar.getInstance();
		checkcal.setTime(beginsun);
		Assert.assertEquals(new Integer(1),
				(Integer) checkcal.get(Calendar.DAY_OF_MONTH));
		Assert.assertEquals(new Integer(2014),
				(Integer) checkcal.get(Calendar.YEAR));

		Assert.assertEquals((Integer)Calendar.SEPTEMBER,
				(Integer) checkcal.get(Calendar.MONTH));

	}	
	
	@Test
	public void testIsToday() {
		// Calendar for tests
		Calendar cal = Calendar.getInstance();
		
		// test for today  - should be true
		Date today = cal.getTime();
		// call
		boolean test = DateUtils.isToday(today);
		Assert.assertTrue(test);

		// test for a year ago
		cal.add(Calendar.YEAR, -1);
		test = DateUtils.isToday(cal.getTime());
		Assert.assertTrue(!test);
	}		
	
}