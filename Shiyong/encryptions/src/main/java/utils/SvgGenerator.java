package srp.bapp.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.JPEGTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.log4j.Logger;

public class SvgGenerator {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(SvgGenerator.class);

	public static void svgGenerate4Highcharts(String svgString, String type, OutputStream outputStream) throws IOException {

		ImageTranscoder t;
		if(type.equals("jpeg")){
			t=new JPEGTranscoder();
			t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY, new Float(1.0));
		}else{
			t=new PNGTranscoder();
		}
		//svgString = svgString.replaceAll("<svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"");
		TranscoderInput input = new TranscoderInput(new StringReader(svgString));
		try {
			TranscoderOutput output = new TranscoderOutput(outputStream);
			t.transcode(input, output);
			System.out.println("*****************");
		} catch (Exception e) {

			if (logger.isInfoEnabled()) {
				logger.info("svgGenerate(String, String, String, OutputStream)", e); //$NON-NLS-1$
			}
		}
	}

	public static ByteArrayOutputStream svgByteArrayOutputStream (String svgString) throws IOException {
		ImageTranscoder t;
		t=new PNGTranscoder();
		//svgString = svgString.replaceAll("<svg", "<svg xmlns=\"http://www.w3.org/2000/svg\"");
		TranscoderInput input = new TranscoderInput(new StringReader(svgString));
		ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream(); 
		try {
			TranscoderOutput output = new TranscoderOutput(byteArrayOut);
			t.transcode(input, output);
			return byteArrayOut;
		} catch (Exception e) {
			if (logger.isInfoEnabled()) {
				logger.info("svgGenerate(String, String, String, OutputStream)", e); //$NON-NLS-1$
			}
			return null;
		}
	}
	public static void main(String[] args) throws FileNotFoundException, IOException {
		svgGenerate4Highcharts("<svg />", "png", new FileOutputStream("e:/test.png"));
	}
}
/*
 * <!--
 * 
 * @RequestMapping(value="/svg",method=RequestMethod.POST) private void
 * svgServer(HttpServletRequest request,HttpServletResponse response) throws
 * IOException{ String svgString = request.getParameter("svg");
 * 
 * String type = request.getParameter("type"); response.setContentType(type);
 * String filename = new
 * Date().toLocaleString().replace(" ","_")+"."+type.substring(6);
 * response.setHeader("Content-disposition","attachment;filename=" + filename);
 * 
 * JPEGTranscoder t = new JPEGTranscoder();
 * t.addTranscodingHint(JPEGTranscoder.KEY_QUALITY,new Float(.8));
 * TranscoderInput input = new TranscoderInput(new StringReader(svgString)); try
 * { TranscoderOutput output = new TranscoderOutput(response.getOutputStream());
 * t.transcode(input, output); response.getOutputStream().flush();
 * response.getOutputStream().close(); }catch (Exception e){
 * response.getOutputStream().close(); logger.error("",e); } }
 * 在页面上我们将Ext.chart.Chart.save替换为： var mySave = function(surface, config) {
 * config = config || {}; var exportTypes = { 'image/png': 'Image',
 * 'image/jpeg': 'Image', 'image/svg+xml': 'Svg' }, prefix =
 * exportTypes[config.type] || 'Svg', exporter = Ext.draw.engine[prefix +
 * 'Exporter']; exporter.defaultUrl = '<%=basePath+"svg.do" %>'; return
 * exporter.generate(surface, config); };
 * 
 * mySave(chart.surface,{ type: 'image/jpeg' });
 * 
 * -->
 */