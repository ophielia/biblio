package meg.biblio.catalog.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;

@Entity
@Table(name = "classification")
public class ClassificationDao {

    private Long clientid;
    private Long key;
    private String textdisplay;
    private String imagedisplay;
    private String language;
    private String description;
    @Version
    @Column(name = "version")
    private Integer version;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;


    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
        return this.version;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setTextdisplay(String textdisplay) {
        this.textdisplay = textdisplay;
    }

    public void setClientid(Long clientid) {
        this.clientid = clientid;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImagedisplay() {
        return this.imagedisplay;
    }

    public Long getId() {
        return this.id;
    }

    public Long getKey() {
        return this.key;
    }

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setImagedisplay(String imagedisplay) {
        this.imagedisplay = imagedisplay;
    }

    public String getDescription() {
        return this.description;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTextdisplay() {
        return this.textdisplay;
    }

    public Long getClientid() {
        return this.clientid;
    }
}
