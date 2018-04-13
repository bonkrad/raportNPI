package pl.radekbonk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import pl.radekbonk.entity.ReportEntity;
import pl.radekbonk.entity.TaskEntity;

@Component
public class EmailService {

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private ReportsService reportsService;

	public void sendMessage(TaskEntity task) {
		ReportEntity report = reportsService.findOne(task.getReport().getId());

		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("powiadomienie@radekbonk.pl");
		message.setTo("bonk.r@assel.pl");
		message.setSubject("NPI - Zadanie do wykonania w produkcie: " + report.getProductId());
		message.setText("Zadanie: " + task.getDescription() +
				"\n" + "Wymagana data: " + task.getRequiredDate().getTime());
		emailSender.send(message);
	}
}
