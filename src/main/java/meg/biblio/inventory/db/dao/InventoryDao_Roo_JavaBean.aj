// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package meg.biblio.inventory.db.dao;

import java.util.Date;
import meg.biblio.inventory.db.dao.InventoryDao;

privileged aspect InventoryDao_Roo_JavaBean {
    
    public Long InventoryDao.getClientid() {
        return this.clientid;
    }
    
    public void InventoryDao.setClientid(Long clientid) {
        this.clientid = clientid;
    }
    
    public Date InventoryDao.getStartdate() {
        return this.startdate;
    }
    
    public void InventoryDao.setStartdate(Date startdate) {
        this.startdate = startdate;
    }
    
    public Date InventoryDao.getEnddate() {
        return this.enddate;
    }
    
    public void InventoryDao.setEnddate(Date enddate) {
        this.enddate = enddate;
    }
    
    public Integer InventoryDao.getTobecounted() {
        return this.tobecounted;
    }
    
    public void InventoryDao.setTobecounted(Integer tobecounted) {
        this.tobecounted = tobecounted;
    }
    
    public Integer InventoryDao.getTotalcounted() {
        return this.totalcounted;
    }
    
    public void InventoryDao.setTotalcounted(Integer totalcounted) {
        this.totalcounted = totalcounted;
    }
    
    public Integer InventoryDao.getAddedtocount() {
        return this.addedtocount;
    }
    
    public void InventoryDao.setAddedtocount(Integer addedtocount) {
        this.addedtocount = addedtocount;
    }
    
    public Integer InventoryDao.getReconciled() {
        return this.reconciled;
    }
    
    public void InventoryDao.setReconciled(Integer reconciled) {
        this.reconciled = reconciled;
    }
    
    public Boolean InventoryDao.getCompleted() {
        return this.completed;
    }
    
    public void InventoryDao.setCompleted(Boolean completed) {
        this.completed = completed;
    }
    
}