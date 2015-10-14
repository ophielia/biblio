package meg.biblio.lending;

import java.util.Calendar;
import java.util.Date;

import meg.tools.DateUtils;

public class LendingSearchCriteria {

	public final static String LendTypeLkup="lendtypeselect";
	public final static String ClassTypeLkup="classselect";
	public final static String TimeTypeLkup="timeperiodselect";
	public final static String SchoolYearLkup="schoolyear";
	public final static String SchoolYearKey="1";
	
	public final static class LentToType {
		public final static Long TEACHER = 1L;
		public final static Long STUDENT = 2L;
		public final static Long BOTH = 3L;
	}
	
	public final static class ClassType {
		public final static Long ALL = 1L;
	}	


	
	public final static class TimePeriodType {
		public final static Long THISWEEK = 1L;
		public final static Long CURRENTMONTH = 2L;
		public final static Long LASTTHREEMONTHS = 3L;
		public final static Long CURRENTSCHOOLYEAR = 4L;
		public final static Long ALL = 5L;
	}	
	
	public final static class LendingType {
		public final static Long ALL = 1L;
		public final static Long CHECKEDOUT= 2L;
		public final static Long OVERDUE = 3L;
	}	
	
	public final static class SortKey {
		public final static long CLASS = 1L;
		public final static long STUDENTFIRSTNAME = 2L;
		public final static long BOOKID = 3L;
		public final static long CHECKEDOUT = 4L;
		public final static long TITLE = 5L;
		public final static long RETURNED = 6L;
	}	

	public static final class SortByDir {
		public static final long ASC = 1;
		public static final long DESC = 2;
	}
	
	private Date checkedoutafter;
	private Date checkedoutbefore;
	private Date returnedon;
	private Long forschoolgroup;
	private Long lentToType;
	private Boolean overdueonly;
	private Long clientid;
	private Long borrowerid;
	private Long bookid;
	private Boolean checkedout;
	private Long lendingType;
	
	private long sortkey;
	private long sortdir;


	public Date getCheckedoutafter() {
		return checkedoutafter;
	}

	public void setCheckedoutafter(Date checkedouton) {
		this.checkedoutafter = checkedouton;
	}

	
	
	public Date getCheckedoutbefore() {
		return checkedoutbefore;
	}

	public void setCheckedoutbefore(Date checkedoutbefore) {
		this.checkedoutbefore = checkedoutbefore;
	}

	public Boolean getCheckedoutOnly() {
		return checkedout;
	}

	public void setCheckedoutOnly(Boolean checkedout) {
		this.checkedout = checkedout;
	}

	public Date getReturnedon() {
		return returnedon;
	}

	public void setReturnedon(Date returnedon) {
		this.returnedon = returnedon;
	}

	public Long getBookid() {
		return bookid;
	}

	public void setBookid(Long bookid) {
		this.bookid = bookid;
	}

	public Long getSchoolgroup() {
		return forschoolgroup;
	}

	public void setSchoolgroup(Long forschoolgroup) {
		this.forschoolgroup = forschoolgroup;
	}

	public Long getLentToType() {
		return lentToType;
	}

	public void setLentToType(Long lentTo) {
		this.lentToType = lentTo;
	}

	public Boolean getOverdueOnly() {
		return overdueonly;
	}

	public void setOverdueOnly(Boolean overdueonly) {
		this.overdueonly = overdueonly;
	}

	public Long getClientid() {
		return clientid;
	}

	public void setClientid(Long clientid) {
		this.clientid = clientid;
	}

	public Long getBorrowerid() {
		return borrowerid;
	}

	public void setBorrowerid(Long borrowerid) {
		this.borrowerid = borrowerid;
	}


	public void setClassselect(Long classselect) {
		// if class is set to all, return immediately (don't set
		// classid in criteria
		if (classselect ==null || classselect==ClassType.ALL) {
			return;
		}
		// otherwise, set classid in criteria
		setSchoolgroup(classselect);
	}


	public void setTimeselect(Long timeselect) {
		if (timeselect!=null) {
			Date startdate = null;
			if (timeselect.longValue()==TimePeriodType.THISWEEK) {
				// get previous Sunday through present
				startdate = DateUtils.getCurrentWeekSunday(new Date());
			} else if (timeselect.longValue()== TimePeriodType.CURRENTMONTH) {
				// get first day of month through present
				startdate = DateUtils.getFirstDayCurrentMonth(new Date());
			} else if (timeselect.longValue()== TimePeriodType.LASTTHREEMONTHS) {
				// get first day of month, one month ago through last day of month, one month ago
				startdate = DateUtils.getFirstDayThreeMonthsAgo(new Date());
			} else if (timeselect.longValue()==TimePeriodType.CURRENTSCHOOLYEAR) {
				// september 1 of current school year through present
				startdate = DateUtils.getFirstDayOfSchoolYear(new Date());
			} else if (timeselect.longValue()>100) {
				// asking for a different school year
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.YEAR, timeselect.intValue());
				cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
				cal.set(Calendar.DAY_OF_MONTH,1);
				startdate = DateUtils.getFirstDayOfSchoolYear(cal.getTime());
				Date enddate = DateUtils.getLastDayOfSchoolYear(startdate);
				setCheckedoutbefore(enddate);
			}
			setCheckedoutafter(startdate);
		}

	}


	public void setLendtypeselect(Long lendtypeselect) {
		this.lendingType = lendtypeselect;
		if (this.lendingType!=null) {
			if (this.lendingType == LendingType.CHECKEDOUT) {
				setCheckedoutOnly(true);
				setOverdueOnly(false);
			} else if (this.lendingType == LendingType.OVERDUE) {
				setCheckedoutOnly(true);
				setOverdueOnly(true);				
			}
		} else {
			setCheckedoutOnly(false);
			setOverdueOnly(false);
		}
	}

	public void reset() {
		checkedoutafter = null;
		returnedon = null;
		forschoolgroup = null;
		lentToType = null;
		overdueonly = null;
		clientid = null;
		borrowerid = null;
		bookid = null;
		checkedout = null;
	}

	public void setSortKey(long sortkey) {
		this.sortkey = sortkey;
	}

	public void setSortDir(long sortdir) {
		if (sortdir>0) {
			this.sortdir= sortdir;
		} 
	}
	
	public long getSortKey() {
		if (sortkey>0) {
			return this.sortkey;
		} else {
			
			return getDefaultSortKey();
		}
	}
	
	private long getDefaultSortKey() {
		return SortKey.CHECKEDOUT;
	}

	private long getDefaultSortDir() {
		return SortByDir.DESC;
	}
	
	public long getSortDir() {
		if (sortdir>0) {
			return this.sortdir;
		} else {
			
			return getDefaultSortDir();
		}
	}
}
