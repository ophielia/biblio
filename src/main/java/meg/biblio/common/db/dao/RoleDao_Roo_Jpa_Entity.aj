// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package meg.biblio.common.db.dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import meg.biblio.common.db.dao.RoleDao;

privileged aspect RoleDao_Roo_Jpa_Entity {
    
    declare @type: RoleDao: @Entity;
    
    declare @type: RoleDao: @Table(name = "grouprole");
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long RoleDao.id;
    
    @Version
    @Column(name = "version")
    private Integer RoleDao.version;
    
    public Long RoleDao.getId() {
        return this.id;
    }
    
    public void RoleDao.setId(Long id) {
        this.id = id;
    }
    
    public Integer RoleDao.getVersion() {
        return this.version;
    }
    
    public void RoleDao.setVersion(Integer version) {
        this.version = version;
    }
    
}