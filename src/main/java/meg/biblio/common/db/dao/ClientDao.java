package meg.biblio.common.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@Entity
@Table(name = "client")
public class ClientDao {

    private Long clientnr;

    @NotNull
    private String name;

    private String importmapconfig;

    private String importfileconfig;

    private String classifyimplementation;

    private String imagepath;

    private Long lastBcBase;

    private String barcodesheetxsl;

    @OneToMany(mappedBy = "client", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    private List<UserLoginDao> users;

    private Integer studentcheckouttime;

    private Integer teachercheckouttime;

    private Integer studentCOLimit;

    private Integer teacherCOLimit;

    private String overduexslbase;

    private String shortname;

    private Long lastBookNr;

    private String classsummaryxslbase;

    private Boolean usesBarcodes;

    private Long detailCompleteCode;

    private Boolean idForBarcode;

    private Long defaultStatus;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public Integer getTeachercheckouttime() {
        return this.teachercheckouttime;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public void setTeachercheckouttime(Integer teachercheckouttime) {
        this.teachercheckouttime = teachercheckouttime;
    }

    public void setImportfileconfig(String importfileconfig) {
        this.importfileconfig = importfileconfig;
    }

    public void setOverduexslbase(String overduexslbase) {
        this.overduexslbase = overduexslbase;
    }

    public Long getLastBcBase() {
        return this.lastBcBase;
    }

    public void setImportmapconfig(String importmapconfig) {
        this.importmapconfig = importmapconfig;
    }

    public List<UserLoginDao> getUsers() {
        return this.users;
    }

    public void setTeacherCOLimit(Integer teacherCOLimit) {
        this.teacherCOLimit = teacherCOLimit;
    }

    public void setIdForBarcode(Boolean idForBarcode) {
        this.idForBarcode = idForBarcode;
    }

    public Long getClientnr() {
        return this.clientnr;
    }

    public String getOverduexslbase() {
        return this.overduexslbase;
    }

    public Long getDefaultStatus() {
        return this.defaultStatus;
    }

    public void setLastBookNr(Long lastBookNr) {
        this.lastBookNr = lastBookNr;
    }

    public void setUsesBarcodes(Boolean usesBarcodes) {
        this.usesBarcodes = usesBarcodes;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getStudentCOLimit() {
        return this.studentCOLimit;
    }

    public void setLastBcBase(Long lastBcBase) {
        this.lastBcBase = lastBcBase;
    }

    public void setDetailCompleteCode(Long detailCompleteCode) {
        this.detailCompleteCode = detailCompleteCode;
    }

    public String getClassifyimplementation() {
        return this.classifyimplementation;
    }

    public String getImportfileconfig() {
        return this.importfileconfig;
    }

    public void setStudentcheckouttime(Integer studentcheckouttime) {
        this.studentcheckouttime = studentcheckouttime;
    }

    public Integer getTeacherCOLimit() {
        return this.teacherCOLimit;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setUsers(List<UserLoginDao> users) {
        this.users = users;
    }

    public void setClientnr(Long clientnr) {
        this.clientnr = clientnr;
    }

    public void setBarcodesheetxsl(String barcodesheetxsl) {
        this.barcodesheetxsl = barcodesheetxsl;
    }

    public Long getDetailCompleteCode() {
        return this.detailCompleteCode;
    }

    public String getBarcodesheetxsl() {
        return this.barcodesheetxsl;
    }

    public String getShortname() {
        return this.shortname;
    }

    public void setClassifyimplementation(String classifyimplementation) {
        this.classifyimplementation = classifyimplementation;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImportmapconfig() {
        return this.importmapconfig;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return this.id;
    }

    public void setStudentCOLimit(Integer studentCOLimit) {
        this.studentCOLimit = studentCOLimit;
    }

    public Long getLastBookNr() {
        return this.lastBookNr;
    }

    public Boolean getIdForBarcode() {
        return this.idForBarcode;
    }

    public Boolean getUsesBarcodes() {
        return this.usesBarcodes;
    }

    public void setClasssummaryxslbase(String classsummaryxslbase) {
        this.classsummaryxslbase = classsummaryxslbase;
    }

    public String getImagepath() {
        return this.imagepath;
    }

    public Integer getStudentcheckouttime() {
        return this.studentcheckouttime;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public void setDefaultStatus(Long defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public String getClasssummaryxslbase() {
        return this.classsummaryxslbase;
    }

    public String getName() {
        return this.name;
    }
}

