package meg.biblio.catalog;

import java.util.ArrayList;
import java.util.List;

public class StatBreakout extends BasicStat {

	private List<BasicStat> bkoutValues;

	public StatBreakout(Long stattype) {
		super();
		setStattype(stattype);
	}

	public List<BasicStat> getBkoutValues() {
		return bkoutValues;
	}

	public void setBkoutValues(List<BasicStat> bkoutValues) {
		this.bkoutValues = bkoutValues;
	}
	
	public void addBkoutValue(BasicStat value) {
		if (this.bkoutValues==null) {
			this.bkoutValues=new ArrayList<BasicStat>();
		}
		this.bkoutValues.add(value);
	}
	
}
