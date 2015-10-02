package meg.biblio.search;

import java.util.List;

public class BookSearchCriteria {

	private Long clientid;
	private String clientbookid;
	private String keyword;
	private String author;
	private String illustrator;
	private String title;
	private Long shelfclasskey;
	private String publisher;
	private Long status;
	private List<Long> statuslist;
	private Boolean instatuslist;
	private Long detailstatus;
	private Long booktype;
	private Boolean clientspecific;
	private String isbn10;
	private String isbn13;
	private long orderby;
	private long orderbydir;

	public static final class OrderBy {
		public static final long PERTINENCE = 1;
		public static final long DATEADDED = 2;
		public static final long TITLE = 3;
		public static final long AUTHOR = 4;
		public static final long SHELFCLASS = 5;
		public static final long BOOKID = 6;
		public static final long BOOKTYPE = 7;
		public static final long STATUS = 8;
		public static final long DETAILSTATUS = 9;
	}

	public static final String usersortlkup = "usersort";

	public static final class OrderByDir {
		public static final long ASC = 1;
		public static final long DESC = 2;
	}

	public Long getClientid() {
		return clientid;
	}

	public void setClientid(Long clientid) {
		this.clientid = clientid;
	}

	
	public String getClientbookid() {
		return clientbookid;
	}

	public void setClientbookid(String clientbookid) {
		this.clientbookid = clientbookid;
	}

	public boolean hasKeyword() {
		return keyword != null && keyword.trim().length() > 0;
	}
	
	public boolean hasClientspecific() {
		return clientspecific != null;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public boolean hasAuthor() {
		return author != null && author.trim().length() > 0;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public boolean hasIllustrator() {
		return illustrator != null && illustrator.trim().length() > 0;
	}

	public String getIllustrator() {
		return illustrator;
	}

	public void setIllustrator(String illustrator) {
		this.illustrator = illustrator;
	}

	public boolean hasTitle() {
		return title != null && title.trim().length() > 0;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	// MM change to shelf code
	public boolean hasShelfclasskey() {
		return shelfclasskey != null && shelfclasskey.longValue() > 0;
	}

	public Long getShelfclasskey() {
		return shelfclasskey;
	}

	public void setShelfclasskey(Long shelfclasskey) {
		this.shelfclasskey = shelfclasskey;
	}

	public boolean hasStatusList() {
		return this.statuslist != null && this.statuslist.size() > 0;
	}
	
	
	public List<Long> getStatuslist() {
		return statuslist;
	}

	public void setStatuslist(List<Long> statuslist) {
		this.statuslist = statuslist;
	}

	
	public Boolean getInstatuslist() {
		return instatuslist;
	}

	public void setInstatuslist(Boolean inlist) {
		this.instatuslist = inlist;
	}

	public boolean hasSingleStatus() {
		return this.status != null && this.status.longValue() > 0;
	}

	public Long getSingleStatus() {
		return this.status;
	}

	public void setSingleStatus(Long status) {
		this.status = status;
	}

	public Boolean getClientspecific() {
		return clientspecific;
	}

	public void setClientspecific(Boolean clientspecific) {
		this.clientspecific = clientspecific;
	}

	public boolean hasDetailstatus() {
		return this.detailstatus != null && this.detailstatus.longValue() > 0;
	}

	public Long getDetailstatus() {
		return detailstatus;
	}

	public void setDetailstatus(Long detailstatus) {
		this.detailstatus = detailstatus;
	}

	public boolean hasPublisher() {
		return this.publisher != null && this.publisher.length() > 0;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisherentry) {
		this.publisher = publisherentry;
	}

	public boolean hasBooktype() {
		return this.booktype != null && this.booktype.longValue() > 0;
	}

	public Long getBooktype() {
		return booktype;
	}

	public void setBooktype(Long booktype) {
		this.booktype = booktype;
	}

	public long getOrderby() {
		if (orderby == 0) {
			return getDefaultOrder();
		}
		return orderby;
	}

	public void setOrderby(long orderby) {
		this.orderby = orderby;
	}

	public long getOrderbydir() {
		if (orderbydir == 0) {
			return getDefaultOrderDir();
		}
		return orderbydir;
	}

	public void setOrderbydir(long orderbydir) {
		this.orderbydir = orderbydir;
	}

	private long getDefaultOrder() {
		// default is pertinance if keyword search is used, otherwise, by recent
		if (hasKeyword()) {
			return OrderBy.PERTINENCE;
		} else {
			return OrderBy.BOOKID;
		}
	}

	private long getDefaultOrderDir() {
		if (orderby == OrderBy.TITLE || orderby == OrderBy.AUTHOR) {
			return OrderByDir.ASC;
		}
		return OrderByDir.DESC;

	}

	public void setIsbn10(String isbn) {
		this.isbn10=isbn;
		
	}

	public String getIsbn10() {
		return isbn10;
	}

	public void setIsbn13(String isbn) {
		this.isbn13=isbn;
	}

	public String getIsbn13() {
		return isbn13;
	}

	public boolean hasClientbookid() {
		// TODO Auto-generated method stub
		return clientbookid!=null && clientbookid.trim().length()>0;
	}
	
	
}
