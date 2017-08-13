package meg.biblio.catalog.db.dao;

import meg.biblio.catalog.db.FoundWordsDao;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookdetail")
public class BookDetailDao {


    @NotNull
    @Size(min = 1, message = "field_required")
    private String title;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "BOOK_AUTHOR",
            joinColumns = @JoinColumn(name = "bookdetail_id", referencedColumnName = "id"),
            inverseJoinColumns = {@JoinColumn(name = "artist_id", referencedColumnName = "id")})
    @OrderColumn(name = "authororder")
    private List<ArtistDao> authors;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "BOOK_ILLUSTRATOR",
            joinColumns = @JoinColumn(name = "bookdetail_id", referencedColumnName = "id"),
            inverseJoinColumns = {@JoinColumn(name = "artist_id", referencedColumnName = "id")})
    @OrderColumn(name = "illustorder")
    private List<ArtistDao> illustrators;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
            name = "BOOK_SUBJECT",
            joinColumns = @JoinColumn(name = "bookdetail_id", referencedColumnName = "id"),
            inverseJoinColumns = {@JoinColumn(name = "subject_id", referencedColumnName = "id")})
    @OrderColumn(name = "subjectorder")
    private List<SubjectDao> subjects;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)/*@JoinColumn(name="ID")*/
    private PublisherDao publisher;
    private Long publishyear;
    private String isbn10;
    private String isbn13;
    private String language;
    @Column(length = 500)
    private String imagelink;
    @Column(length = 2000)
    private String description;
    @OneToMany(mappedBy = "bookdetail", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FoundWordsDao> foundwords;
    private Long detailstatus;
    private Long finderlog;
    private Long listedtype;
    private String shelfclass;
    private String ark;
    private Boolean clientspecific = false;

    @Transient
    private Boolean textchange = new Boolean(false);

    @Transient
    private Boolean trackchange = new Boolean(false);
    @Version
    @Column(name = "version")
    private Integer version;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    public boolean hasIsbn() {
        boolean hasisbn = (isbn10 != null && isbn10.length() > 0) || (isbn13 != null && isbn13.length() > 0);
        return hasisbn;
    }

    public boolean getHasImage() {
        boolean hasimage = (imagelink != null && imagelink.length() > 0);
        return hasimage;
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

    public String getAuthorsAsString() {
        if (this.authors != null && this.authors.size() > 0) {
            StringBuffer authorlist = new StringBuffer();
            for (ArtistDao author : this.authors) {
                authorlist.append(author.getDisplayName()).append(", ");
            }
            // remove last comma and space
            authorlist.setLength(authorlist.length() - 2);
            return authorlist.toString();
        }
        return "";
    }

    public String getIllustratorsAsString() {
        if (this.illustrators != null && this.illustrators.size() > 0) {
            StringBuffer illustratorlist = new StringBuffer();
            for (ArtistDao illustrator : this.illustrators) {
                illustratorlist.append(illustrator.getDisplayName()).append(", ");
            }
            // remove last comma and space
            illustratorlist.setLength(illustratorlist.length() - 2);
            return illustratorlist.toString();
        }
        return "";
    }


    public void setAuthors(List<ArtistDao> authors) {
        if (authors != null) {
            if (this.authors == null || !artistListsEqual(this.authors, authors)) {
                setClientspecific(true);
                setTextchange(true);
            }
        } else if (this.authors != null) {
            setClientspecific(true);
            setTextchange(true);
        }


        this.authors = authors;
    }


    private boolean artistListsEqual(List<ArtistDao> listone, List<ArtistDao> listtwo) {
        if (listone != null && listtwo != null) {
            int size = listone.size();
            if (size != listtwo.size()) {
                return false;
            }
            StringBuffer testone = new StringBuffer();
            StringBuffer testtwo = new StringBuffer();
            for (int i = 0; i < size; i++) {
                testone.append(listone.get(i).getDisplayName());
                testtwo.append(listtwo.get(i).getDisplayName());
            }
            return testone.toString().equals(testtwo.toString());
        } else {
            return (listone != null || listtwo != null);
        }
    }

    private boolean subjectListsEqual(List<SubjectDao> listone, List<SubjectDao> listtwo) {
        if (listone != null && listtwo != null) {
            int size = listone.size();
            if (size != listtwo.size()) {
                return false;
            }
            StringBuffer testone = new StringBuffer();
            StringBuffer testtwo = new StringBuffer();
            for (int i = 0; i < size; i++) {
                testone.append(listone.get(i).getListing());
                testtwo.append(listtwo.get(i).getListing());
            }
            return testone.toString().equals(testtwo.toString());
        } else {
            return (listone != null || listtwo != null);
        }
    }

    public void setDescription(String description) {
        if (description != null && description.trim().length() > 0) {
            if (this.description == null || !this.description.equals(description)) {
                setTextchange(true);
                setClientspecific(true);
            }
        } else if (this.description != null && this.description.trim().length() > 0) {
            setTextchange(true);
            setClientspecific(true);
        }

        if (description != null && description.length() > 1510) {
            this.description = description.substring(0, 1510);
        }
        this.description = description;
    }

    public void setIllustrators(List<ArtistDao> illustrators) {
        if (illustrators != null) {
            if (this.illustrators == null || !artistListsEqual(this.illustrators, illustrators)) {
                setClientspecific(true);
                setTextchange(true);
            }
        } else if (this.illustrators != null && this.illustrators.size() > 0) {
            setClientspecific(true);
            setTextchange(true);
        }
        this.illustrators = illustrators;
    }

    public void setTitle(String title) {
        if (title != null) {
            if (this.title == null || !this.title.equals(title)) {
                setTextchange(true);
                setClientspecific(true);
            }
        } else if (this.title != null) {
            setTextchange(true);
            setClientspecific(true);
        }

        this.title = title;
    }

    public void setLanguage(String language) {
        if (language != null) {
            if (this.language == null || !this.language.equals(language)) {
                setTextchange(true);
                setClientspecific(true);
            }
        } else if (this.language != null) {
            setTextchange(true);
            setClientspecific(true);
        }


        this.language = language;
    }

    public void setPublisher(PublisherDao publisher) {
        if (this.publisher != publisher) {
            setClientspecific(true);
        }
        this.publisher = publisher;
    }

    public void setPublishyear(Long publishyear) {
        if (publishyear != null) {
            if (this.publishyear == null || this.publishyear.longValue() != publishyear.longValue()) {
                setClientspecific(true);
            }
        } else if (this.publishyear != null) {
            setClientspecific(true);
        }

        this.publishyear = publishyear;
    }

    public void setSubjects(List<SubjectDao> subjects) {

        if (subjects != null) {
            if (this.subjects == null || !subjectListsEqual(this.subjects, subjects)) {
                setClientspecific(true);
                setTextchange(true);
            }
        } else if (this.subjects != null && this.subjects.size() > 0) {
            setClientspecific(true);
            setTextchange(true);
        }
        this.subjects = subjects;
    }


    public Long getFinderlog() {
        if (this.finderlog != null) {
            return this.finderlog;
        }
        return 1L;
    }

    public List<ArtistDao> getAuthors() {
        if (this.authors == null) {
            this.authors = new ArrayList<ArtistDao>();
        }
        return this.authors;
    }

    public List<ArtistDao> getIllustrators() {
        if (this.illustrators == null) {
            this.illustrators = new ArrayList<ArtistDao>();
        }
        return this.illustrators;
    }

    public boolean hasAuthor() {
        return this.authors != null && this.authors.size() > 0;
    }

    public boolean getHasIllustrator() {
        return this.illustrators != null && this.illustrators.size() > 0;
    }

    public void copyFrom(BookDetailDao copyfrom) {
        if (copyfrom != null) {
            if (copyfrom.authors != null) {
                this.authors = copyfrom.authors;
            }
            if (copyfrom.detailstatus != null) {
                this.detailstatus = copyfrom.detailstatus;
            }
            if (copyfrom.finderlog != null) {
                this.finderlog = copyfrom.finderlog;
            }
            if (copyfrom.listedtype != null) {
                this.listedtype = copyfrom.listedtype;
            }
            if (copyfrom.publishyear != null) {
                this.publishyear = copyfrom.publishyear;
            }
            if (copyfrom.publisher != null) {
                this.publisher = copyfrom.publisher;
            }

            if (copyfrom.description != null) {
                this.description = copyfrom.description;
            }

            if (copyfrom.imagelink != null) {
                this.imagelink = copyfrom.imagelink;
            }
            if (copyfrom.isbn10 != null) {
                this.isbn10 = copyfrom.isbn10;
            }
            if (copyfrom.isbn13 != null) {
                this.isbn13 = copyfrom.isbn13;
            }
            if (copyfrom.language != null) {
                this.language = copyfrom.language;
            }

            if (copyfrom.shelfclass != null) {
                this.shelfclass = copyfrom.shelfclass;
            }

            if (copyfrom.title != null) {
                this.title = copyfrom.title;
            }

        }
    }


    public Boolean getClientspecific() {
        return this.clientspecific;
    }

    public void setClientspecific(Boolean clientspecific) {
        if (this.trackchange) {
            this.clientspecific = clientspecific;
        }
    }

    public void setIsbn10(String isbn10) {
        setIsbn(isbn10);
    }

    public void setIsbn13(String isbn13) {
        setIsbn(isbn13);
    }

    public String getTitle() {
        return this.title;
    }

    public String getTitleForDisplay() {
        if (this.title != null && this.title.length() > 75) {
            return this.title.substring(0, 74) + "...";
        }
        return this.title;
    }

    public void setShelfclass(String shelfclass) {
        this.shelfclass = shelfclass;
    }

    public void setArk(String ark) {
        this.ark = ark;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPublishyear() {
        return this.publishyear;
    }

    public Integer getVersion() {
        return this.version;
    }

    public String getShelfclass() {
        return this.shelfclass;
    }

    public void setImagelink(String imagelink) {
        this.imagelink = imagelink;
    }

    public Boolean getTrackchange() {
        return this.trackchange;
    }

    public String getIsbn13() {
        return this.isbn13;
    }

    public List<SubjectDao> getSubjects() {
        return this.subjects;
    }

    public Long getDetailstatus() {
        return this.detailstatus;
    }

    public PublisherDao getPublisher() {
        return this.publisher;
    }

    public void setTrackchange(Boolean trackchange) {
        this.trackchange = trackchange;
    }

    public String getImagelink() {
        return this.imagelink;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getArk() {
        return this.ark;
    }

    public Long getId() {
        return this.id;
    }

    public void setFoundwords(List<FoundWordsDao> foundwords) {
        this.foundwords = foundwords;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setFinderlog(Long finderlog) {
        this.finderlog = finderlog;
    }

    public Long getListedtype() {
        return this.listedtype;
    }

    public void setDetailstatus(Long detailstatus) {
        this.detailstatus = detailstatus;
    }

    public String getIsbn10() {
        return this.isbn10;
    }

    public void setListedtype(Long listedtype) {
        this.listedtype = listedtype;
    }

    public Boolean getTextchange() {
        return this.textchange;
    }

    public void setTextchange(Boolean textchange) {
        this.textchange = textchange;
    }

    public List<FoundWordsDao> getFoundwords() {
        return this.foundwords;
    }
}
