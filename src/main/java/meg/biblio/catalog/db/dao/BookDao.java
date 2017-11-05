package meg.biblio.catalog.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "book")
public class BookDao {

    private Long clientid;
    private Long status;

    private Long clientshelfcode;
    private String clientshelfclass;
    private Date createdon;
    private String clientbookid;
    private Long clientbookidsort;
    private String barcodeid;
    @OneToOne(cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(name="bookdetail")
    private BookDetailDao bookdetail;
    private Long clientbooktype;
    private String note;
    private Boolean tocount;
    private Date counteddate;
    private Long countstatus;

    private Long userid;
    @Version
    @Column(name = "version")
    private Integer version;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    public void setClientbookid(String clientbookid) {
        if (clientbookid != null) {
            this.clientbookid = clientbookid.trim();
            setClientbookidsort(clientbookid);
        }
    }

    public void setClientbookidsort(String clientbid) {
        if (clientbid != null) {
            if (clientbid.matches("^[0-9]+$")) {
                // only numbers - save in sort field
                Long longclientid = new Long(clientbid);
                this.clientbookidsort = longclientid;
            }
        }
    }


    public BookDetailDao getBookdetail() {
        if (this.bookdetail == null) {
            this.bookdetail = new BookDetailDao();
        }
        return this.bookdetail;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getClientbookid() {
        return this.clientbookid;
    }

    public Integer getVersion() {
        return this.version;
    }

    public String getNote() {
        return this.note;
    }

    public void setClientshelfcode(Long clientshelfcode) {
        this.clientshelfcode = clientshelfcode;
    }

    public void setClientbooktype(Long clientbooktype) {
        this.clientbooktype = clientbooktype;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCountstatus() {
        return this.countstatus;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Long getStatus() {
        return this.status;
    }

    public void setCreatedon(Date createdon) {
        this.createdon = createdon;
    }

    public void setTocount(Boolean tocount) {
        this.tocount = tocount;
    }

    public void setBarcodeid(String barcodeid) {
        this.barcodeid = barcodeid;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setClientshelfclass(String clientshelfclass) {
        this.clientshelfclass = clientshelfclass;
    }

    public String getBarcodeid() {
        return this.barcodeid;
    }

    public Long getId() {
        return this.id;
    }

    public Long getClientbookidsort() {
        return this.clientbookidsort;
    }

    public void setCountstatus(Long countstatus) {
        this.countstatus = countstatus;
    }

    public void setCounteddate(Date counteddate) {
        this.counteddate = counteddate;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public void setClientid(Long clientid) {
        this.clientid = clientid;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getClientid() {
        return this.clientid;
    }

    public void setBookdetail(BookDetailDao bookdetail) {
        this.bookdetail = bookdetail;
    }

    public Long getUserid() {
        return this.userid;
    }

    public Long getClientshelfcode() {
        return this.clientshelfcode;
    }

    public void setClientbookidsort(Long clientbookidsort) {
        this.clientbookidsort = clientbookidsort;
    }

    public String getClientshelfclass() {
        return this.clientshelfclass;
    }

    public Date getCreatedon() {
        return this.createdon;
    }

    public Date getCounteddate() {
        return this.counteddate;
    }

    public Long getClientbooktype() {
        return this.clientbooktype;
    }

    public Boolean getTocount() {
        return this.tocount;
    }
}
