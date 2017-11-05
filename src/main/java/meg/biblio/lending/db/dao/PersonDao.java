package meg.biblio.lending.db.dao;

import meg.biblio.common.db.dao.ClientDao;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@Entity
@Table(name = "person")
@DiscriminatorColumn(name = "PSN_TYPE")
public class PersonDao {

    private String firstname;

    @NotNull
    private String lastname;

    private String barcodeid;

    @OneToOne(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
    @JoinColumn(name="client")
    private ClientDao client;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="schoolgroup")
    protected SchoolGroupDao schoolgroup;

    private Boolean active;

    @Column(name = "PSN_TYPE", insertable = false, updatable = false)
    private String psn_type;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;
    @Version
    @Column(name = "version")
    private Integer version;

    public void fillInName(String text) {
        if (text != null) {
            if (text.contains(",")) {
                // break text by comma
                String[] tokens = text.trim().split(",");
                List<String> tknlist = arrayToList(tokens);
                // first member goes to last name
                String lastname = tknlist.remove(0);
                this.lastname = lastname;
                if (tknlist.size() > 0) {
                    // break remaining by space
                    String remaining = tknlist.get(0);
                    this.firstname = remaining.trim();
                }
            } else {
                // break name into list
                String[] tokens = text.trim().split(" ");
                List<String> tknlist = arrayToList(tokens);
                // first member of list is first name
                String firstname = tknlist.remove(0);
                this.firstname = firstname;
                // if members remaining, go into lastname
                if (tknlist.size() > 0) {
                    StringBuffer lastname = new StringBuffer();
                    for (String namepart : tknlist) {
                        lastname.append(namepart).append(" ");
                    }
                    this.lastname = lastname.toString().trim();
                }
            }

        }

    }


    private List<String> arrayToList(String[] tokens) {
        List<String> list = new ArrayList<String>();
        if (tokens != null) {
            for (int i = 0; i < tokens.length; i++) {
                list.add(tokens[i]);
            }
        }
        return list;
    }


    public String getFulldisplayname() {
        StringBuffer display = new StringBuffer();
        if (getFirstname() != null) {
            display.append(getFirstname()).append(" ");
        }
        if (getLastname() != null) {
            display.append(getLastname());
        }
        String returnstr = display.toString().trim();
        return returnstr;
    }

    public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getBarcodeid() {
        return this.barcodeid;
    }

    public void setBarcodeid(String barcodeid) {
        this.barcodeid = barcodeid;
    }

    public ClientDao getClient() {
        return this.client;
    }

    public void setClient(ClientDao client) {
        this.client = client;
    }

    public SchoolGroupDao getSchoolgroup() {
        return this.schoolgroup;
    }

    public String getPsn_type() {
        return this.psn_type;
    }

    public void setSchoolgroup(SchoolGroupDao schoolgroup) {
        this.schoolgroup = schoolgroup;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setPsn_type(String psn_type) {
        this.psn_type = psn_type;
    }

    public Boolean getActive() {
        return this.active;
    }
}
