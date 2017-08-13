package meg.biblio.inventory.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "inventory")
public class InventoryDao {

    private Long clientid;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date startdate;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(style = "M-")
    private Date enddate;

    private Integer tobecounted;
    private Integer totalcounted;
    private Integer addedtocount;
    private Integer reconciled;
    private Boolean completed;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setClientid(Long clientid) {
        this.clientid = clientid;
    }

    public Long getClientid() {
        return this.clientid;
    }

    public Date getStartdate() {
        return this.startdate;
    }

    public void setStartdate(Date startdate) {
        this.startdate = startdate;
    }

    public Boolean getCompleted() {
        return this.completed;
    }

    public Date getEnddate() {
        return this.enddate;
    }

    public void setAddedtocount(Integer addedtocount) {
        this.addedtocount = addedtocount;
    }

    public Integer getReconciled() {
        return this.reconciled;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }

    public void setEnddate(Date enddate) {
        this.enddate = enddate;
    }

    public Integer getTobecounted() {
        return this.tobecounted;
    }

    public void setTobecounted(Integer tobecounted) {
        this.tobecounted = tobecounted;
    }

    public Integer getTotalcounted() {
        return this.totalcounted;
    }

    public void setTotalcounted(Integer totalcounted) {
        this.totalcounted = totalcounted;
    }

    public void setReconciled(Integer reconciled) {
        this.reconciled = reconciled;
    }

    public Integer getAddedtocount() {
        return this.addedtocount;
    }

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
}
