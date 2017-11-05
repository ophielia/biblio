package meg.biblio.lending.db.dao;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.db.dao.ClientDao;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "loanrecord")
public class LoanRecordDao {

    @OneToOne
    @JoinColumn(name="client")
    private ClientDao client;

    @OneToOne
    @JoinColumn(name="book")
    private BookDao book;

    @OneToOne
    @JoinColumn(name="borrower")
    private PersonDao borrower;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date checkoutdate;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date duedate;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date returned;

    private Integer schoolyear;

    private Long teacherid;

    private Long borrowersection;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public PersonDao getBorrower() {
        return this.borrower;
    }

    public ClientDao getClient() {
        return this.client;
    }

    public void setClient(ClientDao client) {
        this.client = client;
    }

    public BookDao getBook() {
        return this.book;
    }

    public void setBook(BookDao book) {
        this.book = book;
    }

    public void setBorrowersection(Long borrowersection) {
        this.borrowersection = borrowersection;
    }

    public void setBorrower(PersonDao borrower) {
        this.borrower = borrower;
    }

    public Date getCheckoutdate() {
        return this.checkoutdate;
    }

    public Integer getSchoolyear() {
        return this.schoolyear;
    }

    public void setDuedate(Date duedate) {
        this.duedate = duedate;
    }

    public void setTeacherid(Long teacherid) {
        this.teacherid = teacherid;
    }

    public void setCheckoutdate(Date checkoutdate) {
        this.checkoutdate = checkoutdate;
    }

    public Date getReturned() {
        return this.returned;
    }

    public Date getDuedate() {
        return this.duedate;
    }

    public void setReturned(Date returned) {
        this.returned = returned;
    }

    public void setSchoolyear(Integer schoolyear) {
        this.schoolyear = schoolyear;
    }

    public Long getTeacherid() {
        return this.teacherid;
    }

    public Long getBorrowersection() {
        return this.borrowersection;
    }
}
