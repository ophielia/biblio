package meg.biblio.catalog;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.transaction.Transactional;

import meg.biblio.catalog.db.dao.ClassificationDao;
import meg.biblio.catalog.web.model.StatsModel;
import meg.biblio.common.AppSettingService;
import meg.biblio.common.SelectKeyService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.inventory.InventoryService;
import meg.biblio.inventory.InventoryStatus;
import meg.biblio.inventory.db.dao.InventoryDao;
import meg.biblio.lending.LendingSearchCriteria;
import meg.biblio.lending.LendingSearchService;
import meg.biblio.search.SearchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class StatServiceImpl implements StatService {

	@Autowired
	SearchService searchService;

	@Autowired
	AppSettingService setService;

	@Autowired
	SelectKeyService keyService;

	@Autowired
	CatalogService catalogService;

	@Autowired
	LendingSearchService lendingSearchService;

	@Autowired
	InventoryService inventoryService;

	public StatsModel fillStatsForClient(ClientDao client, Locale loc) {
		// get statsconfig
		StatsConfig config = getConfigForClient(client);

		if (config != null) {
			StatsModel model = new StatsModel();

			// run zone 1 stats
			List<Long> torun = config.getZone1Stats();
			List<BasicStat> zone1 = runBasicStats(client, torun, loc);

			// put stats in model
			model.setZone1Stats(zone1);

			// run zone 2 stats
			torun = config.getZone2Stats();
			List<BasicStat> zone2 = runBasicStats(client, torun, loc);
			// put stats in model
			model.setZone2Stats(zone2);

			// run zone 3 stats
			torun = config.getZone3Stats();
			List<StatBreakout> zone3 = runBreakoutStats(client, torun, loc);
			// put stats in model
			model.setZone3Stats(zone3);

			// return statsmodel
			return model;
		}
		// return statsmodel
		return null;
	}

	private List<StatBreakout> runBreakoutStats(ClientDao client,
			List<Long> torun, Locale loc) {

		List<StatBreakout> results = new ArrayList<StatBreakout>();
		// check for null, initialize result list
		if (torun != null) {

			// go through list running stats
			for (Long stattype : torun) {
				StatBreakout stat = runBreakoutStatByType(client, loc, stattype);
				// save stats in result list
				if (stat != null) {
					results.add(stat);
				}
			}

		}
		// return results
		return results;
	}

	@Override
	public StatBreakout runBreakoutStatByType(ClientDao client, Locale loc,
			Long stattype) {
		// get language
		String lang = loc.getLanguage();

		if (stattype.longValue() == StatService.StatType.CATEGORYBKOUT) {
			return runStatCatBkout(client, lang);
		} else if (stattype.longValue() == StatService.StatType.COCATEGORYBKOUT_GL) {
			return runStatCOCatAllBkout(client, lang);
		} else if (stattype.longValue() == StatService.StatType.COCATEGORYBKOUT_YR) {
			return runStatCOCatYearBkout(client, lang);
		} else if (stattype.longValue() == StatService.StatType.STATUSBKOUT) {
			return runStatStatusBkout(client, lang);
		} else if (stattype.longValue() == StatService.StatType.POPULARBKOUT_GL) {
			return runPopularBkout(client, false);
		} else if (stattype.longValue() == StatService.StatType.POPULARBKOUT_YR) {
			return runPopularBkout(client, true);
		}
		return null;
	}

	private List<BasicStat> runBasicStats(ClientDao client, List<Long> torun,
			Locale loc) {
		List<BasicStat> results = new ArrayList<BasicStat>();
		// check for null, initialize result list
		if (torun != null) {

			// go through list running stats
			for (Long stattype : torun) {
				BasicStat stat = runBasicStatByType(client, loc, stattype);
				// save stats in result list
				if (stat != null) {
					results.add(stat);
				}
			}

		}
		// return results
		return results;
	}

	@Override
	public BasicStat runBasicStatByType(ClientDao client, Locale loc,
			Long stattype) {
		// get language
		String lang = loc.getLanguage();

		if (stattype.longValue() == StatService.StatType.CATALOGCOUNT) {
			return runBasicCatCount(client, lang);// SearchService
		} else if (stattype.longValue() == StatService.StatType.CHECKEDOUTCOUNT) {
			return runBasicCheckoutCount(client, lang);// LendingSearchService
		} else if (stattype.longValue() == StatService.StatType.BORROWERCOUNT) {
			return runBasicBorrowerCount(client, lang);// ClassManagementService
		} else if (stattype.longValue() == StatService.StatType.MOSTPOPULAR_YR) {
			return runBasicYearlyPopular(client, lang);// LendingSearchService
		} else if (stattype.longValue() == StatService.StatType.MOSTPOPULAR_GL) {
			return runBasicGlobalPopular(client, lang);// LendingSearchService
		} else if (stattype.longValue() == StatService.StatType.INVENTORY) {
			return runBasicInventoryInfo(client, loc);// InventoryService
		} else if (stattype.longValue() == StatService.StatType.OVERDUECOUNT) {
			return runBasicOverdueCount(client, lang);// InventoryService
		} else if (stattype.longValue() == StatService.StatType.CHECKEDOUTTOTAL) {
			return runBasicCheckoutTotalCount(client, lang);// InventoryService
		}

		return null;
	}

	private StatsConfig getConfigForClient(ClientDao client) {
		// later can retrieve config for client. Currently
		// we're doing one size fits all...;
		return getDefaultConfig();
	}

	private StatsConfig getDefaultConfig() {
		// set zone 1 defaults
		List<Long> zone1 = new ArrayList<Long>();
		zone1.add(StatService.StatType.CATALOGCOUNT);
		zone1.add(StatService.StatType.CHECKEDOUTCOUNT);
		zone1.add(StatService.StatType.OVERDUECOUNT);
		zone1.add(StatService.StatType.CHECKEDOUTTOTAL);

		// set zone 2 defaults
		List<Long> zone2 = new ArrayList<Long>();
		zone2.add(StatService.StatType.MOSTPOPULAR_YR);
		zone2.add(StatService.StatType.INVENTORY);
		zone2.add(StatService.StatType.BORROWERCOUNT);

		// set zone 3 defaults
		List<Long> zone3 = new ArrayList<Long>();
		zone3.add(StatService.StatType.CATEGORYBKOUT);
		zone3.add(StatService.StatType.STATUSBKOUT);
		zone3.add(StatService.StatType.POPULARBKOUT_GL);

		// put together config
		StatsConfig config = new StatsConfig();
		config.setZone1Stats(zone1);
		config.setZone2Stats(zone2);
		config.setZone3Stats(zone3);

		return config;
	}

	private BasicStat runBasicInventoryInfo(ClientDao client, Locale loc) {
		// initialize StatBreakout - labels, messages, and so on
		BasicStat stat = new BasicStat();
		stat.setStattype(StatService.StatType.INVENTORY);

		// run stat
		InventoryDao current = inventoryService.getCurrentInventory(client);
		if (current != null) {
			stat.setLabel("msg_stats_inventorycurrent");
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, loc);
			String startdate = df.format(current.getStartdate());
			stat.setValue(startdate);
		} else {
			// no current inventory
			InventoryStatus lastcompleted = inventoryService
					.getLastCompleted(client);
			if (lastcompleted != null) {
				stat.setLabel("msg_stats_inventorylast");
				DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM,
						loc);
				String enddate = df.format(lastcompleted.getEnddate());
				stat.setValue(enddate);
			} else {
				stat.setLabel("msg_stats_inventorylast");
				stat.setValue("msg_stats_invnotrun");
			}
		}

		return stat;
	}

	private BasicStat runBasicGlobalPopular(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		BasicStat stat = new BasicStat();
		stat.setStattype(StatService.StatType.MOSTPOPULAR_GL);
		stat.setLabel("msg_stats_popularglobal");

		// run stat
		HashMap<String, Long> statusbkout = lendingSearchService
				.mostPopularBreakout(client.getId(), false);
		// fill in display values
		if (statusbkout != null) {

			Set<String> keys = statusbkout.keySet();
			String title = null;
			for (String key : keys) {
				title = key;
				break;
			}
			stat.setValue(title);
		} else {
			stat.setValue("");
		}
		return stat;
	}

	private BasicStat runBasicYearlyPopular(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		BasicStat stat = new BasicStat();
		stat.setStattype(StatService.StatType.MOSTPOPULAR_YR);
		stat.setLabel("msg_stats_popularyearly");
		stat.setAddlLabel("cy");

		// run stat
		HashMap<String, Long> statusbkout = lendingSearchService
				.mostPopularBreakout(client.getId(), true);
		// fill in display values
		if (statusbkout != null) {

			Set<String> keys = statusbkout.keySet();
			String title = null;
			for (String key : keys) {
				title = key;
				break;
			}
			stat.setValue(title);
		} else {
			stat.setValue("");
		}
		return stat;
	}

	private BasicStat runBasicBorrowerCount(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		BasicStat stat = new BasicStat();
		stat.setStattype(StatService.StatType.BORROWERCOUNT);
		stat.setLabel("msg_stats_borrowercount");

		// run stat
		Long borrowercount = lendingSearchService
				.getActiveBorrowerCount(client);
		String value = String.valueOf(borrowercount);
		stat.setValue(value);

		return stat;
	}

	private BasicStat runBasicCheckoutCount(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		BasicStat stat = new BasicStat();
		stat.setStattype(StatService.StatType.CHECKEDOUTCOUNT);
		stat.setLabel("msg_stats_checkoutcount");

		// run stat
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setCheckedoutOnly(true);
		Long checkoutcount = lendingSearchService.findCountByCriteria(criteria,
				client.getId());
		String value = String.valueOf(checkoutcount);
		stat.setValue(value);

		return stat;
	}

	private BasicStat runBasicOverdueCount(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		BasicStat stat = new BasicStat();
		stat.setStattype(StatService.StatType.OVERDUECOUNT);
		stat.setLabel("msg_stats_overduecount");

		// run stat
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setOverdueOnly(true);

		Long checkoutcount = lendingSearchService.findCountByCriteria(criteria,
				client.getId());
		String value = String.valueOf(checkoutcount);
		stat.setValue(value);

		return stat;
	}

	private BasicStat runBasicCheckoutTotalCount(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		BasicStat stat = new BasicStat();
		stat.setStattype(StatService.StatType.CHECKEDOUTTOTAL);
		stat.setLabel("msg_stats_checkedouttotal");
		stat.setAddlLabel("cy");

		// run stat
		LendingSearchCriteria criteria = new LendingSearchCriteria();
		criteria.setTimeselect(LendingSearchCriteria.TimePeriodType.CURRENTSCHOOLYEAR);

		Long checkoutcount = lendingSearchService.findCountByCriteria(criteria,
				client.getId());
		String value = String.valueOf(checkoutcount);
		stat.setValue(value);

		return stat;
	}

	private BasicStat runBasicCatCount(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		BasicStat stat = new BasicStat();
		stat.setStattype(StatService.StatType.CATALOGCOUNT);
		stat.setLabel("msg_stats_catcount");

		// run stat
		Long bookcount = searchService.getBookCount(client.getId());
		String value = String.valueOf(bookcount);
		stat.setValue(value);

		return stat;
	}

	private StatBreakout runStatStatusBkout(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		StatBreakout stat = new StatBreakout(StatService.StatType.STATUSBKOUT);
		stat.setLabel("msg_stats_statusbkout");

		// run stat
		HashMap<Long, Long> statusbkout = searchService.breakoutByBookField(
				SearchService.Breakoutfield.STATUS, client.getId());
		// fill in display values
		if (statusbkout != null) {
			HashMap<Long, String> disps = keyService.getDisplayHashForKey(
					CatalogService.bookstatuslkup, lang);
			Set<Long> keys = statusbkout.keySet();
			for (Long key : keys) {
				// make BasicStat to hold label and value
				BasicStat bs = new BasicStat();
				String display = disps.get(key);
				String value = String.valueOf(statusbkout.get(key));
				bs.setDisplay(display);
				bs.setValue(value);
				stat.addBkoutValue(bs);
			}
		}

		return stat;
	}

	private StatBreakout runStatCOCatAllBkout(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		StatBreakout stat = new StatBreakout(
				StatService.StatType.COCATEGORYBKOUT_GL);
		stat.setLabel("msg_stats_cocatglbkout");

		// run stat
		HashMap<Long, Long> statusbkout = lendingSearchService
				.checkoutBreakout(
						LendingSearchService.Breakoutfield.CLIENTCATEGORY,
						client.getId(), false);
		// fill in display values
		if (statusbkout != null) {
			HashMap<Long, ClassificationDao> disps = catalogService
					.getShelfClassHash(client.getId(), lang);
			String base = setService.getSettingAsString("biblio.imagebase");
			Set<Long> keys = statusbkout.keySet();
			for (Long key : keys) {
				// make BasicStat to hold label and value
				BasicStat bs = new BasicStat();
				ClassificationDao classdao = disps.get(key);
				if (classdao != null) {
					String display = classdao.getDescription();
					String addldisp = classdao.getImagedisplay();
					bs.setDisplay(display);
					bs.setImagepath(base + addldisp);
				}
				String value = String.valueOf(statusbkout.get(key));
				bs.setValue(value);

				stat.addBkoutValue(bs);
			}
		}

		return stat;
	}

	private StatBreakout runStatCOCatYearBkout(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		StatBreakout stat = new StatBreakout(
				StatService.StatType.COCATEGORYBKOUT_YR);
		stat.setLabel("msg_stats_cocatyrbkout");
		stat.setAddlLabel("cy");

		// run stat
		HashMap<Long, Long> statusbkout = lendingSearchService
				.checkoutBreakout(
						LendingSearchService.Breakoutfield.CLIENTCATEGORY,
						client.getId(), true);
		// fill in display values
		if (statusbkout != null) {
			HashMap<Long, ClassificationDao> disps = catalogService
					.getShelfClassHash(client.getId(), lang);
			String base = setService.getSettingAsString("biblio.imagebase");
			Set<Long> keys = statusbkout.keySet();
			for (Long key : keys) {
				// make BasicStat to hold label and value
				BasicStat bs = new BasicStat();
				ClassificationDao classdao = disps.get(key);
				if (classdao != null) {
					String display = classdao.getDescription();
					String addldisp = classdao.getImagedisplay();
					bs.setDisplay(display);
					bs.setImagepath(base + addldisp);
				}
				String value = String.valueOf(statusbkout.get(key));
				bs.setValue(value);

				stat.addBkoutValue(bs);
			}
		}

		return stat;
	}

	private StatBreakout runPopularBkout(ClientDao client,
			boolean currentYearOnly) {
		// initialize StatBreakout - labels, messages, and so on
		Long stattype = StatService.StatType.POPULARBKOUT_GL;
		String stattitle = "msg_stats_popularglobal";
		String addllabel = null;

		if (currentYearOnly) {
			stattype = StatService.StatType.POPULARBKOUT_YR;
			stattitle = "msg_stats_popularyearly";
			addllabel = "cy";
		}

		StatBreakout stat = new StatBreakout(stattype);
		stat.setLabel(stattitle);
		if (addllabel != null) {
			stat.setAddlLabel("cy");
		}

		// run stat
		HashMap<String, Long> statusbkout = lendingSearchService
				.mostPopularBreakout(client.getId(), currentYearOnly);
		// fill in display values
		if (statusbkout != null) {

			Set<String> keys = statusbkout.keySet();
			for (String title : keys) {
				// make BasicStat to hold label and value
				BasicStat bs = new BasicStat();

				bs.setDisplay(title);
				String value = String.valueOf(statusbkout.get(title));
				bs.setValue(value);

				stat.addBkoutValue(bs);
			}
		}
		return stat;
	}

	private StatBreakout runStatCatBkout(ClientDao client, String lang) {
		// initialize StatBreakout - labels, messages, and so on
		StatBreakout stat = new StatBreakout(StatService.StatType.CATEGORYBKOUT);
		stat.setLabel("msg_stats_catbkout");

		// run stat
		HashMap<Long, Long> statusbkout = searchService.breakoutByBookField(
				SearchService.Breakoutfield.CLIENTCATEGORY, client.getId());
		// fill in display values
		if (statusbkout != null) {
			String base = setService.getSettingAsString("biblio.imagebase");
			HashMap<Long, ClassificationDao> disps = catalogService
					.getShelfClassHash(client.getId(), lang);
			Set<Long> keys = statusbkout.keySet();
			for (Long key : keys) {

				// make BasicStat to hold label and value
				BasicStat bs = new BasicStat();
				ClassificationDao classdao = disps.get(key);
				if (classdao != null) {
					String display = classdao.getDescription();
					String addldisp = classdao.getImagedisplay();
					bs.setDisplay(display);
					bs.setImagepath(base + addldisp);
				} else {
					bs.setDisplay(" - - - ");
				}
				String value = String.valueOf(statusbkout.get(key));
				bs.setValue(value);

				stat.addBkoutValue(bs);
			}
		}

		return stat;
	}

}
