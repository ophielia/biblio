package meg.biblio.catalog;

import java.util.List;

public class StatsConfig {

	private List<Long>  zone1Stats;
	private List<Long>  zone2Stats;
	private List<Long>  zone3Stats;
	public List<Long> getZone1Stats() {
		return zone1Stats;
	}
	public void setZone1Stats(List<Long> zone1Stats) {
		this.zone1Stats = zone1Stats;
	}
	public List<Long> getZone2Stats() {
		return zone2Stats;
	}
	public void setZone2Stats(List<Long> zone2Stats) {
		this.zone2Stats = zone2Stats;
	}
	public List<Long> getZone3Stats() {
		return zone3Stats;
	}
	public void setZone3Stats(List<Long> zone3Stats) {
		this.zone3Stats = zone3Stats;
	}
	
	
}
