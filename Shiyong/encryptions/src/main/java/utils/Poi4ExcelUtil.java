package srp.bapp.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.format.CellFormat;
import org.apache.poi.ss.format.CellFormatResult;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.BeanUtils;

import srp.bapp.tools.TDHandler;
import srp.bapp.tools.TRHandler;

/**
 * 一组Excel格式的Html代码生成功能
 * 
 * @author oofrank
 * 
 */
public abstract class Poi4ExcelUtil {

	/**
	 * 单元格宽度系数，修正poi解析excel的单元格宽度与实际单元格宽系数0.625
	 */
	private static final double REVISE_WIDTH = 0.625;
	/**
	 * 高度像素倍数，poi解析excel的宽度转换成像素的倍数
	 */
	private static final double WIDTH_MULITIPLE = 8.2;
	/**
	 * 列宽比系数，poi宽度计算为： 宽度=（字符个数*（字符宽度-1）+5）、（字符宽度-1）*256
	 */
	private static final double WIDTH_POINT = 256.0;
	/**
	 * 行高比系数，excel列高单位为磅，poi列高单位为缇，1磅=0.353毫米=20缇
	 */
	private static final double HEIGHT_POINT = 20.0;
	/**
	 * 行默认高度 18
	 */
	private static final int DEFAULT_HEIGHT = 0x12;
	/**
	 * 列默认宽度72
	 */
	private static final int DEFAULT_WIDTH = 0x48;

	public static void main(String[] args) throws IOException {
		Workbook wbs = new HSSFWorkbook();
		Workbook wb = Poi4ExcelUtil.getWorkbook(new FileInputStream("e:/G01_template.xls"));
		Workbook wb1 = Poi4ExcelUtil.getWorkbook(new FileInputStream("e:/G01_1_template.xls"));
		// 测试
		// String str = Poi4ExcelUtil.getHtml(wb.getSheetAt(0), new TDHandler()
		// {
		//
		// public String getTDContent(Sheet sheet, int r, int c, String content)
		// {
		// return content;
		// }
		//
		// @Override
		// public String getTDAttribute(Sheet sheet, int rowIndex, int
		// columnIndex) {
		// return null;
		// }
		// }, "test");
		copySheet(wbs, wb);
		copySheet(wbs, wb1);
		FileOutputStream os = new FileOutputStream("e:/G02.xls");
		wbs.write(os);

	}

	private static final Logger logger = Logger.getLogger(Poi4ExcelUtil.class);

	/**
	 * 获取工作薄
	 * 
	 * @param in
	 * @return
	 */
	public static Workbook getWorkbook(InputStream in) {
		try {
			return WorkbookFactory.create(in);
		} catch (InvalidFormatException e) {
			logger.error("getWorkbook(InputStream)", e);
		} catch (IOException e) {
			logger.error("getWorkbook(InputStream)", e);
		}
		return null;
	}

	public static String getHtml(InputStream in, TDHandler tDHandler, String id) {
		return getHtml(in, null, tDHandler, id);
	}

	public static String getHtml(InputStream in, TRHandler trHandler, TDHandler tDHandler, String id) {
		Workbook wb = getWorkbook(in);
		if (null == wb) {
			return null;
		}
		return getHtml(wb.getSheetAt(0), null, trHandler, tDHandler, id);
	}

	public static String getHtml(Sheet sheet, TDHandler tDHandler, String id) {
		return getHtml(sheet, null, null, tDHandler, id);
	}

	public static String getHtml(Sheet sheet, TRHandler trHandler, TDHandler tDHandler, String id) {
		return getHtml(sheet, null, trHandler, tDHandler, id);
	}

	/**
	 * excel转为html代码
	 * 
	 * @param sheet
	 * @return
	 */
	public static String getHtml(Sheet sheet, String style, TRHandler trHandler, TDHandler tDHandler, String id) {
		String tableClass = "C" + id;
		StringBuffer sb = new StringBuffer();
		sb.append("<style>\n");
		String[] css = createStyleTagByCellStyle(sb, sheet.getWorkbook(), tableClass);
		// <style>部分
		if (!StringUtil.isEmpty(style)) {
			sb.append(style);
		}
		sb.append("." + tableClass
				+ " {table-layout:fixed;border:1px solid black;border-collapse: collapse;white-space:nowrap;word-break:keep-all;}\n");
		sb.append("." + tableClass + " TD {border-right:1px solid black;border-bottom: 1px solid black;overflow:hidden;}\n");
		sb.append("." + tableClass + " .exinput {border:none;width:100%;height:100%;background:transparent;}\n");
		sb.append("." + tableClass + " th{background-color: #E6E6FA;border-right:1px solid black;border-bottom: 1px solid black;}\n");
		// 所有input
		sb.append("." + tableClass + " .i{ width:100%; height:100%; border:none; padding:0 0 0 0;background:transparent;}\n");
		// 输入框
		sb.append("." + tableClass + " .it{ border-bottom:solid 1px #32CD32;}\n");
		// 只读的
		// sb.append("." + tableClass + " .ro{ background: transparent;}\n");
		sb.append("." + tableClass + " .cell-mouseover{background:#D0E5F5;}\n");
		sb.append("." + tableClass + " .cell-selected{background:#FBEC88;}\n");
		sb.append("." + tableClass + " .cell-bg-yellow{background:yellow;}\n");
		sb.append("." + tableClass + " .cell-bg-red{background:red;}\n");
		sb.append("." + tableClass + " .cell-b-orange{border: 1px solid orange;}\n");
		sb.append("." + tableClass + " .cell-warning{background:yellow;}\n");
		sb.append("." + tableClass + " .cell-error{background:red;}\n");

		// 靠右对齐
		sb.append("." + tableClass + " .rt{text-align:right;}\n");
		//继承
		sb.append("." + tableClass + " .jc{text-align:inherit;}\n");
		sb.append("table.imagetable{table-layout:fixed;width:100%;border:1px solid black;border-collapse:collapse;font-family:verdana,arial,sans-serif;font-size:11px;}\n");
		sb.append("table.imagetable th{background-color: #E6E6FA;border-right:1px solid black;border-bottom: 1px solid black;padding:8px;}\n");
		sb.append("table.imagetable td{word-break:break-all;word-wrap:break-word;border-right:1px solid black;border-bottom: 1px solid black;padding:8px;}\n");
		sb.append("</style>\n");
		int maxCols = -1;
		int maxColsRowNum = 0;
		for (int r = 0; r <= sheet.getLastRowNum(); r++) {
			Row row = sheet.getRow(r);
			if (null == row) {
				continue;
			}
			maxCols = Math.max(row.getLastCellNum(), maxCols);
			maxColsRowNum = r;
		}
		// <table>部分
		sb.append("<table class='" + tableClass + "' name='extable' cellpadding='0' width='" + getWidth(sheet, maxColsRowNum) + "px'>");
		// 给最上面的行列添加数值1,2,3...
		sb.append("<tr><th width=\"50\">&nbsp;</th>");

		for (int cNum = 1; cNum < maxCols + 1; cNum++) {
			sb.append("<th width=\"" + getColWidth(sheet, cNum - 1) + "px\"");
			if (sheet.isColumnHidden(cNum - 1)) {
				sb.append(" style=\"display:none\" ");
			}
			sb.append(">").append(cNum).append("</th>");
		}
		sb.append("</tr>");
		Set<CellRangeAddress> mergeSet = new HashSet<CellRangeAddress>();
		for (int r = 0; r < sheet.getLastRowNum() + 1; r++) {
			Row row = sheet.getRow(r);
			if (null == row) {
				sb.append("<tr></tr>");
				continue;
			}

			sb.append("<tr");
			if (null != trHandler) {
				sb.append(trHandler.getTRAttribute(sheet, r));
			}
			if (row.getZeroHeight()) {
				sb.append(" style=\"display:none\" >");
			} else {
				int height = getRowHeight(row);
				sb.append(" height='" + height + "px'>");
			}
			sb.append("<th>").append(r + 1).append("</th>");
			for (int c = 0; c < row.getLastCellNum(); c++) {
				Cell cell = row.getCell(c);
				int height = getRowHeight(row);
				if (null == cell) { // 未定义单元格，设置一个空单元格
					sb.append("<td c=\"" + (c + 1) + "\"");
					if (sheet.isColumnHidden(c)) {
						sb.append(" style=\"display:none\" ");
					} else {
						sb.append(" height='" + height + "px'");
					}
					sb.append(">&nbsp;</td>");
					continue;
				}
				CellRangeAddress region = getRegionContains(sheet, r, c);
				if (region == null) { // 非合并单元格
					sb.append("<td c=\"" + (c + 1) + "\"");
					if (sheet.isColumnHidden(c)) {
						sb.append(" style=\"display:none\" ");
					} else {
						sb.append(" height='" + height + "px'");
					}
					if (cell != null && cell.getCellStyle() != null) {
						sb.append(" class=\"").append(css[cell.getCellStyle().getIndex()]).append("\"");
					}
					sb.append(getCellAttribute(cell, tDHandler));
//					if(StringUtil.isEmpty(getCellValue(cell))){
//						sb.replace(sb.lastIndexOf("\""), sb.length(), "text-align:right\"");
//						//sb.append("text-align:right;");
//					}
					sb.append(">");
					sb.append(getCellValue(cell, tDHandler));
					sb.append("</td>");
				} else { // 合并单元格
					if (region.getFirstColumn() == c && region.getFirstRow() == r) {
						mergeTd(sheet, tDHandler, sb, css, c, cell, region);
					} else if (!mergeSet.contains(region) && !sheet.isColumnHidden(c) && region.getFirstRow() < r && !row.getZeroHeight()) {// 合并单元格中第一行为隐藏列
						Row frow = sheet.getRow(region.getFirstRow());
						if (null != frow && frow.getZeroHeight()) {
							mergeSet.add(region);
							mergeTd(sheet, tDHandler, sb, css, c, cell, region);
						}

					}
				}
			}
			sb.append("</tr>");
		}
		sb.append("</table>");
		return sb.toString();
	}

	private static void mergeTd(Sheet sheet, TDHandler tDHandler, StringBuffer h, String[] css, int c, Cell cell, CellRangeAddress region) {
		int colspan = region.getLastColumn() - region.getFirstColumn() + 1;
		int rowspan = region.getLastRow() - region.getFirstRow() + 1;
		int height = 0;
		for (int rr = region.getFirstRow(); rr <= region.getLastRow(); rr++) {
			Row rrow = sheet.getRow(rr);
			if (null == rrow) {
				continue;
			}
			if (rrow.getZeroHeight()) {
				rowspan--;
			} else {
				height += getRowHeight(rrow);
			}
			for (int rc = region.getFirstColumn(); rc <= region.getLastColumn(); rc++) {
				if (sheet.isColumnHidden(rc)) {
					colspan--;
				}
			}
		}
		h.append("<td c=\"" + (c + 1) + "\""); // width='" + getColWidth(sheet,
												// c) + "px'
		if (0 < height) {
			h.append(" height='").append(height).append("px'");
		}
		if (1 < colspan) {
			h.append(" colspan='").append(colspan).append("'");
		}
		if (1 < rowspan) {
			h.append(" rowspan='").append(rowspan).append("'");
		}
		if (cell != null && cell.getCellStyle() != null) {
			h.append("class='").append(css[cell.getCellStyle().getIndex()]).append("' ");
			h.append(getCellAttribute(cell, tDHandler));
		}
		h.append(">");
		if (cell != null) {
			h.append(getCellValue(cell, tDHandler));
		}
		h.append("</td>");
	}

	/**
	 * 得到cell值
	 * 
	 * @param cell
	 * @return
	 */
	private static String getCellAttribute(Cell cell, TDHandler tDHandler) {
		String attr = tDHandler.getTDAttribute(cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex());
		return StringUtil.isEmpty(attr) ? "" : attr;
	}

	/**
	 * 得到cell值
	 * 
	 * @param cell
	 * @return
	 */
	private static String getCellValue(Cell cell, TDHandler tDHandler) {
		String cellValue = getCellValue(cell);
		if (cell.getCellStyle() != null) {
			CellStyle style = cell.getCellStyle();
			cellValue = HtmlEncoder.encode(StringUtil.fillString("", 3 * style.getIndention(), ' ') + StringUtil.nvl(cellValue, ""));
		}
		try {
			cellValue = tDHandler.getTDContent(cell.getSheet(), cell.getRowIndex(), cell.getColumnIndex(), cellValue);
		} catch (Exception e) {
			// cell==null
		}
		return StringUtil.isEmpty(cellValue) ? "&nbsp;" : cellValue;
	}

	public static String getCellValue(Cell cell) {
		return getCellValueIgnoreFormat(cell);
	}

	public static String getCellValueIgnoreFormat(Cell cell) {
		if (cell == null)
			return null;
		String cellValue = "";
		CellStyle style = cell.getCellStyle();
		if (null == style || null == style.getDataFormatString()) {
			return cellValue;
		}
		// @的数值类型单元格不能使用getNumericCellValue正确取数
		if ((Cell.CELL_TYPE_NUMERIC == cell.getCellType()) && !"@".equals(style.getDataFormatString())) {
			cellValue = StringUtil.doubleToStringNotEndWithZero(cell.getNumericCellValue(), 6);
		} else {
			CellFormat cf = CellFormat.getInstance(style.getDataFormatString());
			CellFormatResult result = cf.apply(cell);
			try {
				cellValue = result.text;
			} catch (Exception e) {
				cellValue = "";
			}
		}
		return cellValue;
	}

	/**
	 * 
	 * @param cell
	 * @param dateFormat
	 *            null 表示不是日期
	 * @return
	 */
	public static String getCellValue4Import(Cell cell, String dateFormat) {
		if (cell == null)
			return null;
		String cellValue = "";
		CellStyle style = cell.getCellStyle();
		if (null == style || null == style.getDataFormatString()) {
			return cellValue;
		}
		// @的数值类型单元格不能使用getNumericCellValue正确取数
		if (Cell.CELL_TYPE_NUMERIC == cell.getCellType() && dateFormat != null) {
			cellValue = DateUtil.formatDateTime(new Date((long) cell.getNumericCellValue()), dateFormat);
		} else if ((Cell.CELL_TYPE_NUMERIC == cell.getCellType()) && !"@".equals(style.getDataFormatString())) {
			cellValue = StringUtil.doubleToStringNotEndWithZero(cell.getNumericCellValue(), 6);
		} else {
			CellFormat cf = CellFormat.getInstance(style.getDataFormatString());
			CellFormatResult result = cf.apply(cell);
			cellValue = result.text;
		}
		return cellValue;
	}

	/**
	 * 合并单元格
	 * 
	 * @param sheet
	 * @param r
	 * @param c
	 * @return
	 */
	private static CellRangeAddress getRegionContains(Sheet sheet, int r, int c) {
		// 得到所有合并的区域个数sheet.getNumMergedRegions()
		for (int m = 0; m < sheet.getNumMergedRegions(); m++) {
			CellRangeAddress region = sheet.getMergedRegion(m);
			if (region.isInRange(r, c)) {
				return region;
			}
		}
		return null;
	}

	/**
	 * 根据CellStyle获取样式标签
	 * 
	 * @param sw
	 * @param wb
	 * @param tableClass
	 * @return
	 */
	private static String[] createStyleTagByCellStyle(StringBuffer sw, Workbook wb, String tableClass) {
		String[] css = new String[wb.getNumCellStyles()];
		StringBuffer sb = new StringBuffer();
		HashMap<String, Object> hm = new HashMap<String, Object>();
		for (short i = 0; i < wb.getNumCellStyles(); i++) {
			CellStyle cs = wb.getCellStyleAt(i);
			sb.append("{");
			if (getBGColor(cs) != null) {
				sb.append("background-color:").append(getHTMLBGColor(cs)).append(";");
			}
			if (getFontColor(wb, cs) != null) {
				sb.append("color:").append(getHTMLFontColor(wb, cs)).append(";");
			}

			sb.append("font-family:").append(getHTMLFontName(wb, cs)).append(";");
			sb.append("font-size:").append(getHTMLFontSize(wb, cs)).append("pt;");
			sb.append("font-weight:" + getHTMLFontBoldweight(wb, cs)).append(";");
			sb.append(getHTMLFontStyle(wb, cs));

			if (cs.getAlignment() == CellStyle.ALIGN_CENTER) {
				sb.append("text-align:center;margin: 0 auto;");
			} else if (cs.getAlignment() == CellStyle.ALIGN_RIGHT) {
				sb.append("text-align:right;margin: 0 auto;");
			} else if (cs.getAlignment() == CellStyle.ALIGN_CENTER_SELECTION) {
				sb.append("text-align:center;");
			} else if (cs.getAlignment() == CellStyle.ALIGN_LEFT) {

				sb.append("text-align:left;");

			}
			sb.append("}\n");
			String ssb = sb.toString();
			if (hm.containsKey(ssb)) {
				css[i] = (String) hm.get(ssb);
			} else {
				hm.put(ssb, getCssName(i));
				css[i] = getCssName(i);
				sw.append("." + tableClass + " ." + getCssName(i) + ssb);
			}
			sb.delete(0, sb.length());
		}
		return css;
	}

	private static String getCssName(short i) {
		return "r" + i;
	}

	private static Object getHTMLFontStyle(Workbook wb, CellStyle cs) {
		if (wb.getFontAt(cs.getFontIndex()).getItalic()) {
			return "font-style:italic";
		}
		return "";
	}

	private static short getHTMLFontBoldweight(Workbook wb, CellStyle cs) {
		return wb.getFontAt(cs.getFontIndex()).getBoldweight();
	}

	private static Object getHTMLFontSize(Workbook wb, CellStyle cs) {
		return wb.getFontAt(cs.getFontIndex()).getFontHeightInPoints();
	}

	private static Object getHTMLFontName(Workbook wb, CellStyle cs) {
		Object obj = wb.getFontAt(cs.getFontIndex()).getFontName();
		return obj;
	}

	private static Object getHTMLFontColor(Workbook wb, CellStyle cs) {
		return getHTMLColor(((HSSFColor) getFontColor(wb, cs)).getTriplet());
	}

	private static Object getFontColor(Workbook wb, CellStyle cs) {
		return HSSFColor.getIndexHash().get(new Integer(wb.getFontAt(cs.getFontIndex()).getColor()));
	}

	private static Object getBGColor(CellStyle cs) {
		return HSSFColor.getIndexHash().get(new Integer(cs.getFillForegroundColor()));
	}

	private static String getHTMLBGColor(CellStyle cs) {
		Color c = cs.getFillForegroundColorColor();// getFillBackgroundColorColor();
		if (c instanceof HSSFColor) {
			return getHTMLColor(((HSSFColor) c).getTriplet());
		}
		// 07版本的需要导入一些包暂时不考虑此版本
		// if(c instanceof XSSFColor){
		// return ((XSSFColor)c).getARGBHex();
		// }
		return "white";  //默认白色背景
	}

	private static String getHTMLColor(short[] rgb) {
		return "RGB(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + ")";
	}

	/**
	 * 获取真实行高，excel列高单位为磅，poi列高单位为缇，1磅=0.353毫米=20缇
	 * 
	 * @param row
	 * @return
	 */
	public static int getRowHeight(Row row) {
		int h = row.getHeight();
		if (h < 0) {
			return DEFAULT_HEIGHT;
		}
		return (int) (h / HEIGHT_POINT) - 1;
	}

	/**
	 * 获取真实列宽
	 * 
	 * @param sheet
	 * @param c
	 * @return
	 */
	public static int getColWidth(Sheet sheet, int c) {
		double w = sheet.getColumnWidth(c) / WIDTH_POINT;
		// excel默认单元格宽度为8.3，如果poi读取宽度为8对应像素为72
		if ((8 - w) == 0) {
			return DEFAULT_WIDTH;
		}
		if (w < 8) {
			return (int) Math.round((w - REVISE_WIDTH) * WIDTH_MULITIPLE);
		} else {
			return (int) Math.round((w - REVISE_WIDTH) * WIDTH_MULITIPLE) - 30;
		}
	}

	/**
	 * 获取table真实宽度
	 * 
	 * @param sheet
	 * @param maxColsRowNum
	 * @return
	 */
	private static int getWidth(Sheet sheet, int maxColsRowNum) {
		double rw = 0;
		Row row = sheet.getRow(maxColsRowNum);
		for (int c = 0; c < row.getLastCellNum(); c++) {
			double w = sheet.getColumnWidth(c) / WIDTH_POINT;
			// excel默认单元格宽度为8.3，如果poi读取宽度为8对应像素为72
			if ((8 - w) == 0) {
				rw += DEFAULT_WIDTH;
			}
			if (w < 8) {
				rw += (int) Math.round((w - REVISE_WIDTH) * WIDTH_MULITIPLE);
			} else {
				rw += (int) Math.round((w - REVISE_WIDTH) * WIDTH_MULITIPLE) - 30;
			}
		}
		return (int) rw;
	}

	public static int getAnchorX(int px, int colWidth) {
		return (int) Math.round(((double) 701 * 16000.0 / 301) * ((double) 1 / colWidth) * px);
	}

	public static int getAnchorY(int px, int rowHeight) {
		return (int) Math.round(((double) 144 * 8000 / 301) * ((double) 1 / rowHeight) * px);
	}

	public static int getRowHeight(int px) {
		return (int) Math.round(((double) 4480 / 300) * px);
	}

	public static int getColWidth(int px) {
		return (int) Math.round(((double) 10971 / 300) * px);
	}

	/**
	 * <p>
	 * 方法名称: copySheet|描述: 插入sheet（由source读出sheet数据插入target中）。
	 * </p>
	 * 
	 * @param target
	 *            待写Workbook
	 * @param source
	 *            数据Workbook
	 * @return Workbook
	 */
	public static Workbook copySheet(Workbook target, Workbook source) {
		if (null == target)
			target = new HSSFWorkbook();
		Font[] fs = new Font[source.getNumberOfFonts() + 1];// 字体复制
		for (short i = 0; i <= source.getNumberOfFonts(); i++) {
			Font childFont = source.getFontAt(i);
			Font font = target.createFont();
			BeanUtils.copyProperties(childFont, font);
			fs[i] = font;
		}
		CellStyle[] css = new CellStyle[source.getNumCellStyles() + 1];// 样式复制
		for (short i = 0; i < source.getNumCellStyles(); i++) {
			CellStyle childCs = source.getCellStyleAt(i);
			CellStyle cs = target.createCellStyle();
			copyCellStyle(childCs, cs);
			if (fs[childCs.getFontIndex()] != null) {
				cs.setFont(fs[childCs.getFontIndex()]);
			}
			css[i] = cs;
		}
		for (int i = 0; i < source.getNumberOfSheets(); i++) {
			Sheet childSheet = source.getSheetAt(i);
			Sheet sheet = target.createSheet(source.getSheetName(i));
			if (null != childSheet.getRowBreaks()) {// 复制边框线
				int rowBreaks[] = childSheet.getRowBreaks();
				for (int j = 0; j < rowBreaks.length; j++) {
					sheet.setRowBreak(rowBreaks[j]);
				}
			}
			if (null != childSheet.getColumnBreaks()) {// 复制边框线
				int[] colBreaks = childSheet.getColumnBreaks();
				for (short j = 0; j < colBreaks.length; j++) {
					sheet.setColumnBreak((short) colBreaks[j]);
				}
			}
			PrintSetup childPs = childSheet.getPrintSetup();// 复制打印设置对象
			PrintSetup ps = sheet.getPrintSetup();
			BeanUtils.copyProperties(childPs, ps);
			List<CellRangeAddress> rlist = new ArrayList<CellRangeAddress>();

			for (int r = 0; r < childSheet.getLastRowNum() + 1; r++) {
				Row row = childSheet.getRow(r);
				Row row2 = sheet.createRow(r);
				if (row != null) {
					row2.setHeight(row.getHeight());// 复制高度
				}
				for (int c = 0; row != null && c < row.getLastCellNum() + 1; c++) {
					if (r <= 1 && childSheet.getColumnWidth((short) c) != childSheet.getDefaultColumnWidth()) {// 复制宽度
						sheet.setColumnWidth((short) c, childSheet.getColumnWidth((short) c));
					}

					CellRangeAddress regionBeforMeger = removeRegion(childSheet, row.getRowNum(), (short) c);
					if (null != regionBeforMeger) {// 复制布局
						if (!rlist.contains(regionBeforMeger))
							rlist.add(regionBeforMeger);
					}

					Cell col = row.getCell((short) c);
					if (col != null) {
						Cell c2 = row2.createCell((short) c);
						c2.setCellStyle(css[col.getCellStyle().getIndex()]);
						switch (col.getCellType()) {// 复制值与格式
						case HSSFCell.CELL_TYPE_NUMERIC:
							c2.setCellValue(col.getNumericCellValue());
							copyDataFormat(source, target, col, c2);
							break;
						case HSSFCell.CELL_TYPE_STRING:
							c2.setCellValue(col.getRichStringCellValue());
							break;
						case HSSFCell.CELL_TYPE_BOOLEAN:
							c2.setCellValue(col.getBooleanCellValue());
							break;
						case HSSFCell.CELL_TYPE_ERROR:
							c2.setCellValue(col.getErrorCellValue());
							break;
						case HSSFCell.CELL_TYPE_FORMULA:
							copyDataFormat(source, target, col, c2);
							break;
						default:
							copyDataFormat(source, target, col, c2);
							break;
						}
					}
				}
			}
			for (int j = 0; j < rlist.size(); j++) {// 合并单元格
				sheet.addMergedRegion(rlist.get(j));
			}
		}
		return target;
	}

	public static void copyCellStyle(CellStyle fromStyle, CellStyle toStyle) {
		toStyle.setAlignment(fromStyle.getAlignment());
		// 边框和边框颜色
		toStyle.setBorderBottom(fromStyle.getBorderBottom());
		toStyle.setBorderLeft(fromStyle.getBorderLeft());
		toStyle.setBorderRight(fromStyle.getBorderRight());
		toStyle.setBorderTop(fromStyle.getBorderTop());
		toStyle.setTopBorderColor(fromStyle.getTopBorderColor());
		toStyle.setBottomBorderColor(fromStyle.getBottomBorderColor());
		toStyle.setRightBorderColor(fromStyle.getRightBorderColor());
		toStyle.setLeftBorderColor(fromStyle.getLeftBorderColor());

		// 背景和前景
		toStyle.setFillBackgroundColor(fromStyle.getFillBackgroundColor());
		toStyle.setFillForegroundColor(fromStyle.getFillForegroundColor());

		toStyle.setDataFormat(fromStyle.getDataFormat());
		toStyle.setFillPattern(fromStyle.getFillPattern());
		toStyle.setHidden(fromStyle.getHidden());
		toStyle.setIndention(fromStyle.getIndention());// 首行缩进
		toStyle.setLocked(fromStyle.getLocked());
		toStyle.setRotation(fromStyle.getRotation());// 旋转
		toStyle.setVerticalAlignment(fromStyle.getVerticalAlignment());
		toStyle.setWrapText(fromStyle.getWrapText());

	}

	public static CellRangeAddress removeRegion(Sheet sheet, int r, short c) {
		for (int m = 0; m < sheet.getNumMergedRegions(); m++) {
			CellRangeAddress region = sheet.getMergedRegion(m);
			if (containRegion(region, r, c)) {
				CellRangeAddress regionToReturn = new CellRangeAddress(region.getFirstRow(), region.getLastRow(), region.getFirstColumn(),
						region.getLastColumn());
				sheet.removeMergedRegion(m);
				return regionToReturn;
			}
		}
		return null;
	}

	private static boolean containRegion(CellRangeAddress region, int r, short c) {
		return (region.getFirstRow() <= r && region.getLastRow() >= r && region.getFirstColumn() <= c && region.getLastColumn() >= c);
	}

	private static void copyDataFormat(Workbook wb, Workbook wb2, Cell c, Cell c2) {
		if (c.getCellStyle() != null) {
			String hdf = wb.createDataFormat().getFormat(c.getCellStyle().getDataFormat());
			try {
				short hdf2 = wb2.createDataFormat().getFormat(hdf);
				c2.getCellStyle().setDataFormat(hdf2);
			} catch (NullPointerException e) {
				c2.getCellStyle().setDataFormat((short) 0);
			}
		}
	}

	public static String getWorkbookFileType(Workbook w) {
		if (w == null) {
			return null;
		}
		if (w.getClass().getSimpleName().startsWith("XSSF")) {
			return "xlsx";
		} else {
			return "xls";
		}
	}

}
