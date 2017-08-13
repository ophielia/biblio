package meg.biblio.common.db.dao;

import meg.biblio.common.db.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

@Configurable
@Component
public class ClientDaoDataOnDemand {
    @Autowired
    ClientRepository clientRepository;
    private Random rnd = new SecureRandom();
    private List<ClientDao> data;

    public void setImportmapconfig(ClientDao obj, int index) {
        String importmapconfig = "importmapconfig_" + index;
        obj.setImportmapconfig(importmapconfig);
    }

    public void setIdForBarcode(ClientDao obj, int index) {
        Boolean idForBarcode = Boolean.TRUE;
        obj.setIdForBarcode(idForBarcode);
    }

    public void setClassifyimplementation(ClientDao obj, int index) {
        String classifyimplementation = "classifyimplementation_" + index;
        obj.setClassifyimplementation(classifyimplementation);
    }

    public ClientDao getNewTransientClientDao(int index) {
        ClientDao obj = new ClientDao();
        setBarcodesheetxsl(obj, index);
        setClassifyimplementation(obj, index);
        setClasssummaryxslbase(obj, index);
        setClientnr(obj, index);
        setDefaultStatus(obj, index);
        setDetailCompleteCode(obj, index);
        setIdForBarcode(obj, index);
        setImagepath(obj, index);
        setImportfileconfig(obj, index);
        setImportmapconfig(obj, index);
        setLastBcBase(obj, index);
        setLastBookNr(obj, index);
        setName(obj, index);
        setOverduexslbase(obj, index);
        setShortname(obj, index);
        setStudentCOLimit(obj, index);
        setStudentcheckouttime(obj, index);
        setTeacherCOLimit(obj, index);
        setTeachercheckouttime(obj, index);
        setUsesBarcodes(obj, index);
        return obj;
    }

    public void setBarcodesheetxsl(ClientDao obj, int index) {
        String barcodesheetxsl = "barcodesheetxsl_" + index;
        obj.setBarcodesheetxsl(barcodesheetxsl);
    }

    public void setClasssummaryxslbase(ClientDao obj, int index) {
        String classsummaryxslbase = "classsummaryxslbase_" + index;
        obj.setClasssummaryxslbase(classsummaryxslbase);
    }

    public void setClientnr(ClientDao obj, int index) {
        Long clientnr = new Integer(index).longValue();
        obj.setClientnr(clientnr);
    }

    public void setLastBcBase(ClientDao obj, int index) {
        Long lastBcBase = new Integer(index).longValue();
        obj.setLastBcBase(lastBcBase);
    }

    public void setOverduexslbase(ClientDao obj, int index) {
        String overduexslbase = "overduexslbase_" + index;
        obj.setOverduexslbase(overduexslbase);
    }

    public ClientDao getRandomClientDao() {
        init();
        ClientDao obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getId();
        return clientRepository.findOne(id);
    }

    public ClientDao getSpecificClientDao(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        ClientDao obj = data.get(index);
        Long id = obj.getId();
        return clientRepository.findOne(id);
    }

    public void setDefaultStatus(ClientDao obj, int index) {
        Long defaultStatus = new Integer(index).longValue();
        obj.setDefaultStatus(defaultStatus);
    }

    public void setUsesBarcodes(ClientDao obj, int index) {
        Boolean usesBarcodes = Boolean.TRUE;
        obj.setUsesBarcodes(usesBarcodes);
    }

    public void setImportfileconfig(ClientDao obj, int index) {
        String importfileconfig = "importfileconfig_" + index;
        obj.setImportfileconfig(importfileconfig);
    }

    public void setLastBookNr(ClientDao obj, int index) {
        Long lastBookNr = new Integer(index).longValue();
        obj.setLastBookNr(lastBookNr);
    }

    public void setShortname(ClientDao obj, int index) {
        String shortname = "shortname_" + index;
        obj.setShortname(shortname);
    }

    public void setTeachercheckouttime(ClientDao obj, int index) {
        Integer teachercheckouttime = new Integer(index);
        obj.setTeachercheckouttime(teachercheckouttime);
    }

    public void setTeacherCOLimit(ClientDao obj, int index) {
        Integer teacherCOLimit = new Integer(index);
        obj.setTeacherCOLimit(teacherCOLimit);
    }

    public void setName(ClientDao obj, int index) {
        String name = "name_" + index;
        obj.setName(name);
    }

    public void setStudentcheckouttime(ClientDao obj, int index) {
        Integer studentcheckouttime = new Integer(index);
        obj.setStudentcheckouttime(studentcheckouttime);
    }

    public void setDetailCompleteCode(ClientDao obj, int index) {
        Long detailCompleteCode = new Integer(index).longValue();
        obj.setDetailCompleteCode(detailCompleteCode);
    }

    public void setImagepath(ClientDao obj, int index) {
        String imagepath = "imagepath_" + index;
        obj.setImagepath(imagepath);
    }

    public void setStudentCOLimit(ClientDao obj, int index) {
        Integer studentCOLimit = new Integer(index);
        obj.setStudentCOLimit(studentCOLimit);
    }

    public boolean modifyClientDao(ClientDao obj) {
        return false;
    }

    public void init() {
        int from = 0;
        int to = 10;
        data = clientRepository.findAll(new org.springframework.data.domain.PageRequest(from / to, to)).getContent();
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'ClientDao' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }

        data = new ArrayList<ClientDao>();
        for (int i = 0; i < 10; i++) {
            ClientDao obj = getNewTransientClientDao(i);
            try {
                clientRepository.save(obj);
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext(); ) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            clientRepository.flush();
            data.add(obj);
        }
    }
}
