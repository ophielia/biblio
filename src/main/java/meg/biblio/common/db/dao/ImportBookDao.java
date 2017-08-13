package meg.biblio.common.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;

@Entity
public class ImportBookDao {

    private String clientbookid;
    private String title;
    private String author;
    private String illustrator;
    private String publisher;
    private String isbn10;
    private String isbn13;
    private String barcode;
    private String error;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public String getIsbn13() {
        return this.isbn13;
    }

    public String getClientbookid() {
        return this.clientbookid;
    }

    public String getError() {
        return this.error;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Long getId() {
        return this.id;
    }

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getIsbn10() {
        return this.isbn10;
    }

    public String getTitle() {
        return this.title;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setClientbookid(String clientbookid) {
        this.clientbookid = clientbookid;
    }

    public String getIllustrator() {
        return this.illustrator;
    }

    public void setIllustrator(String illustrator) {
        this.illustrator = illustrator;
    }

    public String getBarcode() {
        return this.barcode;
    }
}
