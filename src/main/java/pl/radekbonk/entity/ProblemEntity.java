package pl.radekbonk.entity;

import javax.persistence.*;
import java.util.Calendar;
import java.util.List;

@Entity
public class ProblemEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Lob
	private String description;
	@Lob
	private String recommendation;

	private String attachmentSrc;
	private String category;
	@Lob
	private String answer;

	@Temporal(TemporalType.TIMESTAMP)
	private Calendar timestamp;

	private String author;
	private int priority;
	private int warning;

	private Boolean closed;

	@ElementCollection
	private List<String> imgSrc;

	@ManyToOne
	private ReportEntity report;

	public ProblemEntity() {

	}

	public ProblemEntity(String description, String recommendation, List<String> imgSrc,String attachmentSrc, String category, String answer, String author, int priority, Boolean closed) {
		this.description = description;
		this.recommendation = recommendation;
		this.imgSrc = imgSrc;
		this.attachmentSrc = attachmentSrc;
		this.category = category;
		this.answer = answer;
		this.author = author;
		this.priority = priority;
		this.closed = closed;
		this.timestamp = Calendar.getInstance();
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

	public String getRecommendation() {
		return recommendation;
	}

	public void setRecommendation(String recommendation) {
		this.recommendation = recommendation;
	}

	public List<String> getImgSrc() {
		return imgSrc;
	}

	public void setImgSrc(List<String> imgSrc) {
		this.imgSrc = imgSrc;
	}

	public String getAttachmentSrc() {
		return attachmentSrc;
	}

	public void setAttachmentSrc(String attachmentSrc) {
		this.attachmentSrc = attachmentSrc;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp() {
		this.timestamp = Calendar.getInstance();
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getWarning() {
		return warning;
	}

	public void setWarning(int warning) {
		this.warning = warning;
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

	public ReportEntity getReport() {
		return report;
	}

	public void setReport(ReportEntity report) {
		this.report = report;
	}
}
