package pl.radekbonk.service;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.radekbonk.Main;
import pl.radekbonk.entity.ProblemEntity;
import pl.radekbonk.entity.ReportEntity;
import pl.radekbonk.repository.ReportRepository;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ReportsService {

	@Autowired
	private ReportRepository reportRepository;

	@Autowired
	private ProblemsService problemsService;

	@Autowired
	private ProductsService productsService;

	@Autowired
	private HttpServletRequest request;

	private List<String> srcFiles = new ArrayList<>();
	private int i;

	public Iterable<ReportEntity> getAllReports() {
		return reportRepository.findAll();
	}

	public Iterable<ReportEntity> findByProductId(long productId) {
		return reportRepository.findByProductId(productId);
	}

	public Iterable<ReportEntity> findByProductIdOrderByRevisionDesc(long productId) {
		return reportRepository.findByProductIdOrderByRevisionDesc(productId);
	}

	public Iterable<ReportEntity> findByProductIdOrderByRevisionAsc(long productId) {
		return reportRepository.findByProductIdOrderByRevisionAsc(productId);
	}

	public ReportEntity findOne(long id) {
		return reportRepository.findOne(id);
	}

	public ReportEntity findHighestRevision(long id) {
		return reportRepository.findFirstByProductIdOrderByRevisionDesc(id);
	}

	public void save(ReportEntity reportEntity, MultipartFile[] attachment, String[] attachmentsToDelete) {
		reportEntity = removeAttachement(reportEntity, attachmentsToDelete);
		reportEntity = reportWithAttachment(reportEntity, attachment);
		reportRepository.save(reportEntity);
	}

	public void save(ReportEntity reportEntity, boolean copyLast, long productId) {
		ReportEntity oldReport = findHighestRevision(productId);
		if (copyLast) {
			//reportEntity.setAttachmentSrc(oldReport.getAttachmentSrc());
			reportEntity.setConclusion(oldReport.getConclusion());
			reportEntity.setSummary(oldReport.getSummary());
		}
		reportRepository.save(reportEntity);
		if (copyLast) {
			copyProblemsFormLastReports(productId, oldReport.getId(), reportEntity.getId());
		}
	}

	public ReportEntity removeAttachement(ReportEntity reportEntity, String[] attachmentToDelete) {
		List<String> attSrc = reportEntity.getAttachmentSrc();
		for (String attUrl : attachmentToDelete) {
			if (attSrc.contains(attUrl)) {
				attSrc.remove(attUrl);
			} else {
				System.out.println("Problem doesn't contain this attachement");
			}
		}
		return reportEntity;
	}

	public ReportEntity reportWithAttachment(ReportEntity reportEntity, MultipartFile[] attachments) {
		long productId = reportEntity.getProduct().getId();
		List<String> attachmentSources = reportEntity.getAttachmentSrc();
		if (attachments.length > 0) {
			for (MultipartFile attachment : attachments) {
				try {
					String realPathToUploads = Main.getUploadPath() + productId + "/";
					System.out.println(realPathToUploads);
					if (!new File(realPathToUploads).exists()) {
						new File(realPathToUploads).mkdir();
					}
					UUID uuid = UUID.randomUUID();
					String filePath = realPathToUploads + "/" + uuid + attachment.getOriginalFilename().replace(" ", "");
					File dest = new File(filePath);
					//image.transferTo(dest);
					attachment.transferTo(dest);
					attachmentSources.add("/disk/" + productId + "/" + uuid + attachment.getOriginalFilename().replace(" ", ""));

				} catch (Exception e) {
					e.printStackTrace();
				}
				reportEntity.setAttachmentSrc(attachmentSources);
			}
		}
		return reportEntity;
	}

	public BigDecimal getNewRevision(long productId) {
		ReportEntity report = findHighestRevision(productId);
		if (report == null) {
			return new BigDecimal("1.00");
		} else {
			return report.getRevision().add(new BigDecimal("0.10"));
		}
	}

	private void copyProblemsFormLastReports(long productId, long oldReportId, long newReportId) {
		Iterable<ProblemEntity> problemsToCopy = problemsService.findByReportId(oldReportId);
		for (ProblemEntity problem : problemsToCopy) {
			ProblemEntity copiedProblem = new ProblemEntity(problem.getDescription(), problem.getRecommendation(), problem.getImgSrc(), problem.getAttachmentSrc(), problem.getCategory(), problem.getAnswer(), problem.getAuthor(), problem.getPriority(), problem.getClosed());
			copiedProblem.setReport(problem.getReport());
			problemsService.save(copiedProblem, newReportId);
		}
	}

	protected void addImageInCell(Sheet sheet, URL url, Drawing<?> drawing, int colNumber, int rowNumber) {
		try {
			BufferedImage imageIO = ImageIO.read(url);
			int height = imageIO.getHeight();
			int width = imageIO.getWidth();

			new AddDimensionedImage().addImageToSheet(colNumber, rowNumber, sheet, drawing, url, 7, 5,
					AddDimensionedImage.OVERLAY_ROW_AND_COLUMN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void addImageInCell(Sheet sheet, URL url, Drawing<?> drawing, int colNumber, int rowNumber, int height, int width) {
		try {
			BufferedImage imageIO = ImageIO.read(url);

			new AddDimensionedImage().addImageToSheet(colNumber, rowNumber, sheet, drawing, url, width, height,
					AddDimensionedImage.OVERLAY_ROW_AND_COLUMN);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}


	public String generateExcel(long reportId) throws IOException {
		i = 0;
		srcFiles.clear();
		long productId = findOne(reportId).getProduct().getId();
		String realPathToUploads = Main.getUploadPath() + productId + "/";
		if (!new File(realPathToUploads).exists()) {
			new File(realPathToUploads).mkdir();
		}

		FileOutputStream fos = new FileOutputStream(realPathToUploads + getReportNameZip(reportId));
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		ReportEntity report = findOne(reportId);

		String productName = productsService.getProductById(productId).getName();

		String fileName = getReportName(reportId);

		Workbook workbook = new XSSFWorkbook();

		Sheet sheet = workbook.createSheet("Raport NPI");

		CellStyle boldText = workbook.createCellStyle();
		Font boldFont = workbook.createFont();
		boldFont.setBold(true);
		boldText.setFont(boldFont);

		CellStyle boldBigText = workbook.createCellStyle();
		Font boldBigFont = workbook.createFont();
		boldBigFont.setBold(true);
		boldBigFont.setFontHeightInPoints((short) 14);
		boldBigText.setFont(boldBigFont);

		CellStyle headerStyle = workbook.createCellStyle();
		Font headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 18);
		headerStyle.setFont(headerFont);

		int rowNum = 3;

		Row row = sheet.createRow((short) rowNum); //3

		Cell cell = row.createCell(1);
		cell.setCellValue("Raport NPI");
		cell.setCellStyle(headerStyle);

		rowNum += 5; //8
		row = sheet.createRow((short) rowNum); //8

		cell = row.createCell(1);
		cell.setCellValue("Nazwa modelu");
		cell.setCellStyle(boldText);
		CellRangeAddress cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum() + 1, 1, 2);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);
		row.createCell(5).setCellValue(String.valueOf(productId) + " " + productName);
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum() + 1, 5, 7);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);

		rowNum += 2; //10
		row = sheet.createRow((short) rowNum);

		cell = row.createCell(1);
		cell.setCellValue("Inżynier procesu");
		cell.setCellStyle(boldText);
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum() + 1, 1, 2);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);
		row.createCell(5).setCellValue("ENG");
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum() + 1, 5, 7);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);

		rowNum += 2; //12
		row = sheet.createRow((short) rowNum);

		cell = row.createCell(1);
		cell.setCellValue("Inżynier produktu");
		cell.setCellStyle(boldText);
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum() + 1, 1, 2);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);
		row.createCell(5).setCellValue("PE");
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum() + 1, 5, 7);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);

		rowNum += 2; //14
		row = sheet.createRow((short) rowNum);

		cell = row.createCell(1);
		cell.setCellValue("Inżynier jakości");
		cell.setCellStyle(boldText);
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum() + 1, 1, 2);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);
		row.createCell(5).setCellValue("PQA");
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum() + 1, 5, 7);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);

		rowNum += 3; //17
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(1);
		cell.setCellValue("Historia rewizji");
		cell.setCellStyle(boldText);
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 7);
		sheet.addMergedRegion(cellRangeAddress);
		setBordersMedium(cellRangeAddress, sheet);

		CellStyle borderThin = workbook.createCellStyle();
		borderThin.setBorderBottom(BorderStyle.THIN);
		borderThin.setBorderTop(BorderStyle.THIN);
		borderThin.setBorderLeft(BorderStyle.THIN);
		borderThin.setBorderRight(BorderStyle.THIN);

		CellStyle borderThinRed = workbook.createCellStyle();
		borderThinRed.setBorderBottom(BorderStyle.THIN);
		borderThinRed.setBorderTop(BorderStyle.THIN);
		borderThinRed.setBorderLeft(BorderStyle.THIN);
		borderThinRed.setBorderRight(BorderStyle.THIN);
		Font font = workbook.createFont();
		font.setColor(Font.COLOR_RED);
		borderThinRed.setFont(font);
		borderThinRed.setWrapText(true);


		CellStyle wrapText = workbook.createCellStyle();
		wrapText.setBorderRight(BorderStyle.THIN);
		wrapText.setBorderLeft(BorderStyle.THIN);
		wrapText.setBorderTop(BorderStyle.THIN);
		wrapText.setBorderBottom(BorderStyle.THIN);
		wrapText.setWrapText(true);

		rowNum += 1; //19
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(1);
		cell.setCellValue("Rewizja");
		cell.setCellStyle(borderThin);
		row.createCell(2).setCellValue("Zmiana");
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 2, 5);
		sheet.addMergedRegion(cellRangeAddress);
		setBorderThin(cellRangeAddress, sheet);
		row.createCell(6).setCellValue("Data");
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 6, 7);
		sheet.addMergedRegion(cellRangeAddress);
		setBorderThin(cellRangeAddress, sheet);


		short rowNumRevs = (short) (rowNum + 1);
		Iterable<ReportEntity> reports = findByProductIdOrderByRevisionAsc(productId);
		for (ReportEntity reportEntity : reports) {
			row = sheet.createRow(rowNumRevs);
			cell = row.createCell(1);
			cell.setCellValue(String.valueOf(reportEntity.getRevision()));
			cell.setCellStyle(borderThin);

			row.createCell(2).setCellValue("");
			cellRangeAddress = new CellRangeAddress(rowNumRevs, rowNumRevs, 2, 5);
			sheet.addMergedRegion(cellRangeAddress);
			setBorderThin(cellRangeAddress, sheet);

			row.createCell(6).setCellValue(reportEntity.getTimestamp().getTime().toString());
			cellRangeAddress = new CellRangeAddress(rowNumRevs, rowNumRevs, 6, 7);
			sheet.addMergedRegion(cellRangeAddress);
			setBorderThin(cellRangeAddress, sheet);
			rowNumRevs++;
		}

		rowNum += 14; //33
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(0);
		cell.setCellValue("1.Wstęp");
		cell.setCellStyle(boldBigText);

		rowNum += 1; //34
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(0);
		cell.setCellValue("Krótki opis produktu oraz przebiegu wdrożenia – wyszczególnienie procesów, jakie miały miejsce, każdy proces po 2-3 zdania opisu");

		rowNum += 2; //36
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(0);
		cell.setCellValue("2.Znalezione problemy, wątpliwości");
		cell.setCellStyle(boldBigText);

		rowNum += 2; //38
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(0);
		cell.setCellValue("Krytyczne - wymagane zamknięcie przed kolejną produkcją");
		cell.setCellStyle(boldText);
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 8);
		sheet.addMergedRegion(cellRangeAddress);
		setBorderThin(cellRangeAddress, sheet);

		Iterable<ProblemEntity> majorProblems = problemsService.findMajorByReportId(reportId);
		rowNum = createProblemsTable(majorProblems, workbook, sheet, rowNum, borderThin, productId, borderThinRed, wrapText);

		rowNum++;
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(0);
		cell.setCellValue("Pozostałe");
		cell.setCellStyle(boldText);
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 8);
		sheet.addMergedRegion(cellRangeAddress);
		setBorderThin(cellRangeAddress, sheet);

		Iterable<ProblemEntity> minorProblems = problemsService.findMinorByReportId(reportId);
		rowNum = createProblemsTable(minorProblems, workbook, sheet, rowNum, borderThin, productId, borderThinRed, wrapText);

		rowNum += 2;
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(0);
		cell.setCellValue("3.Podsumowanie ekonomiczne");
		cell.setCellStyle(boldBigText);
		rowNum++;
		row = sheet.createRow((short) rowNum);
		row.createCell(0).setCellValue(report.getSummary());
		rowNum += 2;
		row = sheet.createRow((short) rowNum);
		row.createCell(0).setCellValue("Załączniki");
		row.getCell(0).setCellStyle(boldText);
		rowNum++;
		int k = 1;
		for (String reportAttachmentSrc : report.getAttachmentSrc()) {
			row = sheet.createRow((short) rowNum);
			cell = row.createCell(0);
			cell.setCellValue("Attachment " + k);
			CreationHelper helper = workbook.getCreationHelper();
			Hyperlink link = helper.createHyperlink(HyperlinkType.FILE);
			link.setAddress(reportAttachmentSrc.replace("/disk/" + productId + "/", ""));
			cell.setHyperlink(link);
			k++;
			rowNum++;
		}


		rowNum += 2;
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(0);
		cell.setCellValue("4.Wnioski");
		cell.setCellStyle(boldBigText);
		rowNum++;
		row = sheet.createRow((short) rowNum);
		row.createCell(0).setCellValue(report.getConclusion());

		workbook.setPrintArea(0,0,9,0,rowNum);

		sheet.getPrintSetup().setPaperSize(XSSFPrintSetup.A4_PAPERSIZE);
		sheet.getPrintSetup().setLandscape(true);
		sheet.setDisplayGridlines(true);

		try (FileOutputStream outputStream = new FileOutputStream(realPathToUploads + fileName)) {
			workbook.write(outputStream);
			workbook.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(realPathToUploads + fileName);
		for (String reportAttachmentSrc : report.getAttachmentSrc()) {
			srcFiles.add(Main.getUploadPath().replace("disk/", "") + reportAttachmentSrc);
		}

		srcFiles.add(realPathToUploads + fileName);
		for (String srcFile : srcFiles) {
			File fileToZip = new File(srcFile);
			FileInputStream fis = new FileInputStream(fileToZip);
			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
			zipOut.putNextEntry(zipEntry);

			byte[] bytes = new byte[1024];
			int length;
			while ((length = fis.read(bytes)) >= 0) {
				zipOut.write(bytes, 0, length);
			}
			fis.close();
		}
		zipOut.close();
		fos.close();

		return realPathToUploads + getReportNameZip(reportId);
	}

	private int createProblemsTable(Iterable<ProblemEntity> problems, Workbook workbook, Sheet sheet, int rowNum, CellStyle borderThin, long productId, CellStyle borderThinRed, CellStyle wrapText) {

		rowNum++;
		Row row = sheet.createRow((short) rowNum);
		Cell cell = row.createCell(0);
		cell.setCellValue("Lp.");
		cell.setCellStyle(borderThin);
		sheet.setColumnWidth(cell.getColumnIndex(), (int) Math.ceil((sheet.getColumnWidth(15)) * 0.6));
		cell = row.createCell(1);
		cell.setCellValue("Priorytet");
		cell.setCellStyle(borderThin);
		cell = row.createCell(2);
		cell.setCellValue("Opis");
		cell.setCellStyle(borderThin);
		sheet.setColumnWidth(cell.getColumnIndex(), (int) Math.ceil((sheet.getColumnWidth(15)) * 3.1));
		cell = row.createCell(3);
		cell.setCellValue("Zagrożenie");
		cell.setCellStyle(borderThin);
		sheet.autoSizeColumn(cell.getColumnIndex());
		cell = row.createCell(4);
		cell.setCellValue("Zdjęcie");
		CellRangeAddress cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 4, 5);
		sheet.addMergedRegion(cellRangeAddress);
		setBorderThin(cellRangeAddress, sheet);
		cell = row.createCell(6);
		cell.setCellValue("Zalecenia/Akcje Assel");
		cell.setCellStyle(borderThin);
		sheet.setColumnWidth(cell.getColumnIndex(), (int) Math.ceil((sheet.getColumnWidth(15)) * 3.1));
		cell = row.createCell(7);
		cell.setCellValue("Status");
		cell.setCellStyle(borderThin);
		cell = row.createCell(8);
		cell.setCellValue("Odpowiedź klienta");
		cell.setCellStyle(borderThin);
		sheet.setColumnWidth(cell.getColumnIndex(), (int) Math.ceil((sheet.getColumnWidth(15)) * 2.2));

		rowNum++;
		//int i = 0;
		int j = 1;
		for (ProblemEntity problem : problems) {
			row = sheet.createRow((short) rowNum++);
			cell = row.createCell(0);
			cell.setCellValue(j++);
			cell.setCellStyle(borderThin);
			cell = row.createCell(1);
			cell.setCellValue(problem.getPriority());
			cell.setCellStyle(borderThin);
			cell = row.createCell(2);
			cell.setCellValue(problem.getDescription());
			cell.setCellStyle(wrapText);
			cell = row.createCell(3);
			cell.setCellValue("");
			int warning = problem.getWarning();
			if (warning != 0) {
				ClientAnchor anchorImg = cell.getSheet().getWorkbook().getCreationHelper().createClientAnchor();
				anchorImg.setCol1(cell.getColumnIndex());
				anchorImg.setRow1(cell.getRowIndex());

				switch (warning) {
					case 1:
						cell.setCellValue("CTQ");
						break;
					case 2:
						cell.setCellValue("OTD");
						break;
					case 3:
						cell.setCellValue("CTQ \nOTD");
						break;
				}
			}
			cell.setCellStyle(borderThinRed);

			cell = row.createCell(4);
			if (!problem.getImgSrc().isEmpty()) {
				i++;
				cell.setCellValue("fig" + i);
				String pathToImages = Main.getUploadPath().replace("/disk/", "/");
				Sheet imageSheet = workbook.createSheet("fig" + i);
				int totalHeight = 0;
				for (String imgSrc : problem.getImgSrc()) {
					try (InputStream is = new FileInputStream(pathToImages + imgSrc)) {
						BufferedImage bufferedImage = ImageIO.read(new FileInputStream(pathToImages + imgSrc));
						byte[] bytes = IOUtils.toByteArray(is);
						int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
						Drawing drawing = imageSheet.createDrawingPatriarch();
						CreationHelper helper = workbook.getCreationHelper();
						ClientAnchor anchor = helper.createClientAnchor();
						anchor.setCol1(0);
						int currentRow = (int) Math.ceil(totalHeight / 20);
						anchor.setRow1(currentRow);
						Picture pict = drawing.createPicture(anchor, pictureIdx);
						totalHeight = totalHeight + bufferedImage.getHeight();
						pict.resize();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				CreationHelper helper = workbook.getCreationHelper();
				Hyperlink link = helper.createHyperlink(HyperlinkType.DOCUMENT);
				link.setAddress("'fig" + i + "'!A1");
				cell.setHyperlink(link);
			}
			cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 4, 5);
			sheet.addMergedRegion(cellRangeAddress);
			setBorderThin(cellRangeAddress, sheet);
			cell = row.createCell(6);
			cell.setCellValue(problem.getRecommendation());
			cell.setCellStyle(wrapText);
			cell = row.createCell(7);
			cell.setCellValue(problem.getStatus());
			cell.setCellStyle(wrapText);
			cell = row.createCell(8);
			cell.setCellValue(problem.getAnswer());
			cell.setCellStyle(wrapText);
			if (problem.getAttachmentSrc() != ("")) {
				cell = row.createCell(9);
				cell.setCellValue("att.");
				cell.setCellStyle(wrapText);
				CreationHelper helper = workbook.getCreationHelper();
				Hyperlink link = helper.createHyperlink(HyperlinkType.FILE);
				link.setAddress(problem.getAttachmentSrc().replace("/disk/" + productId + "/", ""));
				cell.setHyperlink(link);
				srcFiles.add(Main.getUploadPath().replace("disk/", "") + problem.getAttachmentSrc());
			}
		}
		return rowNum;
	}


	private String getReportName(long reportId) {
		ReportEntity report = findOne(reportId);
		long productId = report.getProduct().getId();
		BigDecimal reportRev = report.getRevision();
		//String productName = productsService.getProductById(productId).getName();

		return productId + "-rev." + reportRev + ".xlsx";
	}

	public String getReportNameZip(long reportId) {
		ReportEntity reportEntity = findOne(reportId);
		long productId = reportEntity.getProduct().getId();
		BigDecimal reportRev = reportEntity.getRevision();

		return productId + "-rev." + reportRev + ".zip";
	}

	private void setBordersMedium(CellRangeAddress cellRangeAddress, Sheet sheet) {
		setBorder(cellRangeAddress, sheet, BorderStyle.MEDIUM);
	}

	private void setBorderThin(CellRangeAddress cellRangeAddress, Sheet sheet) {
		setBorder(cellRangeAddress, sheet, BorderStyle.THIN);
	}

	private void setBorder(CellRangeAddress cellRangeAddress, Sheet sheet, BorderStyle borderStyle) {
		RegionUtil.setBorderBottom(borderStyle, cellRangeAddress, sheet);
		RegionUtil.setBorderTop(borderStyle, cellRangeAddress, sheet);
		RegionUtil.setBorderLeft(borderStyle, cellRangeAddress, sheet);
		RegionUtil.setBorderRight(borderStyle, cellRangeAddress, sheet);
	}

}
