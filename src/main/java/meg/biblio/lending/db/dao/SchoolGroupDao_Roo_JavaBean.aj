// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package meg.biblio.lending.db.dao;

import java.util.List;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;

privileged aspect SchoolGroupDao_Roo_JavaBean {
    
    public ClientDao SchoolGroupDao.getClient() {
        return this.client;
    }
    
    public void SchoolGroupDao.setClient(ClientDao client) {
        this.client = client;
    }
    
    public List<TeacherDao> SchoolGroupDao.getTeacherlist() {
        return this.teacherlist;
    }
    
    public List<StudentDao> SchoolGroupDao.getStudents() {
        return this.students;
    }
    
    public void SchoolGroupDao.setStudents(List<StudentDao> students) {
        this.students = students;
    }
    
    public Integer SchoolGroupDao.getSchoolyearbegin() {
        return this.schoolyearbegin;
    }
    
    public void SchoolGroupDao.setSchoolyearbegin(Integer schoolyearbegin) {
        this.schoolyearbegin = schoolyearbegin;
    }
    
    public Integer SchoolGroupDao.getSchoolyearend() {
        return this.schoolyearend;
    }
    
    public void SchoolGroupDao.setSchoolyearend(Integer schoolyearend) {
        this.schoolyearend = schoolyearend;
    }
    
}