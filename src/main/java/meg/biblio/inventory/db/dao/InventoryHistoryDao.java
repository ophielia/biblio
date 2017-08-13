package meg.biblio.inventory.db.dao;

import meg.biblio.catalog.db.dao.BookDao;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;

@Entity
@Table(name = "inventoryhistory")
public class InventoryHistoryDao {

    @ManyToOne
    private InventoryDao inventory;

    @OneToOne(fetch = FetchType.EAGER)
    private BookDao book;

    private Long originalstatus;

    private Long newstatus;

    private Boolean foundbook;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;


    public void setInventory(InventoryDao inventory) {
        this.inventory = inventory;
    }

    public InventoryDao getInventory() {
        return this.inventory;
    }

    public BookDao getBook() {
        return this.book;
    }

    public void setBook(BookDao book) {
        this.book = book;
    }

    public Long getOriginalstatus() {
        return this.originalstatus;
    }

    public void setFoundbook(Boolean foundbook) {
        this.foundbook = foundbook;
    }

    public void setOriginalstatus(Long originalstatus) {
        this.originalstatus = originalstatus;
    }

    public Long getNewstatus() {
        return this.newstatus;
    }

    public void setNewstatus(Long newstatus) {
        this.newstatus = newstatus;
    }

    public Boolean getFoundbook() {
        return this.foundbook;
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

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
