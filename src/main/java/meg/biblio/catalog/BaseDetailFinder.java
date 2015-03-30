package meg.biblio.catalog;

import meg.biblio.catalog.db.SubjectRepository;
import meg.biblio.catalog.db.dao.BookDetailDao;
import meg.biblio.catalog.db.dao.FoundDetailsDao;
import meg.biblio.catalog.web.model.BookModel;
import meg.biblio.search.SearchService;

import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseDetailFinder implements DetailFinder {



	DetailFinder next;

	FinderObject searchLogic(FinderObject findobj) throws Exception {
		throw new Exception("Implementation Error");
	}

	protected Long getIdentifier() throws Exception {
		throw new Exception("Implementation Error");
	}

	protected boolean isEnabled() throws Exception {
		throw new Exception("Implementation Error");
	}
	
	protected FinderObject assignDetail(FinderObject findobj, FoundDetailsDao fd)  throws Exception {
		throw new Exception("Implementation Error");
	}
	

	@Override
	public FinderObject findDetails(FinderObject findobj, long clientcomplete)
			throws Exception {
		
		// check eligibility
		boolean iseligible = isEligible(findobj);
		boolean isenabled = isEnabled();

		// if eligible, run searchlogic
		if (iseligible && isenabled) {

			
			// log search
			findobj.logFinderRun(getIdentifier());

			// run logic
			findobj = searchLogic(findobj);
			// check completion
			boolean complete = resultsComplete(findobj, clientcomplete);
			if (!complete) {
				if (getNext() != null) {
					findobj = getNext().findDetails(findobj, clientcomplete);
				}
			}
			

		} else {
			// otherwise, run next search
			if (getNext() != null) {
				findobj = getNext().findDetails(findobj, clientcomplete);
			}
		}

		// return finderobject
		return findobj;
	}

	protected DetailFinder getNext() {
		return next;
	}

	protected void setNext(DetailFinder next) {
		this.next = next;
	}

	protected boolean isEligible(FinderObject findobj) throws Exception {
		// Eligible to be run if 1) firsttime search, 2) hasn't used this finder
		// before, or 3) isbn has been added, even if this
		// search was previously made
		if (findobj.isNew())
			return true;

		boolean searchrun = findobj.getCurrentFinderLog() % getIdentifier() == 0;

		return !searchrun;
	}
	
	protected boolean assignEligible(FoundDetailsDao fdetails) throws Exception {
		// do the details belong to this finder?
		Long detailssource = fdetails.getSearchsource();
		return detailssource!=null && detailssource.longValue()==getIdentifier().longValue();
	}	

	protected boolean resultsComplete(FinderObject findobj, long clientcomplete) {
		boolean complete = true;
		BookDetailDao detail = findobj.getBookdetail();

		if (complete
				&& clientcomplete
						% DetailSearchService.CompletionTargets.AUTHOR == 0) {
			// check author
			String authorstring = detail.getAuthorsAsString();
			complete = ((authorstring != null && authorstring.trim().length() > 0));
		}
		if (complete
				&& clientcomplete % DetailSearchService.CompletionTargets.TITLE == 0) {
			// check title
			String titlestring = detail.getTitle();
			complete = ((titlestring != null && titlestring.trim().length() > 0));
		}
		if (complete
				&& clientcomplete % DetailSearchService.CompletionTargets.IMAGE == 0) {
			// check image
			String imagestring = detail.getImagelink();
			complete = ((imagestring != null && imagestring.trim().length() > 0));
		}
		if (complete
				&& clientcomplete
						% DetailSearchService.CompletionTargets.DESCRIPTION == 0) {
			// check description
			String descriptionstring = detail.getDescription();
			complete = ((descriptionstring != null && descriptionstring.trim()
					.length() > 0));
		}

		return complete;
	}

	public FinderObject assignDetailToBook(FinderObject findobj, FoundDetailsDao fd) throws Exception {
		// check against identifier in FoundDetails
		if (assignEligible(fd)) {
			// log search
			findobj.logFinderRun(getIdentifier());
			// if identifier matches, run assigndetail
			return this.assignDetail(findobj, fd);
		} else {
			// otherwise, run next search
			if (getNext() != null) {
				findobj = getNext().assignDetailToBook(findobj, fd);
			}	
		}
		
		
		return findobj;
	}

}
