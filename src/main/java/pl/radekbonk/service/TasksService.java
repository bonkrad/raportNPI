package pl.radekbonk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.radekbonk.entity.TaskEntity;
import pl.radekbonk.repository.TaskRepository;

import java.util.Calendar;

@Service
public class TasksService {
	
	@Autowired
	private TaskRepository taskRepository;

	@Autowired
	private ReportsService reportsService;

	@Transactional
	public void deleteById(long id) {
		taskRepository.deleteById(id);
	}
	
	public Iterable<TaskEntity> findByReportId(long id) {
		return taskRepository.findByReportIdOrderByRequiredDateAsc(id);
	}
	
	public TaskEntity getTaskById(long id) {
		return this.taskRepository.findOne(id);
	}
	
	public void save(TaskEntity taskEntity, long reportId, String date) {
		if (taskEntity.getClosed()) {
			taskEntity.setClosedDate(Calendar.getInstance());
		}
		if(date.equals("")) {
		} else {
			taskEntity.setRequiredDate(setDate(date));
		}
		taskEntity.setTimestamp();
		taskEntity.setReport(reportsService.findOne(reportId));
		taskRepository.save(taskEntity);
	}

	
	private Calendar setDate(String date) {
		String[] parts = date.split("-");
		Calendar myCal = Calendar.getInstance();
		myCal.set(Calendar.YEAR, Integer.valueOf(parts[0]));
		myCal.set(Calendar.MONTH, month(parts[1]));
		myCal.set(Calendar.DAY_OF_MONTH, Integer.valueOf(parts[2]));
		return myCal;
	}
	
	private Integer month(String month) {
		switch (Integer.valueOf(month)) {
			case 1:
				return Calendar.JANUARY;
			case 2:
				return Calendar.FEBRUARY;
			case 3:
				return Calendar.MARCH;
			case 4:
				return Calendar.APRIL;
			case 5:
				return Calendar.MAY;
			case 6:
				return Calendar.JUNE;
			case 7:
				return Calendar.JULY;
			case 8:
				return Calendar.AUGUST;
			case 9:
				return Calendar.SEPTEMBER;
			case 10:
				return Calendar.OCTOBER;
			case 11:
				return Calendar.NOVEMBER;
			case 12:
				return Calendar.DECEMBER;
			default:
				return 13;
		}
	}
}
