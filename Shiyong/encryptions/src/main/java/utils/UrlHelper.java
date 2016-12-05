package srp.bapp.utils;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import srp.bapp.httpclient.HttpEntity;
import srp.bapp.httpclient.client.config.RequestConfig;
import srp.bapp.httpclient.client.methods.CloseableHttpResponse;
import srp.bapp.httpclient.client.methods.HttpGet;
import srp.bapp.httpclient.client.methods.HttpPost;
import srp.bapp.httpclient.entity.ByteArrayEntity;
import srp.bapp.httpclient.impl.client.CloseableHttpClient;
import srp.bapp.httpclient.impl.client.HttpClients;
import srp.bapp.httpclient.util.EntityUtils;

/**
 * 定义了一些与Url处理有关的公共函数
 * 
 * @author oofrank
 * 
 */
public abstract class UrlHelper {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(UrlHelper.class);

	/**
	 * 判断是否是http或htts开头的字符串
	 * 
	 * @param source
	 * @return
	 */
	public static boolean isHttpCallUrl(String source) {
		if (StringUtil.isEmpty(source)) {
			return false;
		}
		return source.startsWith("http://") || source.startsWith("https://") || source.startsWith("$");
	}

	/**
	 * 获取j2ee-web应用的完整路径
	 * 如：http://www.java3z.com/cwbwebhome/article/article8/81313.html?id=3138
	 * 返回：http://www.java3z.com/cwbwebhome
	 * 
	 * @param requestUrl
	 * @return
	 */
	private static Pattern pGetWebAppfullPath = Pattern.compile("(http.+//.+/.+)/");

	public static String getWebAppfullPath(String requestUrl) {
		String r = null;
		Matcher m = pGetWebAppfullPath.matcher(requestUrl);
		if (m.find()) {
			r = m.group(1);
		} else {
			throw new IllegalArgumentException("格式错误的requestUrl");
		}
		return r;
	}

	/**
	 * 返回url地址的动态返回内容
	 */
	public static String getUrlContent(String path) {
		String content = null;
		CloseableHttpClient hc = null;
		CloseableHttpResponse response = null;
		try {
			logger.debug("访问地址：" + path);
			hc = HttpClients.createDefault();
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(5000).setConnectTimeout(5000).build();//设置请求和传输超时时间为5秒
			HttpGet m = new HttpGet(path);
			m.setConfig(requestConfig);
			response = hc.execute(m);
			HttpEntity entity = response.getEntity();
			content = EntityUtils.toString(entity, "UTF-8");
		} catch (Exception e) {
			logger.error("getUrlContent(String) - path:" + path, e); //$NON-NLS-1$
		} finally {
			if (null != response) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error("getUrlContent(String) - path:" + path, e); //$NON-NLS-1$
				}
			}
			if (null != hc) {
				try {
					hc.close();
				} catch (IOException e) {
					logger.error("getUrlContent(String) - path:" + path, e); //$NON-NLS-1$
				}
			}
		}
		return content;
	}

	public static String getPostUrlContent(String path, String xmlData) {
		return getPostUrlContent(path, xmlData, 5000);
	}

	public static String getPostUrlContent(String path, String xmlData, int timeout) {
		String content = null;
		CloseableHttpClient hc = null;
		CloseableHttpResponse response = null;
		try {
			logger.debug("访问地址：" + path);
			hc = HttpClients.createDefault();
			RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(timeout).setConnectTimeout(timeout).build();//设置请求和传输超时时间为5秒
			HttpPost post = new HttpPost(path);
			HttpEntity entity = new ByteArrayEntity(xmlData.getBytes("utf-8"));
			post.setEntity(entity);
			post.setConfig(requestConfig);
			response = hc.execute(post);
			HttpEntity respEntity = response.getEntity();
			content = EntityUtils.toString(respEntity, "utf-8");
		} catch (Exception e) {
			logger.error("getUrlContent(String) - path:" + path, e); //$NON-NLS-1$
		} finally {
			if (null != response) {
				try {
					response.close();
				} catch (IOException e) {
					logger.error("getUrlContent(String) - path:" + path, e); //$NON-NLS-1$
				}
			}
			if (null != hc) {
				try {
					hc.close();
				} catch (IOException e) {
					logger.error("getUrlContent(String) - path:" + path, e); //$NON-NLS-1$
				}
			}
		}
		return content;
	}

	public static void main(String[] args) {
		//String context = getUrlContent("http://www.baidu.com");//10.2.163.186

		//http://127.0.0.1:8080/bapp/makeTemplateView.do?f=*.FOX0101&ssId=QC0201&templateId=1111
		String context = getPostUrlContent("http://127.0.0.1:8080/bapp/verification.html",getXMLString());
		System.out.println(context);
	}

	private static String getXMLString() {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<reqt>");
		sb.append("<appHdr><verifyType>qc0004</verifyType><bsnCode>40033</bsnCode><channel>2222222</channel><reqDate>3333</reqDate><reqTime>4444444</reqTime></appHdr>");
		sb.append("<appBody>");
		sb.append("<account>");
        sb.append("<acctNbr1>6231700180008995068</acctNbr1>");
        sb.append("<accName1>55555555</accName1>");
        sb.append("<acctNbr2>6231700180008995068</acctNbr2>");
        sb.append("<accName2>55555555</accName2>");
        sb.append("<transAction>");
        
        sb.append("<featureCode>1111</featureCode>");
        sb.append("<id>1</id>");
        sb.append("<time>2</time>");
        sb.append("<type>3</type>");
        sb.append("<currency>4</currency>");
        sb.append("<transferAmount>5</transferAmount>");
        sb.append("<transferOutBankID>6</transferOutBankID>");
        sb.append("<transferOutBankName>7</transferOutBankName>");
        
        sb.append("<transferOutAccountName>10</transferOutAccountName>");
        sb.append("<transferOutAccountNumber>11</transferOutAccountNumber>");
        sb.append("<opp_Isparty>1</opp_Isparty>");
        sb.append("<transferInBankID>13</transferInBankID>");
        sb.append("<transferInBankName>14</transferInBankName>");
        sb.append("<transferInAccountName>15</transferInAccountName>");
        sb.append("<isparty>1</isparty>");
        sb.append("<tx_cd>17</tx_cd>");
        sb.append("<status>18</status>");
        sb.append("<organKey>19</organKey>");
        sb.append("<organName>20</organName>");
        sb.append("<ip>21</ip>");
        sb.append("<mac>22</mac>");
        sb.append("<deviceId>23</deviceId>");
        sb.append("<place>24</place>");
        sb.append("<remark>25</remark>");
        
        sb.append("</transAction>");
		sb.append("</account>");
		sb.append("</appBody>");
		sb.append("</reqt>");
		return sb.toString();
	}

}
