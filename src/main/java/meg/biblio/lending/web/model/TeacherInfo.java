package meg.biblio.lending.web.model;

import meg.biblio.lending.db.dao.TeacherDao;

public class TeacherInfo {

    private Long id;

    private Long schoolgroupid;

    private String firstname;

    private String lastname;
    private String displayname;


    public TeacherInfo(TeacherDao teacher) {
        super();
        this.id = teacher.getId();
        this.schoolgroupid = teacher.getSchoolgroup().getId();
        this.firstname = teacher.getFirstname();
        this.lastname = teacher.getLastname();
        this.displayname = teacher.getFulldisplayname();
    }

    public Long getId() {
        return id;
    }

    public Long getSchoolgroupid() {
        return schoolgroupid;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }


}
