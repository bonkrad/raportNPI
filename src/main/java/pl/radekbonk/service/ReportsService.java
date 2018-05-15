package pl.radekbonk.service;

import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.util.IOUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.radekbonk.Main;
import pl.radekbonk.entity.*;
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

	/*@Autowired
	private ProductsService productsService;*/

	@Autowired
	private ProductClient productClient;

	@Autowired
	private HttpServletRequest request;

	private List<String> srcFiles = new ArrayList<>();
	private int i;

	public Iterable<ReportEntity> getAllReports() {
		return reportRepository.findAll();
	}

	public List<ReportEntity> findByProductId(long productId) {
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
		long productId = reportEntity.getProductId();
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
		long productId = findOne(reportId).getProductId();
		String realPathToUploads = Main.getUploadPath() + productId + "/";
		if (!new File(realPathToUploads).exists()) {
			new File(realPathToUploads).mkdir();
		}

		FileOutputStream fos = new FileOutputStream(realPathToUploads + getReportNameZip(reportId));
		ZipOutputStream zipOut = new ZipOutputStream(fos);
		ReportEntity report = findOne(reportId);

		//String productName = productsService.getProductById(productId).getName();
		String productName = productClient.getName(String.valueOf(productId));

		String fileName = getReportNameExcel(reportId);

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

		List<ProblemEntity> majorProblems = problemsService.findMajorByReportId(reportId);
		rowNum = createProblemsTable(majorProblems, workbook, sheet, rowNum, borderThin, productId, borderThinRed, wrapText);

		rowNum++;
		row = sheet.createRow((short) rowNum);
		cell = row.createCell(0);
		cell.setCellValue("Pozostałe");
		cell.setCellStyle(boldText);
		cellRangeAddress = new CellRangeAddress(row.getRowNum(), row.getRowNum(), 0, 8);
		sheet.addMergedRegion(cellRangeAddress);
		setBorderThin(cellRangeAddress, sheet);

		List<ProblemEntity> minorProblems = problemsService.findMinorByReportId(reportId);
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

		workbook.setPrintArea(0, 0, 9, 0, rowNum);

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

	private int createProblemsTable(List<ProblemEntity> problems, Workbook workbook, Sheet sheet, int rowNum, CellStyle borderThin, long productId, CellStyle borderThinRed, CellStyle wrapText) {

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
				if (!problem.getAttachmentSrc().equals("")) {
					srcFiles.add(Main.getUploadPath().replace("disk/", "") + problem.getAttachmentSrc());
				}
			}
		}
		return rowNum;
	}

	public String generateWord(long reportId, String language) throws Exception {
		WordDictionary dictionary;
		if (language.equals("polish")) {
			dictionary = new PolishWordDictionary();
		} else {
			dictionary = new EnglishWordDictionary();
		}
		ClassLoader classLoader = getClass().getClassLoader();
		InputStream inputStream = classLoader.getResourceAsStream("document.docx");
		try (XWPFDocument document = new XWPFDocument(inputStream)) {

			i = 0;
			srcFiles.clear();
			long productId = findOne(reportId).getProductId();
			String realPathToUploads = Main.getUploadPath() + productId + "/";
			if (!new File(realPathToUploads).exists()) {
				new File(realPathToUploads).mkdir();
			}

			FileOutputStream fos = new FileOutputStream(realPathToUploads + getReportNameZip(reportId));
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			ReportEntity report = findOne(reportId);

			String productName = productClient.getName(String.valueOf(productId));

			String fileName = getReportNameWord(reportId);

			XWPFParagraph paragraph = document.createParagraph();
			XWPFRun r = paragraph.createRun();

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.setText(dictionary.getRaportNPI());
			r.setBold(true);
			r.setFontSize(36);
			paragraph.setAlignment(ParagraphAlignment.CENTER);

			XWPFTable reportTable = document.createTable(4, 2);
			paragraph = reportTable.getRow(0).getCell(0).getParagraphArray(0);
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(dictionary.getProductName());
			r.setBold(true);
			r.setFontSize(14);

			paragraph = reportTable.getRow(0).getCell(1).getParagraphArray(0);
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(productId + " " + productName);
			r.setFontSize(14);

			paragraph = reportTable.getRow(1).getCell(0).getParagraphArray(0);
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(dictionary.getProcessEngineer());
			r.setBold(true);
			r.setFontSize(14);

			paragraph = reportTable.getRow(1).getCell(1).getParagraphArray(0);
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(report.getProcessEngineer());
			r.setFontSize(14);

			paragraph = reportTable.getRow(2).getCell(0).getParagraphArray(0);
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(dictionary.getProductEngineer());
			r.setBold(true);
			r.setFontSize(14);

			paragraph = reportTable.getRow(2).getCell(1).getParagraphArray(0);
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(report.getProductEngineer());
			r.setFontSize(14);

			paragraph = reportTable.getRow(3).getCell(0).getParagraphArray(0);
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(dictionary.getQualityEngineer());
			r.setBold(true);
			r.setFontSize(14);

			paragraph = reportTable.getRow(3).getCell(1).getParagraphArray(0);
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(report.getQualityEngineer());
			r.setFontSize(14);

			setTableAlign(reportTable, ParagraphAlignment.CENTER);


			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.addCarriageReturn();
			r.addCarriageReturn();
			r.setText(dictionary.getRevisionHistory());
			r.setBold(true);
			r.setFontSize(13);
			paragraph.setAlignment(ParagraphAlignment.CENTER);

			XWPFTable revisionTable = document.createTable();
			XWPFTableRow row = revisionTable.getRow(0);
			paragraph = row.getCell(0).getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(dictionary.getRevision());
			r.setBold(true);

			paragraph = row.addNewTableCell().getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(dictionary.getChange());
			r.setBold(true);

			paragraph = row.addNewTableCell().getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(dictionary.getDate());
			r.setBold(true);

			List<ReportEntity> reports = reportRepository.findByProductIdOrderByRevisionAsc(productId);
			for (int i = 0; i < reports.size(); i++) {
				row = revisionTable.createRow();
				paragraph = row.getCell(0).getParagraphArray(0);
				r = paragraph.createRun();
				r.setText(String.valueOf(reports.get(i).getRevision()));

				paragraph = row.getCell(1).getParagraphArray(0);
				r = paragraph.createRun();
				r.setText("");

				paragraph = row.getCell(2).getParagraphArray(0);
				r = paragraph.createRun();
				r.setText(String.valueOf(reports.get(i).getTimestamp().getTime()));
			}

			setTableAlign(revisionTable, ParagraphAlignment.CENTER);

			try (FileOutputStream outputStream = new FileOutputStream(realPathToUploads + fileName)) {
				document.write(outputStream);
			} catch (Exception e) {
				e.printStackTrace();
			}

			paragraph = document.createParagraph();
			paragraph.setPageBreak(true);
			r = paragraph.createRun();
			r.setText("1. " + dictionary.getIntroduction());
			r.setBold(true);
			r.setFontSize(16);

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			if (report.getIntroduction().equals("")) {
				r.setText("Krótki opis produktu oraz przebiegu wdrożenia – wyszczególnienie procesów, jakie miały miejsce, każdy proces po 2-3 zdania opisu");
			} else {
				r.setText(report.getIntroduction());
			}

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.setText("2. " + dictionary.getFoundProblems());
			r.setFontSize(16);
			r.setBold(true);
			//r = paragraph.createRun();

			paragraph = document.createParagraph();
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.setText(dictionary.getCriticals());
			r.setFontSize(16);
			r.setBold(true);
			//r = paragraph.createRun();

			List<ProblemEntity> majorProblems = problemsService.findMajorByReportId(reportId);
			/*int nCols = 10;
			int nRows = majorProblems.size();*/
			fillTable(document, majorProblems, productId, dictionary);

			paragraph = document.createParagraph();
			paragraph.setAlignment(ParagraphAlignment.CENTER);
			r = paragraph.createRun();
			r.addCarriageReturn();
			r.setText(dictionary.getOtherObservations());
			r.setFontSize(16);
			r.setBold(true);

			List<ProblemEntity> minorProblems = problemsService.findMinorByReportId(reportId);
			/*nCols = 10;
			nRows = minorProblems.size();*/
			fillTable(document, minorProblems, productId, dictionary);

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.addCarriageReturn();
			r.setText("3. " + dictionary.getDetailedDescription());
			r.setFontSize(16);
			r.setBold(true);

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.setText("W przypadku potrzeby rozszerzenia opisu, dodania większych zdjęć do konkretnych problemów z pkt. 2");

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.addCarriageReturn();
			r.setText("4. " + dictionary.getEconomicalSummary());
			r.setFontSize(16);
			r.setBold(true);

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.setText(report.getSummary());

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.addCarriageReturn();
			r.setText("5. " + dictionary.getConclusion());
			r.setFontSize(16);
			r.setBold(true);

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.setText(report.getConclusion());

			paragraph = document.createParagraph();
			r = paragraph.createRun();
			r.addCarriageReturn();
			r.setText("6. " + dictionary.getAttachments());
			r.setFontSize(16);
			r.setBold(true);

			int k = 1;
			for (String reportAttachmentSrc : report.getAttachmentSrc()) {
				if (!reportAttachmentSrc.equals("")) {
					srcFiles.add(Main.getUploadPath().replace("disk/", "") + reportAttachmentSrc);

					paragraph = document.createParagraph();
					String id = paragraph.getDocument().getPackagePart().addExternalRelationship(reportAttachmentSrc.replace("/disk/" + productId + "/", ""), XWPFRelation.HYPERLINK.getRelation()).getId();

					CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
					cLink.setId(id);

					CTText ctText = CTText.Factory.newInstance();
					ctText.setStringValue(dictionary.getAttachment() + " " + k);
					CTR ctr = CTR.Factory.newInstance();
					ctr.setTArray(new CTText[]{ctText});
					cLink.setRArray(new CTR[]{ctr});
					k++;
				}
			}
			/*CTSectPr sectPr = document.getDocument().getBody().addNewSectPr();
			XWPFHeaderFooterPolicy policy = new XWPFHeaderFooterPolicy(document, sectPr);

			CTP ctpHeader = CTP.Factory.newInstance();
			//CTR ctrHeader = ctpHeader.addNewR();
			//CTText ctHeader = ctrHeader.addNewT();
			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
			Date now = new Date();
			String strDate = simpleDateFormat.format(now);
			String headerText = "Pruszcz Gdański, " + strDate;
			//ctHeader.setStringValue(headerText);

			try {
				XWPFParagraph headerParagraph = new XWPFParagraph(ctpHeader, document);
				XWPFParagraph[] parsHeader = new XWPFParagraph[1];
				headerParagraph.setAlignment(ParagraphAlignment.LEFT);
				XWPFRun run = headerParagraph.createRun();
				InputStream is = classLoader.getResourceAsStream("assel.jpg");
				run.addPicture(is, XWPFDocument.PICTURE_TYPE_JPEG, "assel.jpg", Units.toEMU(200), Units.toEMU(50));
				is.close();
				run.addTab();
				run.addTab();
				run.addTab();
				run.addTab();
				run.addTab();
				run.addTab();
				run.addTab();
				run.addTab();
				run.addTab();
				run.addTab();
				run.addTab();
				run.setText(headerText);
				//headerParagraph.removeRun(0);
				parsHeader[0] = headerParagraph;
				XWPFHeader header = policy.createHeader(XWPFHeaderFooterPolicy.DEFAULT, parsHeader);
				System.out.println(header.getText());
				//policy.getHeader(XWPFHeaderFooterPolicy.DEFAULT);
			} catch (NullPointerException e) {
				e.printStackTrace();
			}

			CTP ctpFooter = CTP.Factory.newInstance();
			CTR ctrFooter = ctpFooter.addNewR();
			CTText ctFooter = ctrFooter.addNewT();
			String footerText = "Revision " + report.getRevision();
			ctFooter.setStringValue(footerText);
			XWPFParagraph footerParagraph = new XWPFParagraph(ctpFooter, document);
			XWPFParagraph[] parsFooter = new XWPFParagraph[1];
			parsFooter[0] = footerParagraph;
			policy.createFooter(XWPFHeaderFooterPolicy.DEFAULT, parsFooter);*/

			try (FileOutputStream outputStream = new FileOutputStream(realPathToUploads + fileName)) {
				document.write(outputStream);
				document.close();
			} catch (Exception e) {
				e.printStackTrace();
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
	}

	public void setTableAlign(XWPFTable table, ParagraphAlignment align) {
		CTTblPr tblPr = table.getCTTbl().getTblPr();
		CTJc jc = (tblPr.isSetJc() ? tblPr.getJc() : tblPr.addNewJc());
		STJc.Enum en = STJc.Enum.forInt(align.getValue());
		jc.setVal(en);
	}

	private void fillTable(XWPFDocument document, List<ProblemEntity> problems, long productId, WordDictionary dictionary) {
		XWPFTable table = document.createTable();
		XWPFTableRow row = table.getRow(0);

		/*r.setText("#");
		r.setBold(true);*/

		XWPFParagraph paragraph = row.getCell(0).getParagraphArray(0);
		XWPFRun r = paragraph.createRun();
		r = paragraph.createRun();
		r.setText(dictionary.getPriority());
		r.setFontSize(10);
		r.setBold(true);

		paragraph = row.addNewTableCell().getParagraphArray(0);
		r = paragraph.createRun();
		r.setText(dictionary.getCategory());
		r.setFontSize(10);
		r.setBold(true);

		paragraph = row.addNewTableCell().getParagraphArray(0);
		r = paragraph.createRun();
		r.setText(dictionary.getDescription());
		r.setFontSize(10);
		r.setBold(true);

		paragraph = row.addNewTableCell().getParagraphArray(0);
		r = paragraph.createRun();
		r.setText("");
		r.setFontSize(10);
		r.setBold(true);

		paragraph = row.addNewTableCell().getParagraphArray(0);
		r = paragraph.createRun();
		r.setText(dictionary.getPhoto());
		r.setFontSize(10);
		r.setBold(true);

		paragraph = row.addNewTableCell().getParagraphArray(0);
		r = paragraph.createRun();
		r.setText(dictionary.getReccommendations());
		r.setFontSize(10);
		r.setBold(true);

		paragraph = row.addNewTableCell().getParagraphArray(0);
		r = paragraph.createRun();
		r.setText(dictionary.getStatus());
		r.setFontSize(10);
		r.setBold(true);

		paragraph = row.addNewTableCell().getParagraphArray(0);
		r = paragraph.createRun();
		r.setText(dictionary.getAnswer());
		r.setFontSize(10);
		r.setBold(true);

		paragraph = row.addNewTableCell().getParagraphArray(0);
		r = paragraph.createRun();
		r.setText("");
		r.setFontSize(10);
		r.setBold(true);

		String pathToImages = Main.getUploadPath().replace("/disk/", "/");
		ClassLoader classLoader = getClass().getClassLoader();
		for (int i = 0; i < problems.size(); i++) {
			XWPFTableRow problemRow = table.createRow();
			//problemRow.getCell(0).setText(String.valueOf((i + 1)));
			//problemRow.getCell(0).setText(String.valueOf(problems.get(i).getPriority()));
			paragraph = problemRow.getCell(0).getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(String.valueOf(problems.get(i).getPriority()));
			r.setFontSize(9);
			//problemRow.getCell(1).setText(problems.get(i).getCategory());
			paragraph = problemRow.getCell(1).getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(problems.get(i).getCategory());
			r.setFontSize(9);
//			problemRow.getCell(2).setText(problems.get(i).getDescription());
			paragraph = problemRow.getCell(2).getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(problems.get(i).getDescription());
			r.setFontSize(9);
			XWPFParagraph warningPara = problemRow.getCell(3).getParagraphArray(0);
			try {
				r = warningPara.createRun();
				InputStream inputStream;
				switch (problems.get(i).getWarning()) {
					case 0:
						break;
					case 1:
						inputStream = classLoader.getResourceAsStream("ctqExcel.png");
						r.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, "name", Units.toEMU(30), Units.toEMU(30));
						inputStream.close();
						break;
					case 2:
						inputStream = classLoader.getResourceAsStream("otdExcel.png");
						r.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, "name", Units.toEMU(30), Units.toEMU(30));
						inputStream.close();
						break;
					case 3:
						inputStream = classLoader.getResourceAsStream("ctqOtdExcel.png");
						r.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, "name", Units.toEMU(30), Units.toEMU(60));
						inputStream.close();
						break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
//			problemRow.getCell(4).setText("");

			for (String imgSrc : problems.get(i).getImgSrc()) {
				XWPFParagraph imgPara = problemRow.getCell(4).getParagraphArray(0);
				if (!imgSrc.equals("")) {
					try {
						r = imgPara.createRun();
						//r.setText("Tutaj zdjęcie");
						FileInputStream fis = new FileInputStream(pathToImages + imgSrc);
						BufferedImage img = ImageIO.read(fis);
						int height = img.getHeight();
						int width = img.getWidth();
						double ratio = (double) height / width;
						ByteArrayOutputStream os = new ByteArrayOutputStream();
						ImageIO.write(img, "jpg", os);
						InputStream is = new ByteArrayInputStream(os.toByteArray());
						if (imgSrc.contains(".jpeg") || imgSrc.contains(".jpg")) {
							r.addPicture(is, XWPFDocument.PICTURE_TYPE_JPEG, "name.jpeg", Units.toEMU(200), Units.toEMU(Math.round(ratio * 200)));
							r.addCarriageReturn();
						} else if (imgSrc.contains(".png")) {
							r.addPicture(is, XWPFDocument.PICTURE_TYPE_PNG, "name.png", Units.toEMU(200), Units.toEMU(Math.round(ratio * 200)));
							r.addCarriageReturn();
						}
						is.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
			/*problemRow.getCell(5).setText("Zdjęcia");*/
//			problemRow.getCell(5).setText(problems.get(i).getRecommendation());
			paragraph = problemRow.getCell(5).getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(problems.get(i).getRecommendation());
			r.setFontSize(9);
//			problemRow.getCell(6).setText(problems.get(i).getStatus());
			paragraph = problemRow.getCell(6).getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(problems.get(i).getStatus());
			r.setFontSize(9);
//			problemRow.getCell(7).setText(problems.get(i).getAnswer());
			paragraph = problemRow.getCell(7).getParagraphArray(0);
			r = paragraph.createRun();
			r.setText(problems.get(i).getAnswer());
			r.setFontSize(9);
//			problemRow.getCell(8).setText("");

			String src = problems.get(i).getAttachmentSrc();
			if (!src.equals("")) {

				srcFiles.add(Main.getUploadPath().replace("disk/", "") + src);
				paragraph = problemRow.getCell(8).getParagraphArray(0);
				r = paragraph.createRun();
				r.setFontSize(9);

				String id = paragraph.getDocument().getPackagePart().addExternalRelationship(src.replace("/disk/" + productId + "/", ""), XWPFRelation.HYPERLINK.getRelation()).getId();

				CTHyperlink cLink = paragraph.getCTP().addNewHyperlink();
				cLink.setId(id);

				CTText ctText = CTText.Factory.newInstance();
				ctText.setStringValue(dictionary.getAtt());
				CTR ctr = CTR.Factory.newInstance();
				ctr.setTArray(new CTText[]{ctText});
				cLink.setRArray(new CTR[]{ctr});

			}
		}

	}

	private String getReportNameExcel(long reportId) {
		ReportEntity report = findOne(reportId);
		long productId = report.getProductId();
		BigDecimal reportRev = report.getRevision();
		//String productName = productsService.getProductById(productId).getName();

		return productId + "-rev." + reportRev + ".xlsx";
	}

	private String getReportNameWord(long reportId) {
		ReportEntity report = findOne(reportId);
		long productId = report.getProductId();
		BigDecimal reportRev = report.getRevision();
		//String productName = productsService.getProductById(productId).getName();

		return productId + "-rev." + reportRev + ".docx";
	}

	public String getReportNameZip(long reportId) {
		ReportEntity reportEntity = findOne(reportId);
		long productId = reportEntity.getProductId();
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
