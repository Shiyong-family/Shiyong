package srp.bapp.utils;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jndi.JndiObjectFactoryBean;

public final class JndiUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JndiUtil.class);

	private JndiUtil() {
		// 私有化构造函数，防止有人使用实例
	}

	public static DataSource getDataSource(String jndi) {
	
		
		try{
			JndiObjectFactoryBean jndiObjectFactoryBean=new JndiObjectFactoryBean();
			jndiObjectFactoryBean.setJndiName(jndi);
			jndiObjectFactoryBean.afterPropertiesSet();
			DataSource ds=(DataSource) jndiObjectFactoryBean.getObject();
			if(ds!=null){
				return ds;
			}
		}catch(Exception e){
			logger.error("getDataSource(String) - jndi=" + jndi, e); //$NON-NLS-1$
		}
		
		try{
			JndiObjectFactoryBean jndiObjectFactoryBean=new JndiObjectFactoryBean();
			jndiObjectFactoryBean.setJndiName(jndi);
			jndiObjectFactoryBean.setResourceRef(true);
			jndiObjectFactoryBean.afterPropertiesSet();
			DataSource ds=(DataSource) jndiObjectFactoryBean.getObject();
			if(ds!=null){
				return ds;
			}
		}catch(Exception e){
			logger.error("getDataSource(String)  setResourceRef(true)- jndi=" + jndi, e); //$NON-NLS-1$
		}
		
		logger.error("不能获取JNDI连接  getDataSource(String) - jndi=" + jndi);
		return null;

	}

}
