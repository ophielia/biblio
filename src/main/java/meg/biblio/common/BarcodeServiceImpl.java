package meg.biblio.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import meg.biblio.common.db.ClientRepository;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.report.Barcode;
import meg.biblio.common.report.BarcodeSheet;
import meg.biblio.lending.ClassManagementService;
import meg.biblio.lending.db.StudentRepository;
import meg.biblio.lending.db.TeacherRepository;
import meg.biblio.lending.db.dao.SchoolGroupDao;
import meg.biblio.lending.db.dao.StudentDao;
import meg.biblio.lending.db.dao.TeacherDao;
import meg.biblio.lending.web.model.ClassModel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class BarcodeServiceImpl implements BarcodeService {

	// A10000001000
	private static final int barcodelength=12;
	private static final String filler="000000000000";
	
	@Autowired 
	ClientService clientService;
	
	@Autowired
	MessageSource  appMessageSource;
	
	@Autowired
	StudentRepository studentRepo;	

	
	@Autowired
	TeacherRepository teacherRepo;	
	
	@Autowired 
	ClientRepository clientRepo;
	
	
	@Autowired 
	ClassManagementService classService;	
	
	@Override
	public BarcodeSheet assembleBarcodeSheetForBooks(int barcodecnt, Long clientid,Locale locale) {
		// get client
		ClientDao client = clientService.getClientForKey(clientid);
		// message
		String message = client.getName();
		// get codelist (length barcodecnt)
		List<Barcode> codes = generateCodes(barcodecnt, BarcodeService.CodeType.BOOK,client,message);
		// place codes in BookBarcodeSheet, with client
		String base = appMessageSource.getMessage("reports_barcode_booktitle",null, locale);
		String title = base + client.getName();
		BarcodeSheet sheet = new BarcodeSheet(codes,title);
		// return BookBarcodeSheet
		return sheet;
	}
	
	@Override
	public BarcodeSheet assembleBarcodeSheetForClass(Long classId, Long clientid, Locale locale) {
		// get client
		ClientDao client = clientService.getClientForKey(clientid);

		// assign codes to any students without codes
		SchoolGroupDao schoolgroup = classService.getClassForClient(classId, clientid);
		List<StudentDao> nocodestudents = studentRepo.findActiveStudentsForClassWithoutBarcode(schoolgroup, client,  null);
		if (nocodestudents!=null && nocodestudents.size()>0) {
			List<StudentDao> toupdate = new ArrayList<StudentDao>(); 
			List<Barcode> newcodes = generateCodes(nocodestudents.size(),BarcodeService.CodeType.PERSON,client,"");
			int i=0;
			for (StudentDao student:nocodestudents) {
				StudentDao toupd = studentRepo.findOne(student.getId());
				Barcode bc = newcodes.get(i);
				toupd.setBarcodeid(bc.getCode());
				toupdate.add(toupd);
				i++;
			}
			studentRepo.save(toupdate);
		}
		
		// ensure that teacher has a code
		List<TeacherDao> nocodeteachers = teacherRepo.findActiveTeachersForClientAndClassWithoutBarcode(client, schoolgroup);
		if (nocodeteachers!=null && nocodeteachers.size()>0) {
			List<Barcode> newcodes = generateCodes(nocodeteachers.size(),BarcodeService.CodeType.PERSON,client,"");
			int i=0;
			for (TeacherDao teacher:nocodeteachers) {
				TeacherDao toupd = teacherRepo.findOne(teacher.getId());
				Barcode bc = newcodes.get(i);
				toupd.setBarcodeid(bc.getCode());
				teacherRepo.save(toupd);
				i++;
			}
			
		}		
		
		// load classmodel
		ClassModel model = classService.loadClassModelById(schoolgroup.getId());
		TeacherDao teacher = model.getTeacher();
		List<StudentDao> students = model.getStudents();
		
		// get message for title
		String teachername = model.getTeacher().getFulldisplayname();
		String base = appMessageSource.getMessage("reports_barcode_classtitle",null, locale);

		String title = base + teachername;
		
		// make list for codes
		List<Barcode> classcodes = new ArrayList<Barcode>();
		
		// add teacher to list - filling in message
		Barcode bc = new Barcode(teacher.getBarcodeid(),teacher.getFulldisplayname());
		classcodes.add(bc);
		
		// add all students to list - filling in message
		for (StudentDao student:students) {
			bc = new Barcode(student.getBarcodeid(),student.getFulldisplayname());
			classcodes.add(bc);
		}
		// construct BarcodeSheet
		BarcodeSheet bcs = new BarcodeSheet(classcodes,title);
		// return BarcodeSheet
		return bcs;
		
		
		
		
		
		
	}	

	private List<Barcode> generateCodes(int length, String codetype,
			ClientDao client,String message ) {
		// make resultlist
		List<Barcode> results = new ArrayList<Barcode>();
		
		// make prefix
		Long clientid = client.getClientnr();
		String prefix = codetype + clientid;

		// generate new codegendao (as many as length)
		Long lastused = client.getLastBcBase();
		int begin = lastused.intValue()+1;
		Long after = lastused + length + 1;
		client = clientRepo.findOne(client.getId());
		client.setLastBcBase(after);
		clientRepo.save(client);
		
		for (int i=begin;i<after.intValue();i++) {
			String base = String.valueOf(i);
			int fillerlength = barcodelength - base.length(); 
			String codefiller = filler.substring(0,fillerlength);
			String barcode = prefix + codefiller + base;
			Barcode bc = new Barcode(barcode,message);
			results.add(bc);			
		}
		
		// return resultlist
		return results;
	}
	

}
