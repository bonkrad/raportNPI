package pl.radekbonk.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import pl.radekbonk.entity.TaskEntity;
import pl.radekbonk.service.EmailService;
import pl.radekbonk.service.TasksService;

import java.util.regex.Pattern;

@Controller
@RequestMapping("/")
public class TaskController {

	@Autowired
	private TasksService tasksService;

	@Autowired
	private EmailService emailService;

	@PostMapping(value = "/task", params = {"reportId"})
	public ModelAndView insertTask(@RequestParam(value = "reportId") long reportId, Model model, TaskEntity taskEntity, @RequestParam("rDate") String date, Authentication authentication) {
		taskEntity.setAuthor(authentication.getPrincipal().toString().replace("SBS\\",""));
		tasksService.save(taskEntity, reportId, date);
		return new ModelAndView("redirect:/tasks?reportId=" + reportId);
	}

	@PostMapping(value = "/updateTask", params = {"taskId"})
	public ModelAndView updateTask(@RequestParam(value = "taskId") long taskId, Model model, TaskEntity taskEntity, @RequestParam("rDate") String date, Authentication authentication) {
		long reportId = tasksService.getTaskById(taskId).getReport().getId();
		taskEntity.setId(taskId);
		taskEntity.setAuthor(authentication.getPrincipal().toString().split(Pattern.quote("\\"))[1]);
		tasksService.save(taskEntity, reportId, date);
		return new ModelAndView("redirect:/tasks?reportId=" + reportId);
	}

	@GetMapping(value = "/tasks", params = {"reportId"})
	public String getTasks(@RequestParam(value = "reportId") long reportId, Model model) {
		model.addAttribute("tasks", tasksService.findByReportId(reportId));
		return "task_row :: tasksList";
	}

	@PostMapping(value = "/tasks", params = {"taskId"})
	public ModelAndView deleteTask(@RequestParam(value = "taskId") long taskId, Model model) {
		long reportId = tasksService.getTaskById(taskId).getReport().getId();
		tasksService.deleteById(taskId);
		return new ModelAndView("redirect:/tasks?reportId=" + reportId);
	}

	@PostMapping(value = "/taskReminder", params = {"taskId"})
	@ResponseBody
	public String sendReminder(@RequestParam(value = "taskId") long taskId) {
		TaskEntity task = tasksService.getTaskById(taskId);
		emailService.sendMessage(task);
		return "sucess";
	}
}
