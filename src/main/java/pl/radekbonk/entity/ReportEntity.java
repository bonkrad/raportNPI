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

	private int author;

	private String imgSrc;

	@ElementCollection
	private List<String> attachmentSrc;

	private String summary;
	private String conclusion;

	@ManyToOne
	private ProductEntity product;

	public ReportEntity() {

	}

	public ReportEntity(BigDecimal revision, int author, String imgSrc, List<String> attachmentSrc, String summary, String conclusion) {
		this.revision = revision;
		this.author = author;
		this.imgSrc = imgSrc;
		this.attachmentSrc = attachmentSrc;
		this.summary = summary;
		this.conclusion = conclusion;
		this.timestamp = Calendar.getInstance();
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

	public int getAuthor() {
		return author;
	}

	public void setAuthor(int author) {
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

	public ProductEntity getProduct() {
		return product;
	}

	public void setProduct(ProductEntity product) {
		this.product = product;
	}
}
