package pl.radekbonk.controller;

import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pl.radekbonk.entity.ProblemEntity;
import pl.radekbonk.entity.ReportEntity;
import pl.radekbonk.entity.TaskEntity;
import pl.radekbonk.service.ProblemsService;
import pl.radekbonk.service.ProductClient;
import pl.radekbonk.service.ReportsService;
import pl.radekbonk.service.TasksService;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/")
public class ReportController {

	/*@Autowired
	private ProductsService productsService;*/

	@Autowired
	private ProblemsService problemsService;

	@Autowired
	private ReportsService reportsService;

	@Autowired
	private TasksService tasksService;

	@Autowired
	private ProductClient productClient;

	@GetMapping(value = "/viewReport", params = {"reportId", "productId"})
	public ModelAndView getReportById(@RequestParam(value = "reportId") long reportId, @RequestParam(value = "productId") long productId, Model model) {
		String productName = productClient.getName(String.valueOf(productId));
		try {
			ReportEntity report = reportsService.findOne(reportId);
			if (productId == report.getProductId()) {
				model.addAttribute("productIdAx", productClient.checkId(String.valueOf(productId)));
			} else {
				System.out.println("Product Id Conflict");
				throw new NullPointerException("Product Id conflict");
			}
			model.addAttribute("reportRev", report.getRevision());
			model.addAttribute("reportId", reportId);
			//model.addAttribute("productName", reportsService.findOne(reportId).getProduct().getName());
			model.addAttribute("productName", productName);
			model.addAttribute("reports", reportsService.findByProductIdOrderByRevisionDesc(report.getProductId()));
			model.addAttribute("problem", new ProblemEntity());
			model.addAttribute("task", new TaskEntity());
			model.addAttribute("problems", problemsService.findByReportId(reportId));
			model.addAttribute("majorProblems", problemsService.findMajorByReportId(reportId));
			model.addAttribute("minorProblems", problemsService.findMinorByReportId(reportId));
			model.addAttribute("tasks", tasksService.findByReportId(reportId));
			model.addAttribute("summary", report.getSummary());
			model.addAttribute("conclusion", report.getConclusion());
			model.addAttribute("report", reportsService.findOne(reportId));
			return new ModelAndView("page_view_report");
		} catch (NullPointerException e) {
			System.out.println("No such a report");
			model.addAttribute("alert", "Wybierz poprawny raport lub utwórz nowy");
			model.addAttribute("productIdAx", productClient.checkId(String.valueOf(productId)));
			//model.addAttribute("productName", productsService.getProductById(productId).getName());
			model.addAttribute("productName", productName);
			model.addAttribute("reports", reportsService.findByProductIdOrderByRevisionDesc(productId));
			return new ModelAndView("page_revision");
		}
	}

	/*@GetMapping(value = "/viewReportClient", params = "reportId")
	public ModelAndView viewReportClient(@RequestParam(value = "reportId") long reportId, Model model) {
		//String productName = productClient.getName(String.valueOf(productId));
		ReportEntity report = reportsService.findOne(reportId);
		model.addAttribute("productIdAx", report.getProduct().getId());
		model.addAttribute("reportRev", report.getRevision());
		model.addAttribute("reportId", reportId);
		model.addAttribute("productName", reportsService.findOne(reportId).getProduct().getName());
		model.addAttribute("reports", reportsService.findByProductIdOrderByRevisionDesc(report.getProduct().getId()));
		model.addAttribute("majorProblems", problemsService.findMajorByReportId(reportId));
		model.addAttribute("minorProblems", problemsService.findMinorByReportId(reportId));
		model.addAttribute("summary", report.getSummary());
		model.addAttribute("conclusion", report.getConclusion());
		model.addAttribute("report",reportsService.findOne(reportId));
		return new ModelAndView("page_view_report_client");
	}*/


	@GetMapping(value = "/products", params = {"id"})
	public ModelAndView getReportById(@RequestParam(value = "id") long id, Model model) {
		try {
			String productName = productClient.getName(String.valueOf(id));
			//String productName = productsService.getProductById(id).getName();
			model.addAttribute("productIdAx", productClient.checkId(String.valueOf(id)));
			model.addAttribute("productName", productName);
			model.addAttribute("reports", reportsService.findByProductIdOrderByRevisionDesc(id));
			return new ModelAndView("page_revision");
		} catch (NullPointerException e) {
			System.out.println("No such an ID");
			model.addAttribute("alert", "Nie ma takiego ID");
			return new ModelAndView("page_product");
		}
	}

	@PostMapping(value = "/report", params = {"productId"})
	@ResponseBody
	public String createReport(@RequestParam(value = "productId") long productId, @RequestParam(value = "copyLast") boolean copyLast, Authentication authentication) {
		ReportEntity newReport = new ReportEntity(reportsService.getNewRevision(productId), authentication.getPrincipal().toString().split(Pattern.quote("\\"))[1], "", new ArrayList<>(), "", "", productId, "", "", "","");
		//newReport.setProduct(productsService.getProductById(productId));
		reportsService.save(newReport, copyLast, productId);
		return "/products?productId=" + productId + "&reportId=" + newReport.getId();
	}


	@GetMapping(value = "/report", params = {"reportId"})
	public String viewReport(@RequestParam(value = "reportId") long reportId, Model model) {
		ReportEntity report = reportsService.findOne(reportId);
		model.addAttribute("introduction", report.getIntroduction());
		model.addAttribute("summary", report.getSummary());
		model.addAttribute("conclusion", report.getConclusion());
		model.addAttribute("productEngineer", report.getProductEngineer());
		model.addAttribute("processEngineer", report.getProcessEngineer());
		model.addAttribute("qualityEngineer", report.getQualityEngineer());
		model.addAttribute("reportId", reportId);
		model.addAttribute("report", report);
		return "summary :: summaryFragment";
	}

	@PostMapping(value = "/report", params = {"reportId"})
	public ModelAndView saveReport(@RequestParam(value = "reportId") long reportId, @RequestParam(value = "introduction") String introduction, @RequestParam(value = "summary") String summary, @RequestParam(value = "conclusion") String conclusion, @RequestParam(value = "productEngineer") String productEngineer, @RequestParam(value = "processEngineer") String processEngineer, @RequestParam(value = "qualityEngineer") String qualityEngineer, @RequestParam(value = "attachment[]", required = false) MultipartFile[] attachments, @RequestParam(value = "attachmentsToDelete[]", required = false) String[] attachmentsToDelete) {
		ReportEntity reportEntity = reportsService.findOne(reportId);
		reportEntity.setIntroduction(introduction);
		reportEntity.setSummary(summary);
		reportEntity.setConclusion(conclusion);
		reportEntity.setProductEngineer(productEngineer);
		reportEntity.setProcessEngineer(processEngineer);
		reportEntity.setQualityEngineer(qualityEngineer);
		reportsService.save(reportEntity, attachments, attachmentsToDelete);
		return new ModelAndView("redirect:/report?reportId=" + reportId);
	}

	@PostMapping(value = "generateExcel", params = "reportId")
	public void getExcel(
			@RequestParam("reportId") long reportId,
			HttpServletResponse response) {
		String fileName = reportsService.getReportNameZip(reportId);
		try {
			InputStream is = new FileInputStream(reportsService.generateExcel(reportId));
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			org.apache.poi.util.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException ex) {
			//throw new RuntimeException("IOError writing file to output stream");
			ex.printStackTrace();
		}
	}

	@PostMapping(value = "generateWord", params = {"reportId", "language"})
	public void getWordPolish(@RequestParam("reportId") long reportId, @RequestParam("language") String language, HttpServletResponse response) {
		String fileName = reportsService.getReportNameZip(reportId);
		System.out.println(language);
		try {
			InputStream is = new FileInputStream(reportsService.generateWord(reportId, language));
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
			IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (Exception ex) {
//			throw new RuntimeException("IOError writing file to output stream");
			ex.printStackTrace();
		}
	}

}
