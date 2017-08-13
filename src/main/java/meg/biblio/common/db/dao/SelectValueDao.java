package meg.biblio.common.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "select_value")
public class SelectValueDao {

    @NotNull
    @Size(max = 100)
    private String value;

    private String languagekey;

    @NotNull
    @Size(max = 100)
    private String display;


    private Boolean active;

    @NotNull
    private Long disporder;

    @ManyToOne
    @JoinColumn(name = "keyid")
    private SelectKeyDao selectkey;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getActive() {
        return this.active;
    }

    public Long getDisporder() {
        return this.disporder;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public String getDisplay() {
        return this.display;
    }

    public void setLanguagekey(String languagekey) {
        this.languagekey = languagekey;
    }

    public String getLanguagekey() {
        return this.languagekey;
    }

    public void setSelectkey(SelectKeyDao selectkey) {
        this.selectkey = selectkey;
    }

    public String getValue() {
        return this.value;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setDisporder(Long disporder) {
        this.disporder = disporder;
    }

    public SelectKeyDao getSelectkey() {
        return this.selectkey;
    }
}
