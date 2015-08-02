package meg.biblio.lending;

import java.util.Date;

import meg.tools.DateUtils;

public class LendingSearchCriteria {

	public final static String LendTypeLkup="lendtypeselect";
	public final static String ClassTypeLkup="classselect";
	public final static String TimeTypeLkup="timeperiodselect";
	
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
		public final static Long ALL = 4L;
	}	
	
	public final static class LendingType {
		public final static Long ALL = 1L;
		public final static Long OVERDUE= 2L;
		public final static Long CHECKEDOUT = 3L;
	}	
	

	private Date checkedouton;
	private Date returnedon;
	private Long forschoolgroup;
	private Long lentToType;
	private Boolean overdueonly;
	private Long clientid;
	private Long borrowerid;
	private Long bookid;
	private Boolean checkedout;


	public Date getCheckedouton() {
		return checkedouton;
	}

	public void setCheckedouton(Date checkedouton) {
		this.checkedouton = checkedouton;
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
			}
			setCheckedouton(startdate);
		}

	}


	public void setLendtypeselect(Long lendtypeselect) {
		//this.lendtypeselect = lendtypeselect;
	}

	public void reset() {
		checkedouton = null;
		returnedon = null;
		forschoolgroup = null;
		lentToType = null;
		overdueonly = null;
		clientid = null;
		borrowerid = null;
		bookid = null;
		checkedout = null;
	}

}
