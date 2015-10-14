package meg.biblio.inventory.web.model;

import java.util.List;

import meg.biblio.inventory.InventoryStatus;
import meg.biblio.inventory.db.dao.InvStackDisplay;

public class CountModel {

	private long counttypepref;
	List<InvStackDisplay> stack;
	private String barcodeentry;
	private String manualentry;
	private InventoryStatus inventoryStatus;
	
	public static class CountType {
		public static final long MANUAL=1;
		public static final long BARCODE=2;
	}
	public void setCountTypePref(long pref) {
		this.counttypepref=pref;
	}

	public long getCountTypePref() {
		return counttypepref;
	}

	public void setUserStack(List<InvStackDisplay> stack) {
		this.stack = stack;
		
	}

	public List<InvStackDisplay> getUserStack() {
		return stack;
	}

	public void setBarcodeentry(String entry) {
		this.barcodeentry=entry;
		
	}

	public String getBarcodeentry() {
		return barcodeentry;
	}

	public void setManualentry(String entry) {
		this.manualentry=entry;
		
	}

	public String getManualentry() {
		return manualentry;
	}

	public void setInventoryStatus(InventoryStatus status) {
		this.inventoryStatus = status;
	}

	public InventoryStatus getInventoryStatus() {
		return inventoryStatus;
	}
	
	

}
