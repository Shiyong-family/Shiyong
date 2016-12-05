package srp.bapp.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

public class PDFUtil {
	private static final Logger logger = Logger.getLogger(PDFUtil.class);

	public static void main(String[] args) throws Exception {
		//byte[] htmlStr = WordUtil.convert2Html("E://test//冻结、轮候冻结存款.docx");
		FileInputStream fis = new FileInputStream("E://test//冻结、轮候冻结存款.xml");
		FileOutputStream fos =new FileOutputStream("E://test//冻结、轮候冻结存款.pdf");
		byte[] createPdf = createPdf(InputStreamUtil.inputStreamTOByte(fis));
		System.out.println(new String(createPdf));
		fos.write(createPdf);
		fis.close();
		fos.close();
	}

	public static byte[] createPdf(byte[] html) {
		// BaseFont bfChinese = BaseFont.createFont("STSong-Light",
		// "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
		// Font FontChinese = new Font(bfChinese, 12, Font.NORMAL);

		Document document = new Document();
		PdfWriter writer;
		ByteArrayOutputStream out = null;
		try {
			out = new ByteArrayOutputStream();
			writer = PdfWriter.getInstance(document, out);
			document.open();
			XMLWorkerHelper.getInstance().parseXHtml(writer, document, InputStreamUtil.byteTOInputStream(html), Charset.forName("UTF-8"));
			document.close();
			return out.toByteArray();
		} catch (Exception e) {
			logger.error("", e);
			return null;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

}
