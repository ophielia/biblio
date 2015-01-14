package meg.biblio.lending.db.dao;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import meg.biblio.common.db.dao.ClientDao;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.jpa.entity.RooJpaEntity;
import org.springframework.roo.addon.tostring.RooToString;

@RooJavaBean
@RooToString
@RooJpaEntity(inheritanceType = "SINGLE_TABLE")
@Table(name="person")
@DiscriminatorColumn(name="PSN_TYPE")
public class PersonDao {

	@NotNull
	private String firstname;

	@NotNull
	private String lastname;

	private String barcodeid;
	
	@OneToOne(cascade = CascadeType.PERSIST, fetch=FetchType.EAGER)
	private ClientDao client;

	@ManyToOne(fetch=FetchType.EAGER)
	protected SchoolGroupDao schoolgroup;

	private Boolean active;
	
	@Column(name="PSN_TYPE", insertable=false, updatable=false)
	private String psn_type;

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
				// last member of list is last name
				String lastname = tknlist.remove(tknlist.size() - 1);
				this.lastname = lastname;
				// if members remaining, first member is firstname
				if (tknlist.size() > 0) {
					StringBuffer firstname = new StringBuffer();
					for (String namepart:tknlist) {
						firstname.append(namepart).append(" ");
					}
					this.firstname = firstname.toString().trim();
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
		if (getFirstname()!=null) {
			display.append(getFirstname()).append(" ");
		}
		if (getLastname()!=null) {
			display.append(getLastname());
		}
		String returnstr = display.toString().trim();
		return returnstr;
	}
}
