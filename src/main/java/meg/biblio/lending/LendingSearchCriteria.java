package meg.biblio.lending;

import meg.tools.DateUtils;

import java.util.Calendar;
import java.util.Date;

public class LendingSearchCriteria {

    public final static String LendTypeLkup = "lendtypeselect";
    public final static String ClassTypeLkup = "classselect";
    public final static String TimeTypeLkup = "timeperiodselect";
    public final static String SchoolYearLkup = "schoolyear";
    public final static String SchoolYearKey = "1";

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
        public final static Long CHECKEDOUT = 2L;
        public final static Long RETURNED = 3L;
        public final static Long OVERDUE = 4L;
        public final static Long CURRENT_OVERDUE = 5L;
        public final static Long CURRENT_CHECKEDOUT = 6L;
    }

    public final static class SortKey {
        public final static long CLASS = 1L;
        public final static long STUDENTFIRSTNAME = 2L;
        public final static long BOOKID = 3L;
        public final static long CHECKEDOUT = 4L;
        public final static long TITLE = 5L;
        public final static long RETURNED = 6L;
        public final static long LATE = 7L;
    }

    public static final class SortByDir {
        public static final long ASC = 1;
        public static final long DESC = 2;
    }

    private Long lendingMode;
    private Date startDate;
    private Date endDate;
    private Long forschoolgroup;
    private Long lentToType;
    private Long clientid;
    private Long borrowerid;
    private Long bookid;
    private Boolean checkedout;

    private long sortkey;
    private long sortdir;


    public LendingSearchCriteria(Long lendingMode) {
        super();
        this.lendingMode = lendingMode;
    }


    public Long getLendingMode() {
        return lendingMode;
    }


    public void setLendingMode(Long lendingMode) {
        this.lendingMode = lendingMode;
    }


    public Date getStartDate() {
        return startDate;
    }


    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }


    public Date getEndDate() {
        return endDate;
    }


    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getCheckedoutOnly() {
        return checkedout;
    }

    public void setCheckedoutOnly(Boolean checkedout) {
        this.checkedout = checkedout;
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
        if (classselect == null || classselect == ClassType.ALL) {
            return;
        }
        // otherwise, set classid in criteria
        setSchoolgroup(classselect);
    }


    public void setTimeselect(Long timeselect) {
        if (timeselect != null) {
            Date startdate = null;
            Date enddate = null;
            if (timeselect.longValue() == TimePeriodType.THISWEEK) {
                // get previous Sunday through present
                startdate = DateUtils.getCurrentWeekSunday(new Date());
            } else if (timeselect.longValue() == TimePeriodType.CURRENTMONTH) {
                // get first day of month through present
                startdate = DateUtils.getFirstDayCurrentMonth(new Date());
            } else if (timeselect.longValue() == TimePeriodType.LASTTHREEMONTHS) {
                // get first day of month, one month ago through last day of month, one month ago
                startdate = DateUtils.getFirstDayThreeMonthsAgo(new Date());
            } else if (timeselect.longValue() == TimePeriodType.CURRENTSCHOOLYEAR) {
                // september 1 of current school year through present
                startdate = DateUtils.getFirstDayOfSchoolYear(new Date());
            } else if (timeselect.longValue() > 100) {
                // asking for a different school year
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, timeselect.intValue());
                cal.set(Calendar.MONTH, Calendar.SEPTEMBER);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                startdate = DateUtils.getFirstDayOfSchoolYear(cal.getTime());
                enddate = DateUtils.getLastDayOfSchoolYear(startdate);

            }


            this.startDate = startdate;
            this.endDate = enddate;


        }

    }


    public void setSortKey(long sortkey) {
        this.sortkey = sortkey;
    }

    public void setSortDir(long sortdir) {
        if (sortdir > 0) {
            this.sortdir = sortdir;
        }
    }

    public long getSortKey() {
        if (sortkey > 0) {
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
        if (sortdir > 0) {
            return this.sortdir;
        } else {

            return getDefaultSortDir();
        }
    }


    public boolean isDateSearch() {
        // This method also fills in the endDate, if the endDate is missing, with
        // a default endDate (1 year in the future.
        if (startDate == null) {
            return false;
        } else if (endDate == null) {
            endDate = DateUtils.getOneYearFromDate(startDate);
        }

        return true;
    }
}
