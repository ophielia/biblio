// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package meg.biblio.inventory.db.dao;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Version;
import meg.biblio.inventory.db.dao.InventoryDao;

privileged aspect InventoryDao_Roo_Jpa_Entity {
    
    declare @type: InventoryDao: @Entity;
    
    declare @type: InventoryDao: @Inheritance(strategy = InheritanceType.SINGLE_TABLE);
    
    declare @type: InventoryDao: @DiscriminatorColumn;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long InventoryDao.id;
    
    @Version
    @Column(name = "version")
    private Integer InventoryDao.version;
    
    public Long InventoryDao.getId() {
        return this.id;
    }
    
    public void InventoryDao.setId(Long id) {
        this.id = id;
    }
    
    public Integer InventoryDao.getVersion() {
        return this.version;
    }
    
    public void InventoryDao.setVersion(Integer version) {
        this.version = version;
    }
    
}
