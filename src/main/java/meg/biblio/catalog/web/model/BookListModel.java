package meg.biblio.catalog.web.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.common.web.model.Pager;
import meg.biblio.search.BookSearchCriteria;

public class BookListModel implements Serializable {

	private static final long serialVersionUID = 1L;

	private BookSearchCriteria criteria;
	private Pager pager;
	
	private List<BookDao> books;
	private List<Boolean> checked;
	private Long statusUpdate;
	private Long shelfclassUpdate;
	private List<Long> idref;
	private int gridColumns=-1; 
	
	
	
	public BookListModel(BookSearchCriteria criteria) {
		super();
		this.criteria = criteria;
		this.pager = new Pager();
	}

	public BookSearchCriteria getCriteria() {
		return criteria;
	}

	public void setCriteria(BookSearchCriteria criteria) {
		this.criteria = criteria;
	}

	
	public Pager getPager() {
		return pager;
	}

	public void setPager(Pager pager) {
		this.pager = pager;
	}

	public List<BookDao> getBooks() {
		return books;
	}

	public void setBooks(List<BookDao> books) {
		this.books = books;
		if (this.books!=null && this.books.size()>0) {
			// initialize checked list
			createCheckedAndIdSlots(this.books.size());
		}		
	}
	
	public List<List<BookDao>> getBookRows() {
		List<List<BookDao>> rows = new ArrayList<List<BookDao>>();
		// go through all books, placing them in rows
		// prepare first row
		List<BookDao> singlerow = new ArrayList<BookDao>();
		int count=0;
		for (BookDao book:this.books) {
			// add book to single row
			singlerow.add(book);
			// check if this is the last column
			if (count==gridColumns-1) {
				// if last column, add singlerow to rows, initialize new singlerow 
				// and reset counter
				rows.add(singlerow);
				singlerow = new ArrayList<BookDao>();
				count=0;
			} else {
				// otherwise, increment counter
				count++;
			}
		}
		// add last row in progress to list of rows
		if (singlerow.size()>0) {
			rows.add(singlerow);
		}
		
		
		return rows;
	}
	
	public boolean getUsesGrid() {
		return (gridColumns > 1);
	}
	
	public int getGridColumns() {
		return gridColumns;
	}

	public void setGridColumns(int gridColumns) {
		this.gridColumns = gridColumns;
	}

	public List<Boolean> getChecked() {
		return checked;
	}

	public void setChecked(List<Boolean> checked) {
		this.checked = checked;
	}
	
	

	public Long getStatusUpdate() {
		return statusUpdate;
	}

	public void setStatusUpdate(Long statusUpdate) {
		this.statusUpdate = statusUpdate;
	}

	public Long getShelfclassUpdate() {
		return shelfclassUpdate;
	}

	public void setShelfclassUpdate(Long shelfclassUpdate) {
		this.shelfclassUpdate = shelfclassUpdate;
	}

	/** Setters on pager object **/

	public Integer getResultsperpage() {
		return pager.getResultsperpage();
	}

	public void setResultsperpage(Integer resultsperpage) {
		 pager.setResultsperpage(resultsperpage);
	}

	public Integer getCurrentpage() {
		return pager.getCurrentpage();
	}

	public void setCurrentpage(Integer currentpage) {
		pager.setCurrentpage(currentpage);
	}	
	
	
	
	public int getResultcount() {
		return pager.getResultcount();
	}

	public void setResultcount(int resultcount) {
		this.pager.setResultcount(resultcount);
	}

	/** Setters on criteria object **/

	public String getClientbookid() {
		return criteria.getClientbookid();
	}


	public void setClientbookid(String clientbookid) {
		criteria.setClientbookid(clientbookid);
	}

	public void setKeyword(String keyword) {
		criteria.setKeyword(keyword);
	}

	public String getKeyword() {
		return criteria.getKeyword();
	}

	public String getAuthor() {
		return criteria.getAuthor();
	}

	public void setAuthor(String author) {
		 criteria.setAuthor( author);
	}

	public String getIllustrator() {
		return criteria.getIllustrator();
	}

	public void setIllustrator(String illustrator) {
		criteria.setIllustrator(illustrator);
	}

	public String getTitle() {
		return criteria.getTitle();
	}

	public void setTitle(String title) {
		criteria.setTitle(title);
	}

	public Long getShelfclasskey() {
		if (criteria.getShelfclasskey()!=null) {
			return criteria.getShelfclasskey();	
		}
		return 0L;
	}

	public void setShelfclasskey(Long shelfclasskey) {
		criteria.setShelfclasskey(shelfclasskey);
	}

	public String getPublisher() {
		return criteria.getPublisher();
	}

	public void setPublisher(String publisherentry) {
		criteria.setPublisher(publisherentry);
	}

	public Long getStatus() {
		return criteria.getSingleStatus();
	}

	public void setStatus(Long status) {
		criteria.setSingleStatus(status);
	}

	public Long getDetailstatus() {
		return criteria.getDetailstatus();
	}

	public void setDetailstatus(Long detailstatus) {
		criteria.setDetailstatus(detailstatus);
	}

	public Long getBooktype() {
		return criteria.getBooktype();
	}

	public void setBooktype(Long booktype) {
		criteria.setBooktype(booktype);
	}

	public long getOrderby() {
		return criteria.getOrderby();
	}

	public void setOrderby(long orderby) {
		criteria.setOrderby(orderby);
	}

	public long getOrderbydir() {
		return criteria.getOrderbydir();
	}

	public void setOrderbydir(long orderbydir) {
		criteria.setOrderbydir(orderbydir);
	}

	private void createCheckedAndIdSlots(int size) {
		checked = new ArrayList<Boolean>();
		idref = new ArrayList<Long>();
		for (int i=0;i<size;i++) {
			checked.add(false);
			idref.add(0L);
		}
		
	}
	public List<Long> getIdref() {
		return idref;
	}

	public void setIdref(List<Long> idref) {
		this.idref = idref;
	}	
	
	
	public boolean getHasNext() {
		return pager.hasNext();
	}
	
	
	public boolean getHasPrevious() {
		return pager.hasPrevious();
	}
	
	

	public List<Long> getCheckedBookIds() {
		// make new empty list 
		List<Long> checkedexp = new ArrayList<Long>();
			// go through checked list
			for (int i=0;i<checked.size();i++) {
				// if checked is true, add expenseDao at same slot to checkedlist
				Boolean test = checked.get(i);
				if (test!=null && test) {
					checkedexp.add(idref.get(i));
				}
			}
		// return checked list
		return checkedexp;
	}	
}
