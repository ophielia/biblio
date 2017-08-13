package meg.biblio.inventory.db.dao;

import java.util.Date;

// use LoanRecordDisplay as model
public class InvStackDisplay {

    private Long bookid;
    private Long clientid;
    private String clientbooknr;
    private Long clientbooknrsort;
    private Long clientshelfcode;
    private String clientshelfclass;
    private Long status;
    private String note;
    private Date counteddate;
    private Boolean tocount;
    private Long userid;
    private Long countstatus;
    private String title;


    public InvStackDisplay(Long bookid, Long clientid, String clientbooknr, Long clientbooknrsort,
                           Long clientshelfcode, String clientshelfclass, Long status, String note, Date counteddate, Boolean tocount, Long userid, Long countstatus,
                           String title) {
        super();
        this.bookid = bookid;
        this.clientid = clientid;
        this.clientbooknr = clientbooknr;
        this.clientbooknrsort = clientbooknrsort;
        this.clientshelfcode = clientshelfcode;
        this.clientshelfclass = clientshelfclass;
        this.status = status;
        this.note = note;
        this.counteddate = counteddate;
        this.tocount = tocount;
        this.userid = userid;
        this.countstatus = countstatus;
        this.title = title;
    }


    public Long getBookid() {
        return bookid;
    }

    public Long getClientid() {
        return clientid;
    }

    public String getClientbooknr() {
        return clientbooknr;
    }

    public Long getClientshelfcode() {
        return clientshelfcode;
    }

    public Long getStatus() {
        return status;
    }

    public String getNote() {
        return note;
    }

    public Date getCounteddate() {
        return counteddate;
    }

    public Boolean getTocount() {
        return tocount;
    }

    public Long getUserid() {
        return userid;
    }

    public Long getCountstatus() {
        return countstatus;
    }

    public String getTitle() {
        return title;
    }


    public String getClientshelfclass() {
        return clientshelfclass;
    }


}
