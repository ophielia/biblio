package meg.biblio.catalog.db.dao;
import java.util.ArrayList;
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
@Table(name="bookdetail")
public class BookDetailDao {
	
	
	@NotNull
	@Size(min=1,message="field_required")
	private String title;

	@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinTable(
	    name="BOOK_AUTHOR",
	    joinColumns=@JoinColumn(name="bookdetail_id", referencedColumnName="id"),
	    inverseJoinColumns={@JoinColumn(name="artist_id", referencedColumnName="id")})
	@OrderColumn(name="authororder")
	private List<ArtistDao> authors;

	@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinTable(
		    name="BOOK_ILLUSTRATOR",
		    joinColumns=@JoinColumn(name="bookdetail_id", referencedColumnName="id"),
		    inverseJoinColumns={@JoinColumn(name="artist_id", referencedColumnName="id")})
		@OrderColumn(name="illustorder")
		private List<ArtistDao> illustrators;

	@ManyToMany(cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinTable(
		    name="BOOK_SUBJECT",
		    joinColumns=@JoinColumn(name="bookdetail_id", referencedColumnName="id"),
		    inverseJoinColumns={@JoinColumn(name="subject_id", referencedColumnName="id")})
		@OrderColumn(name="subjectorder")
		private List<SubjectDao> subjects;

	@OneToOne(cascade = CascadeType.ALL, fetch=FetchType.EAGER)/*@JoinColumn(name="ID")*/
	private PublisherDao publisher;
	private Long publishyear;
	private String isbn10;
	private String isbn13;
	private String language;
	private String imagelink;
	@Column( length=2000)
	private String description;
	@OneToMany(mappedBy = "bookdetail",cascade = CascadeType.ALL, fetch=FetchType.LAZY)
	private List<FoundWordsDao> foundwords;
	private Long detailstatus;
	private Long finderlog;
	private Long listedtype;
	private String shelfclass;
	
	@Transient
	private Boolean textchange=new Boolean(false);
	
	
	public boolean hasIsbn() {
		boolean hasisbn = (isbn10!=null && isbn10.length()>0) || (isbn13!=null &&isbn13.length()>0);
		return hasisbn;
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

	

	public Long getFinderlog() {
        if (this.finderlog!=null) {
        	return this.finderlog;	
        }
		return 1L;
    }

	public List<ArtistDao> getAuthors() {
        if (this.authors==null) {
        	this.authors = new ArrayList<ArtistDao>();
        }
    	return this.authors;
    }

	public List<ArtistDao> getIllustrators() {
        if (this.illustrators==null) {
        	this.illustrators = new ArrayList<ArtistDao>();
        }
    	return this.illustrators;
    }

	public boolean hasAuthor() {
		
		return this.authors!=null && this.authors.size()>0;
	}
	
	public BookDetailDao clone() {
		BookDetailDao copy = new BookDetailDao();
		
		copy.authors=authors;
		copy.foundwords=foundwords;
		copy.detailstatus=detailstatus;
		copy.finderlog=finderlog;
		copy.listedtype=listedtype;
		copy.publishyear=publishyear;
		copy.publisher=publisher;
		copy.description=description;
		copy.imagelink=imagelink;
		copy.isbn10=isbn10;
		copy.isbn13=isbn13;
		copy.language=language;
		copy.shelfclass=shelfclass;
		copy.title=title;
		
		return copy;
	}


}
