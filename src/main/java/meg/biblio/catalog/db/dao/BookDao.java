package meg.biblio.catalog.db.dao;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import meg.biblio.catalog.db.FoundWordsDao;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

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

@OneToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER)/*@JoinColumn(name="ID")*/
private PublisherDao publisher;
private Long publishyear;
private String isbn10;
private String isbn13;
private String language;
private Long type;

@Column( length=2000)
private String description;
private Long status;
private Long detailstatus;
private Long shelfclass;
private Boolean shelfclassverified;
private Date createdon;
private String clientbookid;
private Long clientbookidsort;
private String barcodeid;
@OneToMany(mappedBy = "book",cascade = CascadeType.ALL, fetch=FetchType.LAZY)
private List<FoundWordsDao> foundwords;
@Transient
private Boolean textchange=new Boolean(false);


	public void setAuthors(List<ArtistDao> authors) {
        if (this.authors != authors) setTextchange(true);
		this.authors = authors;
    }

	public void setDescription(String description) {
		if (this.description != description) setTextchange(true);
		if (description!=null && description.length()>1510) {
			this.description = description.substring(0,1510);	
		}
		this.description = description;
    }

	public void setIllustrators(List<ArtistDao> illustrators) {
		if (this.illustrators != illustrators) setTextchange(true);
		this.illustrators = illustrators;
    }

	public void setTitle(String title) {
		if (this.title != title) setTextchange(true);
		
		this.title = title;
    }
	
	public String getAuthorsAsString() {
		if (this.authors!=null && this.authors.size()>0) {
			StringBuffer authorlist = new StringBuffer();
			for (ArtistDao author:this.authors ) {
				authorlist.append(author.getDisplayName()).append(", ");
			}
			// remove last comma and space
			authorlist.setLength(authorlist.length()-2);
			return authorlist.toString();
		}
		return "";
	}
	
	public String getIllustratorsAsString() {
		if (this.illustrators!=null && this.illustrators.size()>0) {
			StringBuffer illustratorlist = new StringBuffer();
			for (ArtistDao illustrator:this.illustrators ) {
				illustratorlist.append(illustrator.getDisplayName()).append(", ");
			}
			// remove last comma and space
			illustratorlist.setLength(illustratorlist.length()-2);
			return illustratorlist.toString();
		}
		return "";
	}

	public void setClientbookid(String clientbookid) {
        this.clientbookid = clientbookid;
        setClientbookidsort(clientbookid);
    }
	
	public void setClientbookidsort(String clientbid) {
		if (clientbid!=null) {
			if (clientbid.matches("^[0-9]+$")) {
				// only numbers - save in sort field
				Long longclientid = new Long(clientbid);
				setClientbookidsort(longclientid);
			}
		}
	}

	public boolean hasIsbn() {
		boolean hasisbn = (isbn10!=null && isbn10.length()>0) || (isbn13!=null &&isbn13.length()>0);
		return hasisbn;
	}
}
