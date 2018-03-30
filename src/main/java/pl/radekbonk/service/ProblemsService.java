package pl.radekbonk.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import pl.radekbonk.Main;
import pl.radekbonk.entity.ProblemEntity;
import pl.radekbonk.repository.ProblemRepository;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ProblemsService {

	//TODO
	public final static String FILE_UPLOAD_PATH = "H://disk/";

	//TODO
//	public final static String FILE_UPLOAD_PATH="/home/pi/raport/disk/";

	@Autowired
	private ProblemRepository problemRepository;

	@Autowired
	private HttpServletRequest request;

	@Autowired
	private ReportsService reportsService;

	public Iterable<ProblemEntity> getAllProblems() {
		return problemRepository.findAll();
	}

	public Iterable<ProblemEntity> findByReportId(long id) {
		return problemRepository.findByReportId(id);
	}

	public Iterable<ProblemEntity> findMajorByReportId(long id) {
		return problemRepository.findByReportIdAndPriorityGreaterThan(id, 3);
	}

	public Iterable<ProblemEntity> findMinorByReportId(long id) {
		return problemRepository.findByReportIdAndPriorityLessThan(id, 4);
	}

	@Transactional
	public void deleteById(long id) {
		problemRepository.deleteById(id);
	}

	public ProblemEntity getProblemById(long id) {
		return this.problemRepository.findOne(id);
	}

	public void save(ProblemEntity problemEntity, long reportId) {
		problemEntity.setReport(reportsService.findOne(reportId));
		problemEntity.setTimestamp();
		problemRepository.save(problemEntity);
	}

	public void save(ProblemEntity problemEntity, long reportId, MultipartFile[] images) {
		problemEntity = problemWithImg(problemEntity, images, reportId);
		problemEntity.setReport(reportsService.findOne(reportId));
		problemEntity.setTimestamp();
		problemRepository.save(problemEntity);
	}

	public void save(ProblemEntity problemEntity, MultipartFile attachment, long reportId) {
		problemEntity = problemWithAttachment(problemEntity, attachment, reportId);
		problemEntity.setReport(reportsService.findOne(reportId));
		problemEntity.setTimestamp();
		problemRepository.save(problemEntity);
	}

	public void save(ProblemEntity problemEntity, long reportId, MultipartFile[] images, MultipartFile attachment) {
		problemEntity = problemWithImg(problemEntity, images, reportId);
		problemEntity = problemWithAttachment(problemEntity, attachment, reportId);
		problemEntity.setReport(reportsService.findOne(reportId));
		problemEntity.setTimestamp();
		problemRepository.save(problemEntity);
	}

	public ProblemEntity removeImages(ProblemEntity problemEntity, String[] imagesToDelete) {
		List<String> imagesSrc = problemEntity.getImgSrc();
		for (String imgUrl : imagesToDelete) {
			if (imagesSrc.contains(imgUrl)) {
				imagesSrc.remove(imgUrl);
			} else {
				System.out.println("Problem doesn't contain this image");
			}
		}
		return problemEntity;
	}

	public void update(ProblemEntity problemEntity) {
		//problemRepository.
	}

	private BufferedImage resizedImage(MultipartFile image, String filePath) {
		File convFile = new File(filePath);
		try {
			image.transferTo(convFile);
			BufferedImage originalImage = ImageIO.read(convFile);
			int type = BufferedImage.TYPE_INT_RGB;
			float ratio = ((float) originalImage.getHeight() / originalImage.getWidth());
			final int IMG_WIDTH = 800;
			final int IMG_HEIGHT = Math.round(IMG_WIDTH * ratio);
			BufferedImage resizedImage = new BufferedImage(IMG_WIDTH, IMG_HEIGHT, type);
			Graphics2D g = resizedImage.createGraphics();
			g.drawImage(originalImage, 0, 0, IMG_WIDTH, IMG_HEIGHT, null);
			g.dispose();
			return resizedImage;
		} catch (IOException e) {
			System.out.println(e);
			return null;
		}
	}

	private ProblemEntity problemWithImg(ProblemEntity problemEntity, MultipartFile[] images, long reportId) {
		long productId = reportsService.findOne(reportId).getProduct().getId();
		List<String> imgSources = problemEntity.getImgSrc();
		if (images.length > 0) {
			for (MultipartFile image : images) {
				try {
					String realPathToUploads = Main.getUploadPath() + productId + "/";
					System.out.println(realPathToUploads);
					if (!new File(realPathToUploads).exists()) {
						new File(realPathToUploads).mkdir();
					}
					UUID uuid = UUID.randomUUID();
					String filePath = realPathToUploads + "/" + uuid + image.getOriginalFilename().replace(" ", "");
					File dest = new File(filePath);
					//image.transferTo(dest);
					ImageIO.write(resizedImage(image, filePath), "jpg", dest);
					imgSources.add("/disk/" + productId + "/" + uuid + image.getOriginalFilename().replace(" ", ""));

				} catch (Exception e) {
					System.out.println(e);
				}
				problemEntity.setImgSrc(imgSources);
			}
		} else {
			System.out.println("Problem without image");
		}
		return problemEntity;
	}

	private ProblemEntity problemWithAttachment(ProblemEntity problemEntity, MultipartFile attachment, long reportId) {
		long productId = reportsService.findOne(reportId).getProduct().getId();
		if (!attachment.isEmpty()) {
			try {
				String realPathToUploads = Main.getUploadPath() + productId + "/";
				System.out.println(realPathToUploads);
				if (!new File(realPathToUploads).exists()) {
					new File(realPathToUploads).mkdir();
				}
				UUID uuid = UUID.randomUUID();
				String filePath = realPathToUploads + "/" + uuid + attachment.getOriginalFilename().replace(" ", "");
				File dest = new File(filePath);
				attachment.transferTo(dest);
				problemEntity.setAttachmentSrc("/disk/" + productId + "/" + uuid + attachment.getOriginalFilename().replace(" ", ""));
			} catch (Exception e) {
				System.out.println(e);
			}
		} else {
			System.out.println("Problem without attachement");
		}
		return problemEntity;
	}
}
