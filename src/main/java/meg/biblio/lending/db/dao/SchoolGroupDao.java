package meg.biblio.lending.db.dao;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;

import meg.biblio.common.db.dao.ClientDao;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
public class SchoolGroupDao {
	
	@OneToOne( fetch=FetchType.EAGER)
	private ClientDao client;
	
	
	@OneToOne( fetch=FetchType.EAGER)
	private TeacherDao teacher;
	
	@OneToMany(mappedBy = "schoolgroup",cascade = CascadeType.PERSIST, fetch=FetchType.EAGER)
	@OrderBy("firstname asc,sectionkey asc")
	private List<StudentDao> students;
	
	private Integer schoolyearbegin;
	
	private Integer schoolyearend;
	
	
	public int getClasscount() {
		if (students!=null) {
			return students.size();
		}
		return 0;
	}
}
