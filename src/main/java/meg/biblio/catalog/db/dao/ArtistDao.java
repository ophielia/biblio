package meg.biblio.catalog.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;

@Entity
@Table(name = "artist")
public class ArtistDao {

    private String lastname;
    private String firstname;
    private String middlename;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;


    public boolean hasFirstname() {
        return firstname != null && firstname.length() > 0;
    }

    public boolean hasMiddlename() {
        return middlename != null && middlename.length() > 0;
    }

    public boolean hasLastname() {
        return lastname != null && lastname.length() > 0;
    }

    public String getDisplayName() {
        StringBuffer display = new StringBuffer();
        if (hasFirstname()) {
            display.append(this.firstname).append(" ");
        }
        if (hasMiddlename()) {
            display.append(this.middlename).append(" ");
        }
        if (hasLastname()) {
            display.append(this.lastname).append(" ");
        }
        String displayname = display.toString();

        return displayname.trim();
    }

    public void copyFrom(ArtistDao artist) {
        if (artist.lastname != null) {
            this.lastname = artist.lastname;
        }
        if (artist.firstname != null) {
            this.firstname = artist.firstname;
        }
        if (artist.middlename != null) {
            this.middlename = artist.middlename;
        }

    }

    public String getFirstname() {
        return this.firstname;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getMiddlename() {
        return this.middlename;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getId() {
        return this.id;
    }

    public Integer getVersion() {
        return this.version;
    }
}
