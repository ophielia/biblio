package meg.biblio.common.web;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.Locale;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import meg.biblio.common.BarcodeService;
import meg.biblio.common.ClientService;
import meg.biblio.common.db.dao.ClientDao;
import meg.biblio.common.report.BarcodeSheet;
import meg.biblio.common.report.ClassSummaryReport;
import meg.biblio.common.report.OverdueBookReport;
import meg.biblio.lending.LendingService;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.fop.servlet.ServletContextURIResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/pdfwrangler")
@Controller
public class PDFController {

	@Autowired
	protected ClientService clientService;

	@Autowired
	protected LendingService lendingService;

	@Autowired
	protected BarcodeService barcodeService;
	
	@Autowired
	private ServletContext servletContext;

	private FopFactory fopFactory = FopFactory.newInstance();
	private TransformerFactory tFactory = TransformerFactory.newInstance();

	private String transformdir = "resources/transform/";
	
	protected URIResolver uriResolver;
	
	public URIResolver getResolver() throws ServletException {
		if (this.uriResolver!=null) return this.uriResolver;
		this.uriResolver= new ServletContextURIResolver(servletContext);
		return this.uriResolver;
	}

	@RequestMapping(value = "/overduenotices", method = RequestMethod.POST)
	public void generateOverdueReport(Model uiModel,
			HttpServletRequest request, HttpServletRequest httpServletRequest,
			HttpServletResponse response, Principal principal)
			throws FOPException, JAXBException, TransformerException,
			IOException, ServletException {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		String cxslname = "META-INF/web-resources/transform/" + client.getOverduexslbase() + "-" + lang + ".xsl";

		OverdueBookReport obr = lendingService
				.assembleOverdueBookReport(clientkey);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {

			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
			JAXBContext context = JAXBContext
					.newInstance(OverdueBookReport.class);
			JAXBSource source = new JAXBSource(context, obr);

			// Setup Transformer
			Resource resource = new ClassPathResource(cxslname);
			Source xsltSrc = new StreamSource(resource.getFile());
			Transformer transformer = tFactory.newTransformer(xsltSrc);

			// Make sure the XSL transformation's result is piped through to FOPx
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start the transformation and rendering process
			transformer.transform(source, res);

			// prepare response
			response.setContentType("application/pdf");
			response.setContentLength(out.size());

			// send content to browser
			response.getOutputStream().write(out.toByteArray());
			response.getOutputStream().flush();
		} finally {
			out.close();
		}

	}

	@RequestMapping(value = "/classsummary/{id}", method = RequestMethod.POST, produces = "text/html")
	public void generateClassSummaryReport(@PathVariable("id") Long classid, Model uiModel,
			HttpServletRequest request, HttpServletRequest httpServletRequest,
			HttpServletResponse response, Principal principal)
			throws FOPException, JAXBException, TransformerException,
			IOException, ServletException {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		String cxslname = "META-INF/web-resources/transform/" + client.getClasssummaryxslbase() + "-" + lang + ".xsl";

		ClassSummaryReport csr = lendingService
				.assembleClassSummaryReport(classid, new Date(), clientkey);

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {

			Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
			JAXBContext context = JAXBContext
					.newInstance(ClassSummaryReport.class);
			JAXBSource source = new JAXBSource(context, csr);

			// Setup Transformer
			Resource resource = new ClassPathResource(cxslname);
			Source xsltSrc = new StreamSource(resource.getFile());
			Transformer transformer = tFactory.newTransformer(xsltSrc);

			// Make sure the XSL transformation's result is piped through to FOPx
			Result res = new SAXResult(fop.getDefaultHandler());

			// Start the transformation and rendering process
			transformer.transform(source, res);

			// prepare response
			response.setContentType("application/pdf");
			response.setContentLength(out.size());

			// send content to browser
			response.getOutputStream().write(out.toByteArray());
			response.getOutputStream().flush();
		} finally {
			out.close();
		}
	}
	
	@RequestMapping(value = "/bookbarcodes", method = RequestMethod.POST, produces = "text/html")
	public void generateBookBarcodeSheet(
			@RequestParam("codeCount") Integer codeCount, Model uiModel,
			HttpServletRequest request, HttpServletRequest httpServletRequest,
			HttpServletResponse response, Principal principal)
			throws FOPException, JAXBException, TransformerException,
			IOException, ServletException {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		String cxslname = "META-INF/web-resources/transform/"
				+ client.getBarcodesheetxsl() + ".xsl";

		if (codeCount != null) {

			int codecount = codeCount.intValue();
			BarcodeSheet sheet = barcodeService.assembleBarcodeSheetForBooks(
					codecount, clientkey, locale);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {

				Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
				JAXBContext context = JAXBContext
						.newInstance(BarcodeSheet.class);
				JAXBSource source = new JAXBSource(context, sheet);

				// Setup Transformer
				Resource resource = new ClassPathResource(cxslname);
				Source xsltSrc = new StreamSource(resource.getFile());
				Transformer transformer = tFactory.newTransformer(xsltSrc);

				// Make sure the XSL transformation's result is piped through to
				// FOPx
				Result res = new SAXResult(fop.getDefaultHandler());

				// Start the transformation and rendering process
				transformer.transform(source, res);

				// prepare response
				response.setContentType("application/pdf");
				response.setContentLength(out.size());

				// send content to browser
				response.getOutputStream().write(out.toByteArray());
				response.getOutputStream().flush();
			} finally {
				out.close();
			}
		}
	}
	
	@RequestMapping(value = "/classbarcodes", method = RequestMethod.POST, produces = "text/html")
	public void generateClassBarcodeSheet(
			@RequestParam("classId") Long classId, Model uiModel,
			HttpServletRequest request, HttpServletRequest httpServletRequest,
			HttpServletResponse response, Principal principal)
			throws FOPException, JAXBException, TransformerException,
			IOException, ServletException {
		ClientDao client = clientService.getCurrentClient(principal);
		Long clientkey = client.getId();
		Locale locale = httpServletRequest.getLocale();
		String lang = locale.getLanguage();

		String cxslname = "META-INF/web-resources/transform/"
				+ client.getBarcodesheetxsl() + ".xsl";

		if (classId != null) {

			BarcodeSheet sheet = barcodeService.assembleBarcodeSheetForClass(classId, clientkey);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {

				Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, out);
				JAXBContext context = JAXBContext
						.newInstance(BarcodeSheet.class);
				JAXBSource source = new JAXBSource(context, sheet);

				// Setup Transformer
				Resource resource = new ClassPathResource(cxslname);
				Source xsltSrc = new StreamSource(resource.getFile());
				Transformer transformer = tFactory.newTransformer(xsltSrc);

				// Make sure the XSL transformation's result is piped through to
				// FOPx
				Result res = new SAXResult(fop.getDefaultHandler());

				// Start the transformation and rendering process
				transformer.transform(source, res);

				// prepare response
				response.setContentType("application/pdf");
				response.setContentLength(out.size());

				// send content to browser
				response.getOutputStream().write(out.toByteArray());
				response.getOutputStream().flush();
			} finally {
				out.close();
			}
		}
	}	
}
