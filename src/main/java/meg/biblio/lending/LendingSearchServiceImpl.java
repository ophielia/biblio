package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.PersonDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.web.model.LoanRecordDisplay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LendingSearchServiceImpl implements LendingSearchService {

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private CatalogService catalogService;

	@Override
	public List<LoanRecordDisplay> findLoanRecordsByCriteria(
			LendingSearchCriteria criteria, Long clientid) {
		// set clientid in criteria
		criteria.setClientid(clientid);

		// put together joins
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> c = cb.createTupleQuery();
		// CriteriaQuery<LoanRecordDisplay> c =
		// cb.createQuery(LoanRecordDisplay.class);
		Root<LoanRecordDao> loanrec = c.from(LoanRecordDao.class);
		Join<LoanRecordDao, BookDao> book = loanrec.join("book");
		Join<LoanRecordDao, PersonDao> person = loanrec.join("borrower");
		Join<PersonDao, SchoolGroupDao> sgroup = person.join("schoolgroup");

		// build select clause
		c.select(cb.tuple(book, person, loanrec, sgroup.<Long> get("id")));

		// add predicate
		List<Predicate> whereclause = getPredicatesForCriteria(criteria, cb,
				book, person, sgroup, loanrec);

		// putting where clause together
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		// creating query
		TypedQuery<Tuple> q = em.createQuery(c);

		// adding parameters
		setParametersInQuery(criteria, q, clientid);

		// running query
		List<Tuple> results = q.getResultList();
		List<LoanRecordDisplay> toreturn = new ArrayList<LoanRecordDisplay>();

		for (Tuple t : results) {
			// LoanRecordDisplay result = (LoanRecordDisplay) t.get(0);
			BookDao bookres = (BookDao) t.get(0);
			PersonDao personres = (PersonDao) t.get(1);
			LoanRecordDao lrec = (LoanRecordDao) t.get(2);
			Long classid = (Long) t.get(3);
			LoanRecordDisplay display = new LoanRecordDisplay(lrec, personres,
					bookres, classid);
			toreturn.add(display);
		}

		return toreturn;

	}



	private List<Predicate> getPredicatesForCriteria(
			LendingSearchCriteria criteria, CriteriaBuilder cb, Join book,
			Join person, Join sgroup, Root loanrec) {
		// put together where clause
		List<Predicate> whereclause = new ArrayList<Predicate>();

		// making space for parameters
		// always add client id
		ParameterExpression<Long> clientparam = cb.parameter(Long.class,
				"clientid");
		whereclause.add(cb.equal(person.<Long> get("client").get("id"), clientparam));

		// do checkedouton
		if (criteria.getCheckedouton() != null) {
				ParameterExpression<Date> param = cb.parameter(Date.class,
						"checkoutdate");
				whereclause.add(cb.equal(
						loanrec.<Date> get("checkoutdate"), param));
			
		}

		// do returned on
		if (criteria.getReturnedon() != null) {
				ParameterExpression<Date> param = cb.parameter(Date.class,
						"returned");
				whereclause
						.add(cb.equal(loanrec.<Date> get("returned"), param));
		}

		// do forschoolgroup
		if (criteria.getSchoolgroup() != null) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"schoolgroupid");
			whereclause.add(cb.equal(
					person.<Long> get("schoolgroup").get("id"), param));
		}

		// do borrowerid
		if (criteria.getBorrowerid() != null) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"borrowerid");
			whereclause.add(cb.equal(
					person.<Long>get("id"), param));
		}

		// do bookid
		if (criteria.getBookid() != null) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"bookid");
			whereclause.add(cb.equal(
					book.<Long>get("id"), param));
		}

		// to lentto
		if (criteria.getLentToType() != null) {
			String comparison = "";
			if (criteria.getLentToType() == LendingSearchCriteria.LentToType.TEACHER) {
				comparison = "TeacherDao";
			} else if (criteria.getLentToType() == LendingSearchCriteria.LentToType.STUDENT) {
				comparison = "StudentDao";
			}
			if (comparison.length() > 0) {
				whereclause.add(cb.equal(person.<String> get("psn_type"),
						comparison));

			}
		}

		// to overdue only
		if (criteria.getOverdueOnly() != null && criteria.getOverdueOnly()) {
				Expression<java.util.Date> today = cb
						.literal(new java.util.Date());
				whereclause.add(cb.lessThan(loanrec.<Date> get("duedate"),
						today));

			
		}
		
		// to checkedout only
		if (criteria.getCheckedoutOnly() != null && criteria.getCheckedoutOnly()) {
				whereclause.add(cb.isNull(loanrec.<Date> get("returned")));
		}		
		return whereclause;
	}

	private void setParametersInQuery(LendingSearchCriteria criteria,
			TypedQuery q, Long clientid) {
		// always add clientid
		q.setParameter("clientid", clientid);

		// do checkedouton
		if (criteria.getCheckedouton() != null) {
			q.setParameter("checkoutdate", criteria.getCheckedouton());
		}

		// do returned on
		if (criteria.getReturnedon() != null) {
				q.setParameter("returned", criteria.getReturnedon());
		}

		// do forschoolgroup
		if (criteria.getSchoolgroup() != null) {
			q.setParameter("schoolgroupid", criteria.getSchoolgroup());
		}

		// do forschoolgroup
		if (criteria.getBorrowerid() != null) {
			q.setParameter("borrowerid", criteria.getBorrowerid());
		}

		// do bookid
		if (criteria.getBookid() != null) {
			q.setParameter("bookid", criteria.getBookid());
		}

		// to lentto - no parameter

		// to overdue only - no parameter

	}

	
	/*
	 * 
	@Override
	public List<LoanHistoryDisplay> findLoanHistoryByCriteria(
			LendingSearchCriteria criteria, Long clientid) {
		// set clientid in criteria
		criteria.setClientid(clientid);

		// put together joins
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> c = cb.createTupleQuery();
		Root<LoanHistoryDao> loanrec = c.from(LoanHistoryDao.class);
		Join<LoanHistoryDao, BookDao> book = loanrec.join("book");
		Join<LoanHistoryDao, PersonDao> person = loanrec.join("borrower");
		Join<PersonDao, SchoolGroupDao> sgroup = person.join("schoolgroup");

		// build select clause
		c.select(cb.tuple(book, person, loanrec, sgroup.<Long> get("id")));

		// add predicate
		List<Predicate> whereclause = getPredicatesForCriteria(criteria, cb,
				book, person, sgroup, null, loanrec,
				LendingSearchCriteria.SearchType.RETURNED);

		// putting where clause together
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		// creating query
		TypedQuery<Tuple> q = em.createQuery(c);

		// adding parameters
		setParametersInQuery(criteria, q, clientid,LendingSearchCriteria.SearchType.RETURNED);

		// running query
		List<Tuple> results = q.getResultList();
		List<LoanHistoryDisplay> toreturn = new ArrayList<LoanHistoryDisplay>();

		for (Tuple t : results) {
			// LoanRecordDisplay result = (LoanRecordDisplay) t.get(0);
			BookDao bookres = (BookDao) t.get(0);
			PersonDao personres = (PersonDao) t.get(1);
			LoanHistoryDao lrec = (LoanHistoryDao) t.get(2);
			Long classid = (Long) t.get(3);
			LoanHistoryDisplay display = new LoanHistoryDisplay(lrec,
					personres, bookres, classid);
			toreturn.add(display);
		}

		return toreturn;

	}
	 */
}
