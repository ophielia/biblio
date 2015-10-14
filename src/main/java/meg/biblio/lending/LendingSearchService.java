package meg.biblio.lending;

import java.util.List;

import meg.biblio.lending.db.dao.LoanRecordDisplay;


public interface LendingSearchService {

	List<LoanRecordDisplay> findLoanRecordsByCriteria(
			LendingSearchCriteria criteria, Long clientid);


}