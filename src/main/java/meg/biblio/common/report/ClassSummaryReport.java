package meg.biblio.common.report;

import meg.biblio.lending.db.dao.LoanRecordDisplay;
import meg.biblio.lending.db.dao.SchoolGroupDao;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "classreport")
public class ClassSummaryReport {


    private String clientname;
    private Date rundate;
    private Long classid;
    private String teacherfn;
    private String teacherln;
    private List<LoanRecordDisplay> overduelist;
    private List<LoanRecordDisplay> checkedoutlist;
    private List<LoanRecordDisplay> historylist;


    public ClassSummaryReport() {
        super();
    }

    public ClassSummaryReport(SchoolGroupDao sgroup) {
        if (sgroup != null) {
            this.classid = sgroup.getId();
            this.teacherfn = sgroup.getTeacher().getFirstname();
            this.teacherln = sgroup.getTeacher().getLastname();
        }
    }

    @XmlElement
    public Date getRundate() {
        return rundate;
    }

    public void setRundate(Date date) {
        this.rundate = date;
    }

    @XmlElement
    public String getClientname() {
        return clientname;
    }

    public void setClientname(String name) {
        this.clientname = name;
    }

    @XmlElement
    public Long getClassid() {
        return classid;
    }

    public void setClassid(Long classid) {
        this.classid = classid;
    }

    @XmlElement(name = "firstname_teacher")
    public String getTeacherfn() {
        return teacherfn;
    }

    public void setTeacherfn(String teacherfn) {
        this.teacherfn = teacherfn;
    }

    @XmlElement(name = "lastname_teacher")
    public String getTeacherln() {
        return teacherln;
    }

    public void setTeacherln(String teacherln) {
        this.teacherln = teacherln;
    }

    @XmlElement(name = "checkedoutlist")
    public List<LoanRecordDisplay> getCheckedoutlist() {
        return checkedoutlist;
    }

    public void setCheckedoutlist(List<LoanRecordDisplay> checkedoutlist) {
        this.checkedoutlist = checkedoutlist;
    }

    @XmlElement(name = "overduelist")
    public List<LoanRecordDisplay> getOverduelist() {
        return overduelist;
    }

    public void setOverduelist(List<LoanRecordDisplay> overdue) {
        this.overduelist = overdue;
    }

    @XmlElement(name = "returnedlist")
    public List<LoanRecordDisplay> getReturnedlist() {
        return historylist;
    }

    public void setReturnedlist(List<LoanRecordDisplay> historylist) {
        this.historylist = historylist;
    }

    @XmlElement(name = "checkedoutcount")
    public int getCheckedoutCount() {
        if (getCheckedoutlist() == null) return 0;
        return getCheckedoutlist().size();
    }

    @XmlElement(name = "returnedcount")
    public int getReturnedCount() {
        if (getReturnedlist() == null) return 0;
        return getReturnedlist().size();
    }

    @XmlElement(name = "overduecount")
    public int getOverdueCount() {
        if (getOverduelist() == null) return 0;
        return getOverduelist().size();
    }

    public boolean isEmpty() {
        int total = getCheckedoutCount() + getReturnedCount() + getOverdueCount();
        return total == 0;
    }

    public boolean getIsEmpty() {
        return isEmpty();
    }
}
