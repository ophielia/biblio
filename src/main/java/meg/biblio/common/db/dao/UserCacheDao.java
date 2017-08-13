package meg.biblio.common.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "usercache")
public class UserCacheDao {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cacheuser")
    private UserLoginDao cacheuser;

    @Temporal(TemporalType.TIMESTAMP)
    private Date expiration;

    private String cachetag;

    private String name;

    private String value;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;


    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getValue() {
        return this.value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCachetag(String cachetag) {
        this.cachetag = cachetag;
    }

    public Date getExpiration() {
        return this.expiration;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getId() {
        return this.id;
    }

    public String getCachetag() {
        return this.cachetag;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setCacheuser(UserLoginDao cacheuser) {
        this.cacheuser = cacheuser;
    }

    public String getName() {
        return this.name;
    }

    public void setExpiration(Date expiration) {
        this.expiration = expiration;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserLoginDao getCacheuser() {
        return this.cacheuser;
    }
}
