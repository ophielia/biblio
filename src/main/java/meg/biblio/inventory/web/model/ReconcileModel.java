package meg.biblio.inventory.web.model;

import java.util.ArrayList;
import java.util.List;

import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.inventory.InventoryStatus;
import meg.biblio.inventory.db.dao.InvStackDisplay;

public class ReconcileModel {

	private InventoryStatus inventoryStatus;
	List<InvStackDisplay> uncountedBooks;
	private boolean isComplete;
	private BookDao reconcileBook;
	private List<Long> idref;
	private List<Boolean> checked;
	private Long updateStatus;
	private String note;
	private Integer maxUncounted;
	private int totalUncounted;

	public InventoryStatus getInventoryStatus() {
		return inventoryStatus;
	}

	public void setInventoryStatus(InventoryStatus inventoryStatus) {
		this.inventoryStatus = inventoryStatus;
	}

	public List<InvStackDisplay> getUncountedBooks() {
		return uncountedBooks;
	}

	public void setUncountedBooks(List<InvStackDisplay> uncountedBooks) {
		this.uncountedBooks = uncountedBooks;
		if (this.uncountedBooks != null && this.uncountedBooks.size() > 0) {
			// initialize checked list
			createCheckedAndIdSlots(this.uncountedBooks.size());
		}
	}

	public void setInventoryComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	public boolean isComplete() {
		return isComplete;
	}

	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	public void setReconcileBook(BookDao book) {
		this.reconcileBook = book;

	}

	public BookDao getReconcileBook() {
		return reconcileBook;
	}

	public List<Boolean> getChecked() {
		return checked;
	}

	public void setChecked(List<Boolean> checked) {
		this.checked = checked;
	}

	public Long getUpdateStatus() {
		return this.updateStatus;
	}

	public void setUpdateStatus(Long updateStatus) {
		this.updateStatus = updateStatus;
	}

	public String getNote() {
		if (this.reconcileBook!=null) {
			return this.reconcileBook.getNote();
		}
		return this.note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public List<Long> getIdref() {
		return idref;
	}

	public void setIdref(List<Long> idref) {
		this.idref = idref;
	}

	private void createCheckedAndIdSlots(int size) {
		checked = new ArrayList<Boolean>();
		idref = new ArrayList<Long>();
		for (int i = 0; i < size; i++) {
			checked.add(false);
			idref.add(0L);
		}

	}

	public List<Long> getCheckedBookIds() {
		// make new empty list
		List<Long> checkedids = new ArrayList<Long>();
		// go through checked list
		for (int i = 0; i < checked.size(); i++) {
			// if checked is true, add id at same slot to checkedlist
			Boolean test = checked.get(i);
			if (test != null && test) {
				checkedids.add(idref.get(i));
			}
		}
		// return checked list
		return checkedids;
	}

	public int getTotalUncounted() {
		return totalUncounted;
	}

	public void setTotalUncounted(int totaltoreconcile) {
		this.totalUncounted = totaltoreconcile;
	}

	public Integer getMaxUncounted() {
		return maxUncounted;
	}

	public void setMaxUncounted(Integer maxreconcile) {
		this.maxUncounted = maxreconcile;
	}

}
