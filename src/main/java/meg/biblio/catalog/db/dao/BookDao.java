package meg.biblio.catalog.db.dao;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@RooJavaBean
@RooToString
@RooJpaEntity
@Table(name="book")
public class BookDao {

private Long clientid;

@NotNull
@Size(min=1,message="field_required")
private String title;

@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
@JoinTable(
    name="BOOK_AUTHOR",
    joinColumns=@JoinColumn(name="book_id", referencedColumnName="id"),
    inverseJoinColumns={@JoinColumn(name="artist_id", referencedColumnName="id")})
@OrderColumn(name="authororder")
private List<ArtistDao> authors;

@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
@JoinTable(
	    name="BOOK_ILLUSTRATOR",
	    joinColumns=@JoinColumn(name="book_id", referencedColumnName="id"),
	    inverseJoinColumns={@JoinColumn(name="artist_id", referencedColumnName="id")})
	@OrderColumn(name="illustorder")
	private List<ArtistDao> illustrators;

@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
@JoinTable(
	    name="BOOK_SUBJECT",
	    joinColumns=@JoinColumn(name="book_id", referencedColumnName="id"),
	    inverseJoinColumns={@JoinColumn(name="subject_id", referencedColumnName="id")})
	@OrderColumn(name="subjectorder")
	private List<SubjectDao> subjects;

@OneToOne
@JoinColumn(name="ID")
private PublisherDao publisher;
private Long publishyear;
private String isbn10;
private String isbn13;
private Long language;
private Long type;
private String description;
private Long status;
private Long detailstatus;
private Long shelfclass;
private Boolean shelfclassverified;
private Date createdon;
private String clientbookid;
}
