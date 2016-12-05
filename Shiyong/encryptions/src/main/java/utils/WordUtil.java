package srp.bapp.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.converter.WordToHtmlConverter;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.xwpf.converter.core.FileImageExtractor;
import org.apache.poi.xwpf.converter.core.FileURIResolver;
import org.apache.poi.xwpf.converter.core.XWPFConverterException;
import org.apache.poi.xwpf.converter.xhtml.XHTMLConverter;
import org.apache.poi.xwpf.converter.xhtml.XHTMLOptions;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;

public class WordUtil {
	private static final Logger logger = Logger.getLogger(WordUtil.class);

	public static void main(String argv[]) {
		try {
			System.out.println(convert2Html("E://test//冻结、轮候冻结存款.docx"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void writeFile(String content, String path) {
		FileOutputStream fos = null;
		BufferedWriter bw = null;
		try {
			File file = new File(path);
			fos = new FileOutputStream(file);
			bw = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
			bw.write(content);
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				if (bw != null)
					bw.close();
				if (fos != null)
					fos.close();
			} catch (IOException ie) {
			}
		}
	}

	public static byte[] convert2Html(XWPFDocument document) {
		try {
			File imageFolderFile = new File(FileUtil.TEMP_PATH_NAME + File.separator);
			XHTMLOptions options = XHTMLOptions.create().URIResolver(new FileURIResolver(imageFolderFile));
			options.setExtractor(new FileImageExtractor(imageFolderFile));
			options.indent(4);
			options.setIgnoreStylesIfUnused(false);
			options.setFragment(true);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			XHTMLConverter.getInstance().convert(document, out, options);
			out.close();
			return out.toByteArray();
		} catch (XWPFConverterException e) {
			logger.error("", e);
			return null;
		} catch (IOException e) {
			logger.error("", e);
			return null;
		}
	}

	public static byte[] convert2Html(String fileName) {
		try {
			HWPFDocument wordDocument = new HWPFDocument(new FileInputStream(fileName));
			WordToHtmlConverter wordToHtmlConverter = new WordToHtmlConverter(DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument());
			// wordToHtmlConverter.setPicturesManager(new PicturesManager() {
			// public String savePicture(byte[] content, PictureType
			// pictureType, String suggestedName, float widthInches, float
			// heightInches) {
			// return "/" + suggestedName;
			// }
			// });
			wordToHtmlConverter.processDocument(wordDocument);
			// save pictures
			// List<Picture> pics =
			// wordDocument.getPicturesTable().getAllPictures();
			// if (pics != null) {
			// for (int i = 0; i < pics.size(); i++) {
			// Picture pic = (Picture) pics.get(i);
			// try {
			// pic.writeImageContent(new FileOutputStream("D://" +
			// pic.suggestFullFileName()));
			// } catch (FileNotFoundException e) {
			// e.printStackTrace();
			// }
			// }
			// }
			Document htmlDocument = wordToHtmlConverter.getDocument();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DOMSource domSource = new DOMSource(htmlDocument);
			StreamResult streamResult = new StreamResult(out);
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer serializer = tf.newTransformer();
			serializer.setOutputProperty(OutputKeys.ENCODING, "GB2312");
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(OutputKeys.METHOD, "html");
			serializer.transform(domSource, streamResult);
			out.close();
			return out.toByteArray();
		} catch (OfficeXmlFileException e) {
			try {
				InputStream in = new FileInputStream(new File(fileName));
				XWPFDocument document = new XWPFDocument(in);
				return convert2Html(document);
			} catch (Exception e1) {
				logger.error("", e1);
				return null;
			}
		} catch (Exception e) {
			logger.error("", e);
			return null;
		}
	}
}
