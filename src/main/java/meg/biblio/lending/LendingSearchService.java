package meg.biblio.lending;

import java.util.HashMap;
import java.util.List;

import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.LoanRecordDisplay;


public interface LendingSearchService {

	public static final class Breakoutfield {
		public static final long STATUS=1;
		public static final long CLIENTCATEGORY=2;
	}
	
	List<LoanRecordDisplay> findLoanRecordsByCriteria(
			LendingSearchCriteria criteria, Long clientid);

	HashMap<Long, Long> checkoutBreakout(long breakoutkey, Long clientid,
			Boolean currentYearOnly);

	public Long getActiveBorrowerCount(ClientDao client);
	
	public Long findCountByCriteria(LendingSearchCriteria criteria,Long clientid);

	HashMap<String, Long> mostPopularBreakout(Long clientid,
			Boolean currentYearOnly);


}