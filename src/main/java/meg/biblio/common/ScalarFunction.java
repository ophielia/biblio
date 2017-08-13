package meg.biblio.common;

import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Component
public class ScalarFunction<T> {

    @PersistenceContext
    private EntityManager entityManager;

    public T singleResult(String sql) {
        return singleResult(sql, null);
    }

    public List<T> list(String sql) {
        return list(sql, null);
    }

    public T singleResult(String sql, Object... parms) {
        List<T> result = null;
        result = list(sql, parms);
        return result.isEmpty() ? null : result.get(0);
    }

    public List<T> list(String sql, Object... parms) {
        if (parms == null || parms.length == 0) {
            return entityManager.createNativeQuery(sql).getResultList();
        }
        Query query = entityManager.createNativeQuery(sql);
        for (int i = 0; i < parms.length; i++) {
            query.setParameter(i + 1, parms[i]);
        }
        return query.getResultList();
    }
}
