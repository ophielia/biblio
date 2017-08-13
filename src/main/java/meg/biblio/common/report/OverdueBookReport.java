package meg.biblio.common.report;

import meg.biblio.lending.db.dao.LoanRecordDisplay;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;
import java.util.List;

@XmlRootElement(name = "overduereport")
public class OverdueBookReport {

    private List<LoanRecordDisplay> booklist;
    private String clientname;
    private Date rundate;

    public OverdueBookReport() {
        super();
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

    @XmlElement(name = "book")
    public List<LoanRecordDisplay> getBooklist() {
        return booklist;
    }

    public void setBooklist(List<LoanRecordDisplay> overdue) {
        this.booklist = overdue;
    }

}
