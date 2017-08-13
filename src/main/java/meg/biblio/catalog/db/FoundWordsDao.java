package meg.biblio.catalog.db;

import meg.biblio.catalog.db.dao.BookDetailDao;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;

@Entity
@Table(name = "foundwords")
public class FoundWordsDao {

    @ManyToOne
    private BookDetailDao bookdetail;
    private String word;
    private Integer countintext;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public void copyFrom(FoundWordsDao copyfrom) {
        if (copyfrom != null) {
            if (copyfrom.word != null) {
                this.word = copyfrom.word;
            }
            if (copyfrom.countintext != null) {
                this.countintext = copyfrom.countintext;
            }
        }
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
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

    public BookDetailDao getBookdetail() {
        return this.bookdetail;
    }

    public void setBookdetail(BookDetailDao bookdetail) {
        this.bookdetail = bookdetail;
    }

    public String getWord() {
        return this.word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Integer getCountintext() {
        return this.countintext;
    }

    public void setCountintext(Integer countintext) {
        this.countintext = countintext;
    }
}
