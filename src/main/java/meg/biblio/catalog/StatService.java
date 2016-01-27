package meg.biblio.catalog;

import java.util.Locale;

import meg.biblio.catalog.web.model.StatsModel;
import meg.biblio.common.db.dao.ClientDao;


public interface StatService {

	public final static  class StatType {
		public static final Long CATALOGCOUNT = 1L;  
		public static final Long CHECKEDOUTCOUNT = 2L; 
		public static final Long BORROWERCOUNT = 3L; 
		public static final Long MOSTPOPULAR_YR = 4L; 
		public static final Long MOSTPOPULAR_GL = 5L; 
		public static final Long INVENTORY = 6L; 
		public static final Long OVERDUECOUNT = 7L; 
		public static final Long CHECKEDOUTTOTAL = 8L; 
		public static final Long CATEGORYBKOUT = 106L;
		public static final Long STATUSBKOUT = 107L;
		public static final Long COCATEGORYBKOUT_YR = 108L;
		public static final Long COCATEGORYBKOUT_GL = 109L;
		public static final Long POPULARBKOUT_GL = 110L;
		public static final Long POPULARBKOUT_YR = 111L;

		
	}

	public StatsModel fillStatsForClient(ClientDao client,Locale loc);



	StatBreakout runBreakoutStatByType(ClientDao client, Locale loc,
			Long stattype);



	BasicStat runBasicStatByType(ClientDao client, Locale loc, Long stattype);
}
