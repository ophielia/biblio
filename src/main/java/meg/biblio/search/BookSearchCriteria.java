package meg.biblio.search;

public class BookSearchCriteria {

	private Long clientid;
	private String keyword;
	private String author;
	private String illustrator;
	private String title;
	private String shelfclasskey;
	private String publisher;
	private long orderby;
	private long orderbydir;




	public static final class OrderBy {
		public static final long PERTINENCE=1;
		public static final long DATEADDED=2;
		public static final long TITLE=3;
		public static final long AUTHOR=4;
		public static final long SHELFCLASS=5;
		
	}
	public static final String usersortlkup="usersort";
	
	public static final class OrderByDir {
		public static final long ASC=1;
		public static final long DESC=2;
	}
	
	
	
	public Long getClientid() {
		return clientid;
	}
	public void setClientid(Long clientid) {
		this.clientid = clientid;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public String getIllustrator() {
		return illustrator;
	}
	public void setIllustrator(String illustrator) {
		this.illustrator = illustrator;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getShelfclasskey() {
		return shelfclasskey;
	}
	public void setShelfclasskey(String shelfclasskey) {
		this.shelfclasskey = shelfclasskey;
	}
	public String getPublisher() {
		return publisher;
	}
	public void setPublisher(String publisherentry) {
		this.publisher = publisherentry;
	}
	
	
	public long getOrderby() {
		if (orderby==0) {
			return getDefaultOrder();
		}
		return orderby;
	}
	public void setOrderby(long orderby) {
		this.orderby = orderby;
	}
	

	public long getOrderbydir() {
		if (orderbydir==0) {
			return getDefaultOrderDir();
		}
		return orderbydir;
	}
	public void setOrderbydir(long orderbydir) {
		this.orderbydir = orderbydir;
	}
	
	private long getDefaultOrder() {
		// default is pertinance if keyword search is used, otherwise, by recent
		if (getKeyword()!=null) {
			return OrderBy.PERTINENCE;
		} else {
			return OrderBy.DATEADDED;
		}
	}
	
	private long getDefaultOrderDir() {
		return OrderByDir.DESC;
		
	}	
}
