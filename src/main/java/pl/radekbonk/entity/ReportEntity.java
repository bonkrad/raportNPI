package pl.radekbonk.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

@Entity
public class ReportEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	private BigDecimal revision;

	@Temporal(TemporalType.TIMESTAMP)
	private Calendar timestamp;

	private String author;

	private String imgSrc;

	@ElementCollection
	private List<String> attachmentSrc;

	private String summary;
	private String conclusion;
	private String productEngineer;
	private String processEngineer;
	private String qualityEngineer;

	/*@ManyToOne
	private ProductEntity product;*/

	private long productId;

	public ReportEntity() {

	}

	public ReportEntity(BigDecimal revision, String author, String imgSrc, List<String> attachmentSrc, String summary, String conclusion, long productId, String productEngineer, String processEngineer, String qualityEngineer) {
		this.revision = revision;
		this.author = author;
		this.imgSrc = imgSrc;
		this.attachmentSrc = attachmentSrc;
		this.summary = summary;
		this.conclusion = conclusion;
		this.timestamp = Calendar.getInstance();
		this.productId = productId;
		this.productEngineer = productEngineer;
		this.processEngineer = processEngineer;
		this.qualityEngineer = qualityEngineer;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public BigDecimal getRevision() {
		return revision;
	}

	public void setRevision(BigDecimal revision) {
		this.revision = revision;
	}

	public Calendar getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Calendar timestamp) {
		this.timestamp = timestamp;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getImgSrc() {
		return imgSrc;
	}

	public void setImgSrc(String imgSrc) {
		this.imgSrc = imgSrc;
	}

	public List<String> getAttachmentSrc() {
		return attachmentSrc;
	}

	public void setAttachmentSrc(List<String> attachmentSrc) {
		this.attachmentSrc = attachmentSrc;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getConclusion() {
		return conclusion;
	}

	public void setConclusion(String conclusion) {
		this.conclusion = conclusion;
	}

	public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	public String getProductEngineer() {
		return productEngineer;
	}

	public void setProductEngineer(String productEngineer) {
		this.productEngineer = productEngineer;
	}

	public String getProcessEngineer() {
		return processEngineer;
	}

	public void setProcessEngineer(String processEngineer) {
		this.processEngineer = processEngineer;
	}

	public String getQualityEngineer() {
		return qualityEngineer;
	}

	public void setQualityEngineer(String qualityEngineer) {
		this.qualityEngineer = qualityEngineer;
	}
}
