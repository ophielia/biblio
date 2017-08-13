package meg.biblio.common.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "select_key")
public class SelectKeyDao {

    @NotNull
    @Size(max = 100)
    private String lookup;


    @OneToMany(mappedBy = "selectkey", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<SelectValueDao> selectvalues;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public Integer getVersion() {
        return this.version;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public void setSelectvalues(List<SelectValueDao> selectvalues) {
        this.selectvalues = selectvalues;
    }

    public Long getId() {
        return this.id;
    }

    public void setLookup(String lookup) {
        this.lookup = lookup;
    }

    public String getLookup() {
        return this.lookup;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public List<SelectValueDao> getSelectvalues() {
        return this.selectvalues;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
