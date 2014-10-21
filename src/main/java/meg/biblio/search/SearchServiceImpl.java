package meg.biblio.search;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import meg.biblio.catalog.db.dao.ArtistDao;

import org.springframework.stereotype.Service;

@Service
public class SearchServiceImpl implements SearchService {


    @PersistenceContext
    private EntityManager entityManager;

    

	public ArtistDao findArtistMatchingName(ArtistDao tomatch) {

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<ArtistDao> c = cb.createQuery(ArtistDao.class);
		Root<ArtistDao> exp = c.from(ArtistDao.class);
		c.select(exp);


		if (tomatch != null) {

			
			// get where clause
			List<Predicate> whereclause = new ArrayList<Predicate>();
			// lastname
			if (tomatch.hasLastname()) {
				Expression<String> path = exp.get("lastname");
				Expression<String> lower =cb.lower(path);
				Predicate predicate = cb.equal(lower,tomatch.getLastname().toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("lastname")));
			}
			// middlename
			if (tomatch.hasMiddlename()) {
				Expression<String> path = exp.get("middlename");
				Expression<String> lower =cb.lower(path);
				Predicate predicate = cb.equal(lower,tomatch.getMiddlename().toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("middlename")));
			}
			// firstname
			if (tomatch.hasFirstname()) {
				Expression<String> path = exp.get("firstname");
				Expression<String> lower =cb.lower(path);
				Predicate predicate = cb.equal(lower,tomatch.getFirstname().toLowerCase().trim());
				whereclause.add(predicate);
			} else {
				whereclause.add(cb.isNull(exp.get("firstname")));
			}
			
			// creating the query
			c.where(cb.and(whereclause.toArray(new Predicate[whereclause.size()])));
			TypedQuery<ArtistDao> q = entityManager.createQuery(c);

			
			List<ArtistDao> results = q.getResultList();
			if (results!=null && results.size()>0) {
				return results.get(0);
			} else {
				return null;
			}

		}

		return null;
	}




}

