package meg.biblio.common.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Date;

@Table(name = "userlogin")
@Entity
public class UserLoginDao {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Size(min = 3, max = 50)
    private String username;

    @Size(min = 3, max = 250)
    private String password;

    private Boolean enabled;

    @Size(min = 3, max = 250)
    private String textpassword;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    private Date createdOn;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clientid")
    private ClientDao client;

    @OneToOne(fetch = FetchType.EAGER)
    private RoleDao role;

    @Transient
    private String rolename;

    @Transient
    private Long clientkey;

    @Transient
    private String passwordverify;

    @Transient
    private String oldpassword;
    @Version
    @Column(name = "version")
    private Integer version;


    public String getRolename() {
        return this.rolename;
    }

    public void setOldpassword(String oldpassword) {
        this.oldpassword = oldpassword;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getTextpassword() {
        return this.textpassword;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setPasswordverify(String passwordverify) {
        this.passwordverify = passwordverify;
    }

    public void setClient(ClientDao client) {
        this.client = client;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOldpassword() {
        return this.oldpassword;
    }

    public void setRole(RoleDao role) {
        this.role = role;
    }

    public String getPasswordverify() {
        return this.passwordverify;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public ClientDao getClient() {
        return this.client;
    }

    public String getUsername() {
        return this.username;
    }

    public void setClientkey(Long clientkey) {
        this.clientkey = clientkey;
    }

    public RoleDao getRole() {
        return this.role;
    }

    public String getPassword() {
        return this.password;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    public Boolean getEnabled() {
        return this.enabled;
    }

    public Date getCreatedOn() {
        return this.createdOn;
    }

    public Long getClientkey() {
        return this.clientkey;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setRolename(String rolename) {
        this.rolename = rolename;
    }

    public void setTextpassword(String textpassword) {
        this.textpassword = textpassword;
    }
}
