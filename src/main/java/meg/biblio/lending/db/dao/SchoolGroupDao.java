package meg.biblio.lending.db.dao;

import meg.biblio.common.db.dao.ClientDao;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class SchoolGroupDao {

    @OneToOne(fetch = FetchType.EAGER)
    private ClientDao client;

    @JoinColumn(name = "schoolgroup", insertable = false, updatable = false)
    @Where(clause = "psn_type='TeacherDao'")
    @ElementCollection(targetClass = TeacherDao.class)
    private List<TeacherDao> teacherlist;

    @JoinColumn(name = "schoolgroup", insertable = false, updatable = false)
    @Where(clause = "psn_type='StudentDao'")
    @ElementCollection(targetClass = StudentDao.class)
    @OrderBy("firstname asc,sectionkey asc")
    private List<StudentDao> students;

    private Integer schoolyearbegin;

    private Integer schoolyearend;

    @Transient
    private TeacherDao teacher;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public int getClasscount() {
        if (students != null) {
            return students.size();
        }
        return 0;
    }


    public String getSchoolyeardisplay() {
        String display = schoolyearbegin + " / " + schoolyearend;
        return display;
    }

    public TeacherDao getTeacher() {
        if (this.teacherlist != null && this.teacherlist.size() > 0) {
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
        if (this.teacherlist != null && this.teacherlist.size() > 0) {
            teacher = teacherlist.get(0);
        }
    }

    public Integer getSchoolyearend() {
        return this.schoolyearend;
    }

    public ClientDao getClient() {
        return this.client;
    }

    public void setClient(ClientDao client) {
        this.client = client;
    }

    public List<TeacherDao> getTeacherlist() {
        return this.teacherlist;
    }

    public List<StudentDao> getStudents() {
        return this.students;
    }

    public void setStudents(List<StudentDao> students) {
        this.students = students;
    }

    public Integer getSchoolyearbegin() {
        return this.schoolyearbegin;
    }

    public void setSchoolyearbegin(Integer schoolyearbegin) {
        this.schoolyearbegin = schoolyearbegin;
    }

    public void setSchoolyearend(Integer schoolyearend) {
        this.schoolyearend = schoolyearend;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
