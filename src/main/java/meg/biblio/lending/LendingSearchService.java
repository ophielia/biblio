package meg.biblio.lending;

import java.util.List;

import meg.biblio.lending.web.model.LoanRecordDisplay;


public interface LendingSearchService {

	List<LoanRecordDisplay> findLoanRecordsByCriteria(
			LendingSearchCriteria criteria, Long clientid);


}