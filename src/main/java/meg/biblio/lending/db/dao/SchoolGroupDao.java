package meg.biblio.lending.db.dao;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Transient;

import meg.biblio.common.db.dao.ClientDao;

import org.hibernate.annotations.Where;
import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity
public class SchoolGroupDao {
	
	@OneToOne( fetch=FetchType.EAGER)
	private ClientDao client;
	
    @JoinColumn(name="schoolgroup", insertable=false, updatable=false)
    @Where(clause="psn_type='TeacherDao'")
    @ElementCollection(targetClass=TeacherDao.class)
	private List<TeacherDao>  teacherlist;

    @JoinColumn(name="schoolgroup", insertable=false, updatable=false)
    @Where(clause="psn_type='StudentDao'")
    @ElementCollection(targetClass=StudentDao.class)
    @OrderBy("firstname asc,sectionkey asc")
	private List<StudentDao> students;
	
	private Integer schoolyearbegin;
	
	private Integer schoolyearend;
	
	@Transient
	private TeacherDao teacher;
	
	public int getClasscount() {
		if (students!=null) {
			return students.size();
		}
		return 0;
	}


	public String getSchoolyeardisplay() {
		String display = schoolyearbegin + " / " + schoolyearend;
		return display;
	}

	public TeacherDao getTeacher() {
        if (this.teacherlist!=null && this.teacherlist.size()>0) {
        	TeacherDao teacher = teacherlist.get(0);
        	return teacher;
        }
        return null;
	}


	public void setTeacher(TeacherDao teacher) {
		this.teacher = teacher;
		if (this.teacher == null) {
			setTeacherlist(null);
		} else {
			// need to set this in the list of teachers
			// will make it overwrite any existing....
			List<TeacherDao> newlist = new ArrayList<TeacherDao>();
			newlist.add(teacher);
			setTeacherlist(newlist);
		}
	}
	
	

	public void setTeacherlist(List<TeacherDao> teacherlist) {
        this.teacherlist = teacherlist;
        // teachers should always be a list of one - set the single teacher, if available
        // in the teacher field
        if (this.teacherlist!=null && this.teacherlist.size()>0) {
        	teacher = teacherlist.get(0);
        }
    }
}
