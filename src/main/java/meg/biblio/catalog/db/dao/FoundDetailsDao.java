package meg.biblio.catalog.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;

@Entity
@Table(name = "found_details")
public class FoundDetailsDao {

    private Long bookdetailid;
    private String title;
    private String authors;
    private String illustrators;
    @Column(length = 2000)
    private String description;
    private String publisher;
    private Long publishyear;
    private String isbn10;
    private String isbn13;
    private String language;
    private String type;
    private String imagelink;
    private String searchserviceid;
    private Long searchsource;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;


    public void setDescription(String description) {
        if (description != null && description.length() > 1510) {
            this.description = description.substring(0, 1510);
        }
        this.description = description;
    }

    public void setIsbn(String isbncode) {
        if (isbncode != null) {
            // remove non numeric characters
            String str = isbncode.replaceAll("[^\\d.X]", "");
            if (str.length() > 10) {
                this.isbn13 = str;
            } else {
                this.isbn10 = str;
            }
        }

    }

    public void setIsbn10(String isbn10) {
        setIsbn(isbn10);
    }

    public void setIsbn13(String isbn13) {
        setIsbn(isbn13);
    }

    public Long getBookdetailid() {
        return this.bookdetailid;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return this.language;
    }

    public String getSearchserviceid() {
        return this.searchserviceid;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public void setSearchserviceid(String searchserviceid) {
        this.searchserviceid = searchserviceid;
    }

    public Long getPublishyear() {
        return this.publishyear;
    }

    public String getIllustrators() {
        return this.illustrators;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getIsbn13() {
        return this.isbn13;
    }

    public Long getId() {
        return this.id;
    }

    public void setPublishyear(Long publishyear) {
        this.publishyear = publishyear;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getType() {
        return this.type;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImagelink(String imagelink) {
        this.imagelink = imagelink;
    }

    public void setIllustrators(String illustrators) {
        this.illustrators = illustrators;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public String getAuthors() {
        return this.authors;
    }

    public String getImagelink() {
        return this.imagelink;
    }

    public void setBookdetailid(Long bookdetailid) {
        this.bookdetailid = bookdetailid;
    }

    public Long getSearchsource() {
        return this.searchsource;
    }

    public String getIsbn10() {
        return this.isbn10;
    }

    public String getPublisher() {
        return this.publisher;
    }

    public void setSearchsource(Long searchsource) {
        this.searchsource = searchsource;
    }
}
