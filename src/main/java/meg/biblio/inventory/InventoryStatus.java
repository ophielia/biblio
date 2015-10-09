package meg.biblio.inventory;

import java.util.Date;

import meg.biblio.inventory.db.dao.InventoryDao;

public class InventoryStatus {

	private InventoryDao inventory;
	private long countedbooks=0;
	private long reconciledbooks=0;
	private long refoundbooks=0;
	
	
	public InventoryStatus(InventoryDao inv) {
		super();
		this.inventory = inv;
	}

	public long getCountedbooks() {
		return countedbooks;
	}

	public void setCountedBooks(Long counted) {
		if (counted!=null) {
			this.countedbooks = counted.longValue();	
		} else {
			this.countedbooks = 0;
		}
	}

	public long getReconciledbooks() {
		return reconciledbooks;
	}

	public void setReconciledBooks(Long reconciled) {
		if (reconciled!=null) {
			this.reconciledbooks = reconciled.longValue();	
		} else {
			this.reconciledbooks = 0;
		}
	}

	public long getRefoundbooks() {
		return refoundbooks;
	}

	public void setRefoundBooks(int refound) {
		Long refoundlong = new Long(refound);
		this.refoundbooks = refoundlong.longValue();
	}

	public Integer getTotaltocount() {
		if (this.inventory!=null) {
			return this.inventory.getTobecounted();
		}
		return null;
	}

	public double getPercentcompleted() {
		if (this.inventory!=null) {
			double totalcnt=new Long(this.countedbooks).doubleValue();
			double tocnt=this.inventory.getTobecounted().doubleValue();
			double percent = Math.round((totalcnt* 100.0 / tocnt ));
			return percent;
		}
		
		return 0.0;
	}

	public Date getStartdate() {
		if (this.inventory!=null) {
			return this.inventory.getStartdate();
		}
		return null;
	}
	
	

}
