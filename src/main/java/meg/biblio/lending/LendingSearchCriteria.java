package meg.biblio.lending;

import java.util.Date;

public class LendingSearchCriteria {

	public final static class SearchType {
		public final static Long CHECKEDOUT=1L;
		public final static Long RETURNED=2L;
	}

	public final static class LentToType {
		public final static Long TEACHER=1L;
		public final static Long STUDENT=2L;
		public final static Long BOTH=3L;
	}

	private Date checkedouton;
	private Date returnedon;
	private Long forschoolgroup;
	private Long lentToType;
	private Boolean overdueonly;
	private Long clientid;
	private Long borrowerid;
	private Long bookid;
	private Boolean checkedout;


	public Date getCheckedouton() {
		return checkedouton;
	}
	public void setCheckedouton(Date checkedouton) {
		this.checkedouton = checkedouton;
	}
	
	
	public Boolean getCheckedoutOnly() {
		return checkedout;
	}
	public void setCheckedoutOnly(Boolean checkedout) {
		this.checkedout = checkedout;
	}
	public Date getReturnedon() {
		return returnedon;
	}
	public void setReturnedon(Date returnedon) {
		this.returnedon = returnedon;
	}
	
	public Long getBookid() {
		return bookid;
	}
	public void setBookid(Long bookid) {
		this.bookid = bookid;
	}
	public Long getSchoolgroup() {
		return forschoolgroup;
	}
	public void setSchoolgroup(Long forschoolgroup) {
		this.forschoolgroup = forschoolgroup;
	}
	public Long getLentToType() {
		return lentToType;
	}
	public void setLentToType(Long lentTo) {
		this.lentToType = lentTo;
	}
	public Boolean getOverdueOnly() {
		return overdueonly;
	}
	public void setOverdueOnly(Boolean overdueonly) {
		this.overdueonly = overdueonly;
	}

	public Long getClientid() {
		return clientid;
	}
	public void setClientid(Long clientid) {
		this.clientid = clientid;
	}

	public Long getBorrowerid() {
		return borrowerid;
	}
	public void setBorrowerid(Long borrowerid) {
		this.borrowerid = borrowerid;
	}
	public void reset() {
		  checkedouton=null;
		  returnedon=null;
		  forschoolgroup=null;
		  lentToType=null;
		  overdueonly=null;
		  clientid=null;
		  borrowerid=null;
		  bookid=null;
		  checkedout=null;
		
	}


}
