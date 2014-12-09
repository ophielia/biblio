package meg.biblio.lending;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.ListJoin;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import meg.biblio.catalog.CatalogService;
import meg.biblio.catalog.db.FoundWordsDao;
import meg.biblio.catalog.db.dao.ArtistDao;
import meg.biblio.catalog.db.dao.BookDao;
import meg.biblio.catalog.db.dao.PublisherDao;
import meg.biblio.search.BookSearchCriteria;
import meg.biblio.lending.db.dao.LoanHistoryDao;
import meg.biblio.lending.db.dao.LoanRecordDao;
import meg.biblio.lending.db.dao.PersonDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.LoanHistoryDisplay;
import meg.biblio.lending.web.model.LoanRecordDisplay;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
		List<Predicate> whereclause = new ArrayList<Predicate>();

		// putting where clause together
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		// creating query
		TypedQuery<Tuple> q = em.createQuery(c);

		// adding parameters

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
	
	@Override
	public List<LoanHistoryDisplay> findLoanHistoryByCriteria(
			LendingSearchCriteria criteria, Long clientid) {
		// set clientid in criteria
		criteria.setClientid(clientid);

		// put together joins
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Tuple> c = cb.createTupleQuery();
		// CriteriaQuery<LoanRecordDisplay> c =
		// cb.createQuery(LoanRecordDisplay.class);
		Root<LoanHistoryDao> loanrec = c.from(LoanHistoryDao.class);
		Join<LoanHistoryDao, BookDao> book = loanrec.join("book");
		Join<LoanHistoryDao, PersonDao> person = loanrec.join("borrower");
		Join<PersonDao, SchoolGroupDao> sgroup = person.join("schoolgroup");

		// build select clause
		c.select(cb.tuple(book, person, loanrec, sgroup.<Long> get("id")));

		// add predicate
		List<Predicate> whereclause = new ArrayList<Predicate>();

		// putting where clause together
		c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));

		// creating query
		TypedQuery<Tuple> q = em.createQuery(c);

		// adding parameters

		// running query
		List<Tuple> results = q.getResultList();
		List<LoanHistoryDisplay> toreturn = new ArrayList<LoanHistoryDisplay>();

		for (Tuple t : results) {
			// LoanRecordDisplay result = (LoanRecordDisplay) t.get(0);
			BookDao bookres = (BookDao) t.get(0);
			PersonDao personres = (PersonDao) t.get(1);
			LoanHistoryDao lrec = (LoanHistoryDao) t.get(2);
			Long classid = (Long) t.get(3);
			LoanHistoryDisplay display = new LoanHistoryDisplay(lrec, personres,
					bookres, classid);
			toreturn.add(display);
		}

		return toreturn;

	}
	
	
	
	private List<BookDao> model(BookSearchCriteria criteria, Long clientid) {
		
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<BookDao> c = cb.createQuery(BookDao.class);
		Root<BookDao> bookroot = c.from(BookDao.class);
		c.select(bookroot);

	
		if (criteria != null) {
			
			
			// get where clause
			Expression sortexpr = null;
			List<Predicate> whereclause = getPredicatesForCriteria(criteria,cb,bookroot,sortexpr);
			
			// adding where clause
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			
			// adding orderby
			long orderdir=criteria.getOrderbydir();
			List<Expression> exprlist = new ArrayList<Expression>();
			if (sortexpr==null) {
				if (criteria.getOrderby()==BookSearchCriteria.OrderBy.AUTHOR) {
					Join<BookDao, ArtistDao> authorjoin = bookroot.join("authors");
					sortexpr=authorjoin.get("lastname");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.DATEADDED) {
					sortexpr=bookroot.get("createdon");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.TITLE) {
					sortexpr=bookroot.get("title");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.SHELFCLASS) {
					sortexpr=bookroot.get("shelfclass");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.BOOKID) {
					exprlist.add(bookroot.get("clientbookidsort"));
					sortexpr=bookroot.get("clientbookid");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.BOOKTYPE) {
					sortexpr=bookroot.get("type");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.STATUS) {
					sortexpr=bookroot.get("status");
				} else if (criteria.getOrderby()==BookSearchCriteria.OrderBy.DETAILSTATUS) {
					sortexpr=bookroot.get("detailstatus");
				} else  {
					// default
					sortexpr=bookroot.get("title");
				}  
			} 
			exprlist.add(sortexpr);
			List<Order> orderlist = new ArrayList<Order>();
			if (orderdir==BookSearchCriteria.OrderByDir.ASC) {
				for (Expression order:exprlist) {
					orderlist.add(cb.asc(order));
				}
			} else {
				for (Expression order:exprlist) {
					orderlist.add(cb.desc(order));
				}
			}
			c.orderBy(orderlist);
			// creating the query
			TypedQuery<BookDao> q = em.createQuery(c);

			// setting the parameters
			setParametersInQuery(criteria,q, clientid);
			
			return q.getResultList();

		}

		return null;
	}	



	
	private void setParametersInQuery(BookSearchCriteria criteria, TypedQuery q, Long clientid) {
		// always add clientid
		q.setParameter("clientid", clientid);

		// title
		if (criteria.hasTitle()) {
			q.setParameter("title", "%"
					+ criteria.getTitle().toLowerCase().trim() + "%");
		}
		// shelf class
		if (criteria.hasShelfclasskey()) {
			q.setParameter("shelfclass", criteria.getShelfclasskey());
		}
		// status
		if (criteria.hasStatus()) {
			q.setParameter("status", criteria.getStatus());
		}	
		// status
		if (criteria.hasBooktype()) {
			q.setParameter("type", criteria.getBooktype());
		}			
		// detail status
		if (criteria.hasDetailstatus()) {
			q.setParameter("detailstatus", criteria.getDetailstatus());
		}	
		// author
		if (criteria.hasAuthor()) {
			ArtistDao tomatch = catalogService.textToArtistName(criteria
					.getAuthor());
			// where firstname = firstname and middlename = middlename and
			// lastname = lastname
			// together with likes and to lower
			// lastname
			if (tomatch.hasLastname()) {
				q.setParameter("alastname", "%"
						+ tomatch.getLastname().toLowerCase().trim() + "%");
			}
			// middlename
			if (tomatch.hasMiddlename()) {
				q.setParameter("amiddlename", "%"
						+ tomatch.getMiddlename().toLowerCase().trim() + "%");				
			}
			// firstname
			if (tomatch.hasFirstname()) {
				q.setParameter("afirstname", "%"
						+ tomatch.getFirstname().toLowerCase().trim() + "%");				

			}
		}	
		// publisher
		if (criteria.hasPublisher()) {
			q.setParameter("publisher",  "%"
						+ criteria.getPublisher().toLowerCase().trim() + "%");	
		}

	}

	private List<Predicate> getPredicatesForCriteria(
			BookSearchCriteria criteria, CriteriaBuilder cb, Root<BookDao> bookroot, Expression sortexpr) {
		// put together where clause
		List<Predicate> whereclause = new ArrayList<Predicate>();

		// making space for parameters
		// always add client id
		ParameterExpression<Long> clientparam = cb.parameter(Long.class, "clientid");
		whereclause.add(cb.equal(bookroot.<Long> get("clientid"), clientparam));

		// title
		if (criteria.hasTitle()) {
			ParameterExpression<String> param = cb.parameter(String.class,
					"title");
			whereclause
					.add(cb.like(cb.lower(bookroot.<String> get("title")), param));

		}
		// shelfclass
		if (criteria.hasShelfclasskey()) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"shelfclass");
			whereclause.add(cb.equal(bookroot.<Long> get("shelfclass"), param));

		}
		
		// status
		if (criteria.hasStatus()) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"status");
			whereclause.add(cb.equal(bookroot.<Long> get("status"), param));

		}		
		
		// type
		if (criteria.hasBooktype()) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"type");
			whereclause.add(cb.equal(bookroot.<Long> get("type"), param));

		}		
		
		// detail status
		if (criteria.hasDetailstatus()) {
			ParameterExpression<Long> param = cb.parameter(Long.class,
					"detailstatus");
			whereclause.add(cb.equal(bookroot.<Long> get("detailstatus"), param));

		}			
		
		// author
		if (criteria.hasAuthor()) {
			Join<BookDao, ArtistDao> authorjoin = bookroot.join("authors");
			ArtistDao tomatch = catalogService.textToArtistName(criteria
					.getAuthor());
			// where firstname = firstname and middlename = middlename and
			// lastname = lastname
			// together with likes and to lower
			// lastname
			if (tomatch.hasLastname()) {
				ParameterExpression<String> param = cb.parameter(String.class,
						"alastname");
				Expression<String> path = authorjoin.get("lastname");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.like(lower, param);
				whereclause.add(predicate);
			}
			// middlename
			if (tomatch.hasMiddlename()) {
				ParameterExpression<String> param = cb.parameter(String.class,
						"amiddlename");
				Expression<String> path = authorjoin.get("middlename");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.like(lower, param);
				whereclause.add(predicate);
			}
			// firstname
			if (tomatch.hasFirstname()) {
				ParameterExpression<String> param = cb.parameter(String.class,
						"afirstname");
				Expression<String> path = authorjoin.get("firstname");
				Expression<String> lower = cb.lower(path);
				Predicate predicate = cb.like(lower, param);
				whereclause.add(predicate);
			}
			
			// check sort
			if (criteria.getOrderby()==BookSearchCriteria.OrderBy.AUTHOR) {
				sortexpr = authorjoin.get("lastname");
			}
		}
		// publisher
		if (criteria.hasPublisher()) {
			Join<BookDao, PublisherDao> publishjoin = bookroot
					.join("publisher");

			ParameterExpression<String> param = cb.parameter(String.class,
					"publisher");
			Expression<String> path = publishjoin.get("name");
			Expression<String> lower = cb.lower(path);
			Predicate predicate = cb.like(lower, param);
			whereclause.add(predicate);
		}

		return whereclause;
	}


	
	
	
	
	
}

