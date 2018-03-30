package pl.radekbonk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import pl.radekbonk.entity.ProblemEntity;
import pl.radekbonk.entity.ReportEntity;
import pl.radekbonk.entity.TaskEntity;
import pl.radekbonk.service.ProblemsService;
import pl.radekbonk.service.ProductsService;
import pl.radekbonk.service.ReportsService;

@Controller
@RequestMapping("/")
public class ProblemController {

	@Autowired
	private ProductsService productsService;
	@Autowired
	private ReportsService reportsService;
	@Autowired
	private ProblemsService problemsService;


	@GetMapping(value = "/products", params = {"reportId", "productId"})
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
			return new ModelAndView("problems");
		} catch (NullPointerException e) {
			System.out.println("No such a report");
			model.addAttribute("alert", "Wybierz poprawny raport lub utwórz nowy");
			model.addAttribute("productIdAx", productId);
			model.addAttribute("productName", productsService.getProductById(productId).getName());
			model.addAttribute("reports", reportsService.findByProductIdOrderByRevisionDesc(productId));
			return new ModelAndView("page_revision");
		}
	}


	@GetMapping(value = "/products/problems", params = {"reportId"})
	public String getProblems(@RequestParam(value = "reportId") long reportId, Model model) {
		model.addAttribute("problems", problemsService.findByReportId(reportId));
		model.addAttribute("majorProblems", problemsService.findMajorByReportId(reportId));
		model.addAttribute("minorProblems", problemsService.findMinorByReportId(reportId));
		model.addAttribute("flag", true);
		return "row :: resultsList";
	}


	@PostMapping(value = "/products", params = {"problemId"})
	public ModelAndView deleteProblem(@RequestParam(value = "problemId") long problemId, Model model) {
		long reportId = problemsService.getProblemById(problemId).getReport().getId();
		problemsService.deleteById(problemId);
		return new ModelAndView("redirect:/products/problems?reportId=" + reportId);
	}

	@PostMapping(value = "/update", params = {"problemId"})
	public ModelAndView updateProblem(@RequestParam(value = "problemId") long problemId, Model model, ProblemEntity problemEntity, @RequestParam(value = "images[]", required = false) MultipartFile[] images, @RequestParam(value = "attachment", required = false) MultipartFile attachment, @RequestParam(value = "imagesToDelete[]", required = false) String[] imagesToDelete) {
		for(String url: imagesToDelete) {
			System.out.println(url);
		}
		ProblemEntity oldProblem = problemsService.getProblemById(problemId);
		long reportId = oldProblem.getReport().getId();
		problemEntity.setId(problemId);
		problemEntity.setAttachmentSrc(oldProblem.getAttachmentSrc());
		problemEntity.setImgSrc(oldProblem.getImgSrc());
		if(imagesToDelete.length>0) {
			problemEntity = problemsService.removeImages(problemEntity, imagesToDelete);
		}
		if (images.length == 0 && attachment == null) {
			problemsService.save(problemEntity, reportId);
		} else if (images.length > 0 && attachment == null) {
			problemsService.save(problemEntity, reportId, images);
		} else if (attachment != null && images.length == 0) {
			problemsService.save(problemEntity, attachment, reportId);
		} else {
			problemsService.save(problemEntity, reportId, images, attachment);
		}
		return new ModelAndView("redirect:/products/problems?reportId=" + reportId);
	}

	@PostMapping(value = "/products", params = {"reportId"})
	public ModelAndView insertProblem(@RequestParam(value = "reportId") long reportId, Model model, ProblemEntity problemEntity, @RequestParam(value = "images[]", required = false) MultipartFile[] images, @RequestParam(value = "attachment", required = false) MultipartFile attachment) {
		if (images.length==0  && attachment == null) {
			problemsService.save(problemEntity, reportId);
		} else if (images.length > 0 && attachment == null) {
			problemsService.save(problemEntity, reportId, images);
		} else if (attachment != null && images.length == 0) {
			problemsService.save(problemEntity, attachment, reportId);
		} else {
			problemsService.save(problemEntity, reportId, images, attachment);
		}
		return new ModelAndView("redirect:/products/problems?reportId=" + reportId);
	}

}
