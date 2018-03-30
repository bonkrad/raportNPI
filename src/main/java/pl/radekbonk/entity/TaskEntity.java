package pl.radekbonk.entity;

import javax.persistence.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@Entity
public class TaskEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	
	@Lob
	private String description;
	@Lob
	private String comment;
	
	private int responsibleWorker;
	
	@Temporal(TemporalType.DATE)
	private Calendar requiredDate;
	@Temporal(TemporalType.DATE)
	private Calendar closedDate;
	@Temporal(TemporalType.TIMESTAMP)
	private Calendar timestamp;
	
	private int author;
	
	private Boolean closed;
	
	@ManyToOne
	private ReportEntity report;
	
	public TaskEntity() {
	
	}

	
	public TaskEntity(String description, String comment, int responsibleWorker, Calendar requiredDate, Calendar closedDate, int author, Boolean closed) {
		this.description = description;
		this.comment = comment;
		this.responsibleWorker = responsibleWorker;
		this.requiredDate = requiredDate;
		this.closedDate = closedDate;
		this.timestamp = Calendar.getInstance();
		this.author = author;
		this.closed = closed;
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public int getResponsibleWorker() {
		return responsibleWorker;
	}
	
	public void setResponsibleWorker(int responsibleWorker) {
		this.responsibleWorker = responsibleWorker;
	}
	
	public Calendar getRequiredDate() {
		return requiredDate;
	}
	
	public void setRequiredDate(Calendar requiredDate) {
		this.requiredDate = requiredDate;
	}
	
	public Calendar getClosedDate() {
		return closedDate;
	}
	
	public void setClosedDate(Calendar closedDate) {
		this.closedDate = closedDate;
	}
	
	public Calendar getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp() {
		this.timestamp = Calendar.getInstance();
	}
	
	public int getAuthor() {
		return author;
	}
	
	public void setAuthor(int author) {
		this.author = author;
	}
	
	public Boolean getClosed() {
		return closed;
	}
	
	public void setClosed(Boolean closed) {
		this.closed = closed;
	}
	
	public String getStatus() {
		if (this.closed) {
			return "closed";
		} else {
			return "open";
		}
	}
	
	public void setStatus(String status) {
	}
	
	public String getDate() {
		if (this.requiredDate == null) {
			return "2099-12-31";
		} else {
			Date date = this.requiredDate.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("YYY-MM-dd");
			return sdf.format(date);
		}
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public ReportEntity getReport() {
		return report;
	}

	public void setReport(ReportEntity report) {
		this.report = report;
	}
}
