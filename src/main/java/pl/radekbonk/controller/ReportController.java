package pl.radekbonk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pl.radekbonk.entity.ProblemEntity;
import pl.radekbonk.entity.ReportEntity;
import pl.radekbonk.entity.TaskEntity;
import pl.radekbonk.service.ProblemsService;
import pl.radekbonk.service.ProductsService;
import pl.radekbonk.service.ReportsService;
import pl.radekbonk.service.TasksService;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

@Controller
@RequestMapping("/")
public class ReportController {

	@Autowired
	private ProductsService productsService;

	@Autowired
	private ProblemsService problemsService;

	@Autowired
	private ReportsService reportsService;

	@Autowired
	private TasksService tasksService;

	@GetMapping(value = "/viewReport", params = {"reportId", "productId"})
	public ModelAndView getReportById(@RequestParam(value = "reportId") long reportId, @RequestParam(value = "productId") long productId, Model model) {
		try {
			ReportEntity report = reportsService.findOne(reportId);
			if (productId == report.getProduct().getId()) {
				model.addAttribute("productIdAx", report.getProduct().getId());
			} else {
				System.out.println("Product Id Conflict");
				throw new NullPointerException("Product Id conflict");
			}
			model.addAttribute("reportRev", report.getRevision());
			model.addAttribute("reportId", reportId);
			model.addAttribute("productName", reportsService.findOne(reportId).getProduct().getName());
			model.addAttribute("reports", reportsService.findByProductIdOrderByRevisionDesc(report.getProduct().getId()));
			model.addAttribute("problem", new ProblemEntity());
			model.addAttribute("task", new TaskEntity());
			model.addAttribute("problems", problemsService.findByReportId(reportId));
			model.addAttribute("majorProblems", problemsService.findMajorByReportId(reportId));
			model.addAttribute("minorProblems", problemsService.findMinorByReportId(reportId));
			model.addAttribute("tasks", tasksService.findByReportId(reportId));
			model.addAttribute("summary", report.getSummary());
			model.addAttribute("conclusion", report.getConclusion());
			model.addAttribute("report",reportsService.findOne(reportId));
			return new ModelAndView("page_view_report");
		} catch (NullPointerException e) {
			System.out.println("No such a report");
			model.addAttribute("alert", "Wybierz poprawny raport lub utwórz nowy");
			model.addAttribute("productIdAx", productId);
			model.addAttribute("productName", productsService.getProductById(productId).getName());
			model.addAttribute("reports", reportsService.findByProductIdOrderByRevisionDesc(productId));
			return new ModelAndView("page_revision");
		}
	}


	@GetMapping(value = "/products", params = {"id"})
	public ModelAndView getReportById(@RequestParam(value = "id") long id, Model model) {
		try {
			String productName = productsService.getProductById(id).getName();
			model.addAttribute("productIdAx", id);
			model.addAttribute("productName", productName);
			model.addAttribute("reports", reportsService.findByProductIdOrderByRevisionDesc(id));
			return new ModelAndView("page_revision");
		} catch(NullPointerException e) {
			System.out.println("No such an ID");
			model.addAttribute("alert","Nie ma takiego ID");
			return new ModelAndView("page_product");
		}
	}

	@PostMapping(value = "/report", params = {"productId"})
	@ResponseBody
	public String createReport(@RequestParam(value = "productId") long productId, @RequestParam(value= "copyLast") boolean copyLast) {
		System.out.println(copyLast);
		ReportEntity newReport = new ReportEntity(reportsService.getNewRevision(productId), 1682, "", new ArrayList<>(), "", "");
		newReport.setProduct(productsService.getProductById(productId));
		reportsService.save(newReport,copyLast, productId);
		return "/products?productId="+productId+"&reportId="+ newReport.getId();
	}


	@GetMapping(value="/report", params= {"reportId"})
	public String viewReport(@RequestParam(value = "reportId") long reportId, Model model) {
		ReportEntity report = reportsService.findOne(reportId);
		model.addAttribute("summary", report.getSummary());
		model.addAttribute("conclusion", report.getConclusion());
		model.addAttribute("reportId", reportId);
		model.addAttribute("report", report);
		return "summary :: summaryFragment";
	}

	@PostMapping(value="/report", params= {"reportId"})
	public ModelAndView saveReport(@RequestParam(value = "reportId") long reportId, @RequestParam(value = "summary") String summary, @RequestParam(value = "conclusion") String conclusion, @RequestParam(value = "attachment[]", required = false) MultipartFile[] attachments,@RequestParam(value = "attachmentsToDelete[]", required = false) String[] attachmentsToDelete) {
		ReportEntity reportEntity = reportsService.findOne(reportId);
		reportEntity.setSummary(summary);
		reportEntity.setConclusion(conclusion);
		reportsService.save(reportEntity,attachments,attachmentsToDelete);
		return new ModelAndView("redirect:/report?reportId=" + reportId);
	}

	@PostMapping(value = "generateExcel",params = "reportId")
	public void getFile(
			@RequestParam("reportId") long reportId,
			HttpServletResponse response) {
		String fileName = reportsService.getReportNameZip(reportId);
		//String fileName = reportsService.getReportName(reportId);
		try {
			// get your file as InputStream
			InputStream is = new FileInputStream(reportsService.generateExcel(reportId));
			// copy it to response's OutputStream
			//response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
			response.setContentType("application/zip");
			response.setHeader("Content-Disposition", "attachment; filename="+fileName);
			org.apache.poi.util.IOUtils.copy(is, response.getOutputStream());
			response.flushBuffer();
		} catch (IOException ex) {
			//log.info("Error writing file to output stream. Filename was '{}'", fileName, ex);
			throw new RuntimeException("IOError writing file to output stream");
		}
	}

}
