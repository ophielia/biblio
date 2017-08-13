package meg.biblio.catalog.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;

@Entity
@Table(name = "subject")
public class SubjectDao {
    private String listing;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;


    public void copyFrom(SubjectDao copyfrom) {
        if (copyfrom != null) {
            if (copyfrom.listing != null) {
                this.listing = copyfrom.listing;
            }
        }
    }

    public Integer getVersion() {
        return this.version;
    }

    public String getListing() {
        return this.listing;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setListing(String listing) {
        this.listing = listing;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
