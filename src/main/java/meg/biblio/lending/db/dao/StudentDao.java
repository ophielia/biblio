package meg.biblio.lending.db.dao;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.Entity;

@Entity
public class StudentDao extends PersonDao {


    private Long sectionkey;


    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public Long getSectionkey() {
        return this.sectionkey;
    }

    public void setSectionkey(Long sectionkey) {
        this.sectionkey = sectionkey;
    }
}
