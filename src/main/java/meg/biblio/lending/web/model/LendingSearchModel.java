package meg.biblio.lending.web.model;

import meg.biblio.lending.LendingSearchCriteria;


public class LendingSearchModel {

	private LendingSearchCriteria criteria;
	
	private Long classselect;
	private Long timeselect;
	private Long lendtypeselect;
	
	
	
	public LendingSearchModel() {
		super();
		this.criteria = new LendingSearchCriteria();
	}
	
	
	public Long getClassselect() {
		return classselect;
	}
	public void setClassselect(Long classselect) {
		this.classselect = classselect;
		if (classselect!=null) {
			if (criteria==null) {
				this.criteria = new LendingSearchCriteria();
			}
			this.criteria.setClassselect(classselect);
		}
	}
	public Long getTimeselect() {
		return timeselect;
	}
	public void setTimeselect(Long timeselect) {
		this.timeselect = timeselect;
		if (timeselect!=null) {
			if (criteria==null) {
				this.criteria = new LendingSearchCriteria();
			}
			this.criteria.setTimeselect(timeselect);
		}		
	}
	public Long getLendtypeselect() {
		return lendtypeselect;
	}
	public void setLendtypeselect(Long lendtypeselect) {
		this.lendtypeselect = lendtypeselect;
		if (lendtypeselect!=null) {
			if (criteria==null) {
				this.criteria = new LendingSearchCriteria();
			}
			this.criteria.setLendtypeselect(lendtypeselect);
		}			
	}
	public LendingSearchCriteria getCriteria() {
		return criteria;
	}
	public void setCriteria(LendingSearchCriteria criteria) {
		this.criteria = criteria;
	}
	
	
	
}
