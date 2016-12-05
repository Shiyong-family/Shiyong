package srp.bapp.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.sql.DataSource;
import javax.sql.rowset.serial.SerialClob;

import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import srp.bapp.pub.runtime.DBType;
import srp.bapp.subsystem.s.JdbcSupportDAO;
import srp.bapp.tools.DatabaseInfo;
import srp.bapp.tools.OutputStreamWriter;
import srp.bapp.utils.third.Base64;

public class JdbcUtil {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger.getLogger(JdbcUtil.class);

	/**
	 * 这是给makeSqlCondition()方法用的
	 */
	private static HashMap<String, String> operatorMap = new HashMap<String, String>();
	static {
		operatorMap.put("eq", "=");
		operatorMap.put("gt", ">");
		operatorMap.put("lt", "<");
		operatorMap.put("ge", ">=");
		operatorMap.put("le", "<=");
		operatorMap.put("ne", "<>");
		operatorMap.put("like", " like ");
		operatorMap.put("in", " in ");
	}

	/**
	 * oracle中導入模板
	 * 
	 * @param tableName
	 * @param filedName
	 * @param id
	 * @param t
	 * @throws SQLException
	 */
	public static int blobUpdate(DataSource ds, String tableName, String filedName, String whereStr, final InputStream in) throws SQLException {
		if (JdbcSupportDAO.getDbType().equals(DBType.oracle)) {
			return blobUpdateForOracle(ds, tableName, filedName, whereStr, new OutputStreamWriter() {
				public void write(OutputStream outputStream) throws IOException {
					IOUtil.copy(in, outputStream);
				}
			});
		} else {
			return blobUpdate4Other(ds, tableName, filedName, whereStr, in);
		}
	}

	private static int blobUpdate4Other(DataSource ds, String tableName, String filedName, String whereStr, InputStream in) throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdate(String, String, String, String, byte[]) - start"); //$NON-NLS-1$
		}

		String updateSql = "UPDATE " + tableName + " SET " + filedName + "=? WHERE " + whereStr;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		int result = -1;
		Connection cn = null;
		boolean defaultCommit = true;
		try {
			cn = ds.getConnection();
			defaultCommit = cn.getAutoCommit();
			cn.setAutoCommit(false);
			stmt = cn.prepareStatement(updateSql);
			stmt.setBinaryStream(1, in, in.available());
			result = stmt.executeUpdate();
			cn.commit();

		} catch (Exception e) {
			logger.warn("blobUpdate:" + whereStr, e);
			throw new SQLException("更新失败:" + e.getMessage());
		} finally {

			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					logger.error("blobUpdate", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("blobUpdate", e);
				}
			}
			if (cn != null) {
				try {
					cn.setAutoCommit(defaultCommit);
				} catch (Exception e) {
					logger.error("blobUpdate", e);
				}
				try {
					cn.close();
				} catch (Exception e) {
					logger.error("blobUpdate", e);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdate(String, String, String, String, byte[]) - end"); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * oracle中導入模板
	 * 
	 * @param tableName
	 * @param filedName
	 * @param id
	 * @param t
	 * @throws SQLException
	 */
	public static int clobUpdateForOracle(DataSource ds, String tableName, String filedName, String whereStr, final InputStream in)
			throws SQLException {
		return clobUpdateForOracle(ds, tableName, filedName, whereStr, new OutputStreamWriter() {
			public void write(OutputStream outputStream) throws IOException {
				IOUtil.copy(in, outputStream);
			}
		});
	}

	/**
	 * oracle中導入模板
	 * 
	 * @param tableName
	 * @param filedName
	 * @param id
	 * @param t
	 * @throws SQLException
	 */
	public static int clobUpdateForOracle(DataSource ds, String tableName, String filedName, String whereStr, OutputStreamWriter outputWriter)
			throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdateForOracle(String, String, String, String, byte[]) - start"); //$NON-NLS-1$
		}

		String selectSql = "select " + filedName + " from " + tableName + " where " + whereStr + " for update";
		String updateSql = "UPDATE " + tableName + " SET " + filedName + "=EMPTY_CLOB() WHERE " + whereStr;
		Statement stmt = null;
		ResultSet rs = null;
		int result = -1;
		Connection cn = null;
		try {
			cn = ds.getConnection();
			boolean defaultCommit = cn.getAutoCommit();
			cn.setAutoCommit(false);
			stmt = cn.createStatement();
			result = stmt.executeUpdate(updateSql);
			rs = stmt.executeQuery(selectSql);
			while (rs.next()) {
				BufferedOutputStream out = null;
				try {
					Clob clob = rs.getClob(1);
					out = new BufferedOutputStream(clob.setAsciiStream(1));
					outputWriter.write(out);
					try {
						out.close();
					} catch (Exception e) {
						logger.warn("blobUpdateForOracle:out.close", e);
					}
					cn.commit();
				} catch (Exception e) {
					cn.rollback();
					logger.warn("blobUpdateForOracle:", e);
					throw new SQLException("更新失败:" + e.getMessage());
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (Exception e) {
							// logger.warn("blobUpdateForOracle:out.close", e);
						}
					}
				}
			}
			cn.setAutoCommit(defaultCommit);
		} catch (Exception e) {
			logger.warn("blobUpdateForOracle:" + whereStr, e);
			throw new SQLException("更新失败:" + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
			if (cn != null) {
				try {
					cn.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdateForOracle(String, String, String, String, byte[]) - end"); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * oracle中導入模板
	 * 
	 * @param tableName
	 * @param filedName
	 * @param id
	 * @param t
	 * @throws SQLException
	 */
	public static int blobUpdateForOracle(DataSource ds, String tableName, String filedName, String whereStr, OutputStreamWriter outputWriter)
			throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdateForOracle(String, String, String, String, byte[]) - start"); //$NON-NLS-1$
		}

		String selectSql = "select " + filedName + " from " + tableName + " where " + whereStr + " for update";
		String updateSql = "UPDATE " + tableName + " SET " + filedName + "=EMPTY_BLOB() WHERE " + whereStr;
		Statement stmt = null;
		ResultSet rs = null;
		int result = -1;
		Connection cn = null;
		try {
			cn = ds.getConnection();
			boolean defaultCommit = cn.getAutoCommit();
			cn.setAutoCommit(false);
			stmt = cn.createStatement();
			result = stmt.executeUpdate(updateSql);
			rs = stmt.executeQuery(selectSql);
			while (rs.next()) {
				BufferedOutputStream out = null;
				try {
					Blob blob = rs.getBlob(1);
					out = new BufferedOutputStream(blob.setBinaryStream(1));
					outputWriter.write(out);
					try {
						out.close();
					} catch (Exception e) {
						logger.warn("blobUpdateForOracle:out.close", e);
					}
					cn.commit();
				} catch (Exception e) {
					cn.rollback();
					logger.warn("blobUpdateForOracle:", e);
					throw new SQLException("更新失败:" + e.getMessage());
				} finally {
					if (out != null) {
						try {
							out.close();
						} catch (Exception e) {
							// logger.warn("blobUpdateForOracle:out.close", e);
						}
					}
				}
			}
			cn.setAutoCommit(defaultCommit);
		} catch (Exception e) {
			logger.warn("blobUpdateForOracle:" + whereStr, e);
			throw new SQLException("更新失败:" + e.getMessage());
		} finally {
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
			if (cn != null) {
				try {
					cn.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdateForOracle(String, String, String, String, byte[]) - end"); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * 
	 * @param ds
	 * @return
	 */
	public static final DatabaseInfo getDatabaseInfo(DataSource ds) {
		Connection cn = null;
		try {
			DatabaseInfo db = new DatabaseInfo();
			cn = ds.getConnection();
			String pn = cn.getMetaData().getDatabaseProductName();
			if (pn.toLowerCase().contains("oracle")) {
				db.setDbType(DBType.oracle);
				db.setSchema(cn.getMetaData().getUserName());
			} else if (pn.toLowerCase().contains("db2")) {
				db.setDbType(DBType.db2);
				db.setSchema(cn.getMetaData().getUserName());
			} else if (pn.toLowerCase().contains("mysql")) {
				db.setDbType(DBType.mysql);
				db.setSchema(cn.getCatalog());
			} else {
				throw new RuntimeException("未考虑的数据库类型:" + pn);
			}
			try {
				db.setCatalog(cn.getCatalog());
				db.setUrl(cn.getMetaData().getURL());
				db.setUserName(cn.getMetaData().getUserName());
				db.setProductName(cn.getMetaData().getDatabaseProductName());
				db.setProductVersion(cn.getMetaData().getDatabaseProductVersion());
			} catch (Exception e) {
				logger.error("getDatabaseInfo(DataSource) - can't get DatabaseInfo! ", e); //$NON-NLS-1$
			}
			return db;
		} catch (SQLException e) {
			logger.error("getDatabaseInfo(DataSource) - can't get DatabaseInfo! ", e); //$NON-NLS-1$
			throw new RuntimeException("数据库连接异常");
		} finally {
			if (cn != null) {
				try {
					cn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	public static Map<String, Object> getPagingConditonResult(int start, int limit, Map<String, String> conditionMap, String infosSql, String totalSql) {
		String sql = makeSqlCondition(conditionMap, infosSql);
		totalSql = makeSqlCondition(conditionMap, totalSql);
		String pageingSql = getPagingSQL(JdbcSupportDAO.getDbType(), sql, start, limit);
		List<Map<String, Object>> infos = JdbcSupportDAO.getJdbcTemplate().queryForList(pageingSql);
		int total = JdbcSupportDAO.getJdbcTemplate().queryForObject(totalSql, Integer.class);
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("infos", infos);
		map.put("total", total);
		return map;
	}

	/**
	 * 返回分页的SQL语句<BR/>
	 * 注：DB2 不支持预编译的分页查询
	 * 
	 * @param sql
	 * @return
	 */
	private static String getPagingSQL(DBType dbType, String sql, String offset, String limit) {
		StringBuilder pagingSelect = new StringBuilder(sql.length() + 100);
		if (dbType.equals(DBType.oracle)) {
			pagingSelect.append("select * from ( select row_.*, rownum rownum_ from ( ");
			pagingSelect.append(sql);
			pagingSelect.append(" ) row_  ) where rownum_ > ").append(offset).append(" and rownum <= ").append(limit);
		} else if (dbType.equals(DBType.db2)) {
			if ("?".equals(offset)) {
				throw new RuntimeException("DB2 不支持预编译的分页查询");
			}
			int o = Integer.parseInt(offset);// 起始行
			int l = Integer.parseInt(limit);// 查询数量
			int c = o + l;

			pagingSelect.append("select * from ( select inner2_.*, rownumber() over(order by order of inner2_) as rownumber_ from ( ").append(sql)
					.append(" fetch first ").append(c).append(" rows only ) as inner2_ ) as inner1_ where rownumber_ > ").append(offset)
					.append(" order by rownumber_");
		} else if (dbType.equals(DBType.mysql)) {
			pagingSelect.append(sql);

			if (Integer.parseInt(offset) <= 0) {
				if (Long.parseLong(limit) >= Integer.MAX_VALUE) { // 读取所有，不需要Limit.

				} else {
					pagingSelect.append(" limit ").append(limit);
				}
			} else {
				pagingSelect.append(" limit ").append((offset)).append(", ").append(limit);
			}
		} else if (dbType.equals(DBType.teradata)) {
			pagingSelect.append("select * from ( select row_.*, trim(ROW_NUMBER()OVER(ORDER BY '123')) as rownum_ from ( ");
			pagingSelect.append(sql);
			pagingSelect.append(" ) row_  ) aa where rownum_ > ").append(offset).append(" and rownum_ <= ").append(limit);
		}
		return pagingSelect.toString();
	}

	/**
	 * 返回分页的SQL语句
	 */
	public static String getPagingSQL(DBType dbType, String sql, int offset, int limit) {
		return getPagingSQL(dbType, sql, String.valueOf(offset), String.valueOf(limit));
	}

	/**
	 * 返回分页的SQL语句,以两个？作为offset，limit填充，以便使用预编译;暂时仅支持Oracle
	 */
	public static String getPagingSQL(DBType dbType, String sql) {
		return getPagingSQL(dbType, sql, "?", "?");
	}

	/**
	 * 将形如f.process_Buseness_Class:xx的字符串作为key填充入map中，其中xx的取值为eq,gt,lt,ge,le,ne,
	 * like,in。
	 * 其中f.n.表示map的value需要过滤(f:字符串类型,n:数值类型)，eq表示查询操作符为=，gt为>,lt为<,ge为>=,le为
	 * <=,ne为<>,like为like,in为in。 如果key以eq结尾并且value以%开头或结尾，执行like操作符
	 * 如果key以like结尾并且value以=开头，执行=操作 最后将查询结果返回<BR>
	 * 注:在source中使用${condition}替代符可以指定条件位置,以支持ORDER-BY,GROUP-BY
	 * 
	 * @param conditionMap
	 * @param source
	 * @return
	 */
	public static String makeSqlCondition(Map<String, String> conditionMap, String source) {
		return makeSqlCondition(conditionMap, source, true);
	}

	/**
	 * 功能与makeSqlCondition相同，额外增加自动添加where判断
	 * 
	 * @param conditionMap
	 * @param source
	 * @return
	 */
	public static String makeSqlCondition(Map<String, String> conditionMap, String source, boolean hasWhere) {
		StringBuffer sb = null;
		if (hasWhere) {
			sb = makeSqlCondition(conditionMap);
		} else {
			sb = getSqlConditionWithWhere(conditionMap);
		}
		if (source.indexOf("${condition}") > -1) {
			String s = source.substring(0, source.indexOf("${condition}"));
			String e = source.substring(source.indexOf("${condition}") + "${condition}".length());
			sb.insert(0, s);
			sb.append(" ").append(e);
		} else {
			sb.insert(0, source);
		}
		return sb.toString();
	}

	/**
	 * 有条件自动增加where，没有条件反馈空
	 * 
	 * @param conditionMap
	 * @return
	 */
	private static StringBuffer getSqlConditionWithWhere(Map<String, String> conditionMap) {
		StringBuffer sb = makeSqlCondition(conditionMap);
		if (sb.length() > 0) {
			return sb.replace(0, 4, " where "); // first ' and '
		}
		return sb;
	}

	private static StringBuffer makeSqlCondition(Map<String, String> conditionMap) {
		StringBuffer sb = new StringBuffer();

		for (Iterator<String> i = conditionMap.keySet().iterator(); i.hasNext();) {
			String key = i.next();
			String value = null;
			String vi = String.valueOf(conditionMap.get(key));
			boolean isNull = false;
			if (vi != null) {
				value = vi.trim();
				if (vi.length() > 0 && value.length() == 0) {
					isNull = true;
				}
			}
			if (key.startsWith("f.") && (!StringUtil.isEmpty(value) || isNull)) {
				// 字符串：
				if (value.indexOf('\'') > -1) {
					value = value.replaceAll("\'", "\''");
				}

				// 字段名
				String fieldName = getFieldName(key);
				sb.append(" and ");
				if (isNull) {
					sb.append("(").append(fieldName).append(" IS NULL OR LENGTH(RTRIM(").append(fieldName).append("))=0)");
					continue;
				} else {
					sb.append("(").append(fieldName);
				}

				// > < >= <= = <>...>>>>:eq,like,ge,le,in
				String operator = getOperator(key);
				if (operator.equals("in") || value.indexOf(',') > 0) {
					sb.append(" in ");
					sb.append("(");
					String[] vs = vi.split(",");
					for (int v = 0; v < vs.length; v++) {
						if (vs[v].indexOf('\'') > -1) {
							vs[v] = vs[v].replaceAll("\'", "");
						}
						sb.append('\'');
						sb.append(vs[v].trim());
						sb.append('\'');
						if (v < vs.length - 1) {
							sb.append(',');
						}
					}
					sb.append(")").append(")");
				} else {
					if (operator.equals("eq") && (value.startsWith("%") || value.endsWith("%"))) {
						sb.append(" like ");
					} else if (operator.equals("like") && value.startsWith("=")) {
						sb.append("=");
						operator = "eq";
						value = value.substring(1).trim();
					} else if (value.startsWith(">") || value.startsWith("<")) {
						if (value.length() == 1 || (value.charAt(1) == '=' && value.length() == 2)) {
							continue;
						}
						if (value.contains("&&")) {
							String[] vs = value.split("&&");
							for (int v = 0; v < vs.length; v++) {
								vs[v] = vs[v].trim();
								if (v != 0) {
									sb.append(fieldName);
								}
								if (vs[v].startsWith(">=") || vs[v].startsWith("<=")) {
									operator = vs[v].substring(0, 2);
									sb.append(operator);
									if (vs[v].length() > 2) {
										sb.append('\'').append(vs[v].substring(2)).append('\'');
									} else {
										sb.append("''");
									}
								} else if (vs[v].startsWith(">") || vs[v].startsWith("<")) {
									operator = vs[v].substring(0, 1);
									sb.append(operator);
									if (vs[v].length() > 1) {
										sb.append('\'').append(vs[v].substring(1)).append('\'');
									} else {
										sb.append("''");
									}
								} else {
									int x = value.charAt(2) == '=' ? 2 : 1;
									operator = value.substring(0, x);
									if (operator.equals(">")) {
										operator = "<";
									} else if (operator.equals("<")) {
										operator = ">";
									} else if (operator.equals("<=")) {
										operator = ">=";
									} else if (operator.equals(">=")) {
										operator = "<=";
									}
									sb.append(operator);
									sb.append('\'').append(vs[v]).append('\'');
								}
								if (v < vs.length - 1) {
									sb.append(" AND ");
								} else {
									sb.append(")");
								}
							}
							continue;
						} else {
							int x = value.charAt(2) == '=' ? 2 : 1;
							operator = value.substring(0, x);
							sb.append(operator);
							value = value.substring(x);
						}

					} else {
						sb.append(operatorMap.get(operator));
					}
					sb.append("'");
					if (operator.equals("like")) {
						sb.append("%").append(value).append("%");
					} else {
						sb.append(value);
					}
					sb.append("')");
				}
			}
			if (key.startsWith("n.") && !StringUtil.isEmpty(value)) {
				value = value.replaceAll(" ", "");
				String fieldName = getFieldName(key);
				String operator = getOperator(key);
				if (StringUtil.isNumeric(value)) {
					sb.append(" and ").append(fieldName);
					sb.append(operatorMap.get(operator));
					sb.append(value);
				} else {
					operator = value.replaceAll("[\\d\\.]", "");
					value = value.replaceAll("[\\>\\<\\=]", "");
					if (operator.length() > 0 && operatorMap.containsValue(operator) && operator.length() <= 2 && StringUtil.isNumeric(value)) {
						sb.append(" and ").append(fieldName);
						sb.append(operator);
						sb.append(value);
					}
				}
			}
		}
		return sb;
	}

	private static String getOperator(String key) {
		String operator = key.substring(key.indexOf(':') + 1);
		return operator;
	}

	private static String getFieldName(String key) {
		String fieldName = key.substring(key.indexOf('.') + 1, key.indexOf(':'));
		return fieldName;
	}

	/**
	 * 简单返回ListList
	 * 
	 * @param sql
	 * @param jdbcTemplate
	 * @return
	 */
	public static List<List<String>> getListList(String sql, JdbcTemplate jdbcTemplate) {
		return jdbcTemplate.query(sql, new ResultSetExtractor<List<List<String>>>() {
			public List<List<String>> extractData(ResultSet rs) throws SQLException, DataAccessException {
				List<List<String>> l = new LinkedList<List<String>>();
				int cols = rs.getMetaData().getColumnCount();
				while (rs.next()) {
					List<String> r = new LinkedList<String>();
					for (int i = 1; i <= cols; i++) {
						r.add(rs.getString(i));
					}
					l.add(r);
				}
				return l;
			}
		});
	}

	/**
	 * 获取返回总记录数的SQL
	 * 
	 * @param sql
	 * @return
	 */
	public static String getCountSql(String sql) {
		return "select count(*) from (" + sql + ") t";
	}

	/**
	 * 返回总记录数
	 * 
	 * @param sql
	 * @return
	 */
	public static long getCount(String sql, JdbcTemplate jdbcTemplate, Object... args) {
		try {
			return jdbcTemplate.queryForObject(getCountSql(sql), Long.class, args).longValue();
		} catch (DataAccessException e) {
			logger.error("getCount(String, JdbcTemplate)", e); //$NON-NLS-1$
			return 0;
		}
	}

	public static boolean isStringType(int valueType) {
		return valueType == Types.CHAR || valueType == Types.VARCHAR || valueType == Types.NCHAR || valueType == Types.NVARCHAR
				|| valueType == Types.LONGVARCHAR || valueType == Types.LONGNVARCHAR;
	}

	public static boolean isIntType(int valueType) {
		return valueType == Types.BIGINT || valueType == Types.DECIMAL || valueType == Types.INTEGER || valueType == Types.NUMERIC
				|| valueType == Types.FLOAT || valueType == Types.REAL || valueType == Types.DOUBLE;
	}

	public static boolean isDateType(int valueType) {
		return valueType == Types.DATE;
	}

	public static String getNowFunction() {
		String nowFunction;
		if (DBType.db2.equals(JdbcSupportDAO.getDbType())) {
			nowFunction = "to_char(current timestamp,'yyyy-mm-dd hh24:mi:ss')"; // DB2-9
		} else if (DBType.mysql.equals(JdbcSupportDAO.getDbType())) {
			nowFunction = "concat( date_format(now(),'%Y-%m-%d ') , time_format(now(),'%H:%i:%S'))";
		} else { // Oracle
			nowFunction = "to_char(sysdate,'yyyy-MM-dd HH24:mi:ss')";
		}
		return nowFunction;
	}

	/**
	 * 自动匹配${name}变量批处理，此方法暂时只支持参数为String类型
	 * 
	 * @param jdbcTemplate
	 * @param sql
	 * @param values
	 */
	public static void batchUpdate(JdbcTemplate jdbcTemplate, String sql, final List<Map<String, String>> values) {
		final List<String> names = new ArrayList<String>();
		StringBuffer sb = new StringBuffer();
		Matcher m = StringUtil.pMakeTruthString.matcher(sql);
		while (m.find()) {
			names.add(m.group(1));
			m.appendReplacement(sb, "?");
		}
		m.appendTail(sb);
		jdbcTemplate.batchUpdate(sb.toString(), new BatchPreparedStatementSetter() {
			@Override
			public void setValues(PreparedStatement ps, int i) throws SQLException {
				Map<String, String> map = values.get(i);
				for (int a = 1; a <= names.size(); a++) {
					ps.setString(a, map.get(names.get(a - 1)));
				}
			}

			@Override
			public int getBatchSize() {
				return values.size();
			}
		});
	}

	/**
	 * 返回不同数据库获取表的SQL语句
	 */

	public static String getTablesSql(DBType dbType, String schema, String queryCondition) {
		String tablesSql = "";
		if (dbType.equals(DBType.mysql)) {
			tablesSql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='" + schema + "' AND TABLE_TYPE='BASE TABLE'"
					+ queryCondition + " ORDER BY TABLE_NAME";
		} else if (dbType.equals(DBType.oracle)) {
			tablesSql = "SELECT TABLE_NAME FROM USER_TABLES WHERE 1=1 " + queryCondition + " ORDER BY TABLE_NAME";
		} else if (dbType.equals(DBType.db2)) {
			tablesSql = "SELECT TABNAME AS TABLE_NAME FROM SYSCAT.TABLES WHERE UPPER(OWNER)='" + schema.toUpperCase() + "'" + queryCondition
					+ " ORDER BY TABNAME";
		}
		return tablesSql;
	}

	public static String getColumnsSql(DBType dbType, String schema, String tableName, String queryCondition) {
		String columnSql = "";
		if (dbType.equals(DBType.mysql)) {
			columnSql = "SELECT COLUMN_NAME,DATA_TYPE,CHARACTER_MAXIMUM_LENGTH as DATA_LENGTH,NUMERIC_PRECISION as DATA_PRECISION,NUMERIC_SCALE as DATA_SCALE,"
					+ "case when IS_NULLABLE='NO' then 0 "
					+ "when IS_NULLABLE='YES' then 1 "
					+ "end as NULLABLE,COLUMN_KEY,COLUMN_COMMENT "
					+ "FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='"
					+ schema
					+ "'and TABLE_NAME='"
					+ tableName.toLowerCase()
					+ "' ORDER BY ORDINAL_POSITION";
		} else if (dbType.equals(DBType.oracle)) {
			columnSql = "SELECT T1.COLUMN_NAME,DATA_TYPE,DATA_LENGTH,DATA_PRECISION,DATA_SCALE," + "case when NULLABLE='Y' then 1 "
					+ "when NULLABLE='N' then  0 " + "end as NULLABLE,COMMENTS as COLUMN_COMMENT FROM USER_TAB_COLUMNS T1  "
					+ "INNER JOIN USER_COL_COMMENTS T2 ON T1.TABLE_NAME=T2.TABLE_NAME and T1.COLUMN_NAME=T2.COLUMN_NAME WHERE 1=1 "
					+ "and T1.TABLE_NAME='" + tableName + "' ORDER BY COLUMN_ID";
		} else if (dbType.equals(DBType.db2)) {
			columnSql = "SELECT COLNAME AS COLUMN_NAME,TYPENAME AS DATA_TYPE,LENGTH AS 	DATA_LENGTH,LENGTH AS DATA_PRECISION,SCALE AS DATA_SCALE,"
					+ "case when NULLS='N' then 0 "
					+ "when NULLS='Y' then 1 end "
					+ "AS NULLABLE,c.REMARKS as COLUMN_COMMENT FROM SYSCAT.COLUMNS c inner join  SYSCAT.TABLES t on c.TABNAME=t.TABNAME WHERE OWNER='"
					+ schema.toUpperCase() + "'and t.TABNAME='" + tableName + "' ORDER BY COLNO";
		}
		if (StringUtil.isNotBlank(queryCondition)) {
			columnSql = "SELECT * FROM (" + columnSql + ") T WHERE " + queryCondition;
		}
		return columnSql;
	}

	/**
	 * 依据DBType获取数据库表名及注释SQL
	 */

	public static String getTableWithCommentSql(DBType dbType, String schema, String queryCondition) {
		String tableSql = "";
		if (dbType.equals(DBType.mysql)) {
			tableSql = "SELECT t.TABLE_NAME,t.TABLE_COMMENT AS COMMENTS FROM INFORMATION_SCHEMA.TABLES t WHERE t.TABLE_SCHEMA='" + schema
					+ "' AND TABLE_TYPE='BASE TABLE' ORDER BY TABLE_NAME";
		} else if (dbType.equals(DBType.oracle)) {
			tableSql = "SELECT t.TABLE_NAME,COMMENTS FROM USER_TABLES t JOIN USER_TAB_COMMENTS C ON t.TABLE_NAME = C.TABLE_NAME  ORDER BY t.TABLE_NAME";
		} else if (dbType.equals(DBType.db2)) {
			tableSql = "SELECT TABNAME AS TABLE_NAME,REMARKS AS COMMENTS  FROM SYSCAT.TABLES T WHERE OWNER='" + schema + "'  ORDER BY TABLE_NAME";
		}
		if (StringUtil.isNotBlank(queryCondition)) {
			tableSql = "SELECT * FROM (" + tableSql + ") T WHERE " + queryCondition;
		}
		return tableSql;
	}

	public static String getColumnWithCommentSql(DBType dbType, String schema, String tablename, String queryCondition) {
		String columnSql = "";
		if (dbType.equals(DBType.mysql)) {
			columnSql = "SELECT COLUMN_NAME,COLUMN_COMMENT FROM  INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='" + schema + "'and TABLE_NAME='"
					+ tablename + "'";
		} else if (dbType.equals(DBType.oracle)) {
			columnSql = "SELECT T1.COLUMN_NAME,COMMENTS as COLUMN_COMMENT FROM USER_TAB_COLUMNS T1  "
					+ "INNER JOIN USER_COL_COMMENTS T2 ON T1.TABLE_NAME=T2.TABLE_NAME and T1.COLUMN_NAME=T2.COLUMN_NAME WHERE T1.TABLE_NAME='"
					+ tablename + "'";
		} else if (dbType.equals(DBType.db2)) {
			columnSql = "SELECT COLNAME AS COLUMN_NAME,REMARKS as COLUMN_COMMENT FROM SYSCAT.COLUMNS WHERE TABSCHEMA='" + schema + "'and TABNAME="
					+ tablename + "'";
		}
		if (StringUtil.isNotBlank(queryCondition)) {
			columnSql = "SELECT * FROM (" + columnSql + ") T WHERE " + queryCondition;
		}
		return columnSql;
	}

	public static int hasTable(DBType dbType, String schema, String tableName) {
		String sql = getTablesSql(dbType, schema, "");
		if (dbType.equals(DBType.mysql)) {
			tableName = tableName.toLowerCase();
		} else if (dbType.equals(DBType.oracle)) {
			tableName = tableName.toUpperCase();
		} else if (dbType.equals(DBType.db2)) {
			tableName = tableName.toUpperCase();
		}
		sql = "SELECT COUNT(1) FROM ( " + sql + ") a WHERE TABLE_NAME = '" + tableName + "'";
		return JdbcSupportDAO.getJdbcTemplate().queryForInt(sql);
	}

	public static String getViewSql(DBType dbType, String schema, String queryCondition) {
		String viewSql = "";
		if (dbType.equals(DBType.mysql)) {
			viewSql = "SELECT TABLE_NAME,VIEW_DEFINITION FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA='" + schema + "'" + queryCondition
					+ " ORDER BY TABLE_NAME";
		} else if (dbType.equals(DBType.oracle)) {
			viewSql = "SELECT VIEW_NAME AS TABLE_NAME , TEXT AS VIEW_DEFINITION FROM USER_VIEWS WHERE 1=1 " + queryCondition + " ORDER BY TABLE_NAME";
		} else if (dbType.equals(DBType.db2)) {
			viewSql = "SELECT TABNAME AS TABLE_NAME FROM SYSCAT.TABLES WHERE OWNER='" + schema + "'" + queryCondition + " ORDER BY TABLE_NAME";
		}
		return viewSql;
	}

	public static String getUpdateSqlBy2Ts(List<String> l1, List<String> l2, String t1, String t2, String on, String where) {
		DBType dbType = JdbcSupportDAO.getDbType();
		StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ").append(t1).append(" t1 ");
		if (dbType.equals(DBType.mysql)) {
			sb.append(" INNER JOIN ").append(t2).append(" t2 ").append(" ON ").append(on).append(" SET ");
			for (int i = 0; i < l1.size(); i++) {
				String s1 = l1.get(i);
				String s2 = l2.get(i);
				sb.append(s1).append("=").append(s2).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
		} else {
			sb.append(" SET (");
			for (String s : l1) {
				sb.append(s).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(")=(SELECT ");
			for (String s : l2) {
				sb.append(s).append(",");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append(" FROM ").append(t2).append(" t2 WHERE ").append(on).append(") WHERE ").append(where);
		}
		return sb.toString();
	}

	public static String escapeValue(String source) {
		if (source == null) {
			return null;
		} else {
			return source.replaceAll("'", "''");
		}
	}

	public static String clobToString(Clob clob) {
		if (clob == null) {
			return null;
		}
		try {
			Reader inStreamDoc = clob.getCharacterStream();

			char[] tempDoc = new char[(int) clob.length()];
			inStreamDoc.read(tempDoc);
			inStreamDoc.close();
			return new String(tempDoc);
		} catch (Exception e) {
			logger.warn("clobToString(Clob)", e); //$NON-NLS-1$
		}

		return null;
	}

	/**
	 * 获取主键sql
	 * 
	 * @param dbType
	 * @param schema
	 * @param tableName
	 * @return 主键sql
	 */
	public static String getPrimeKeySqlOfTable(DBType dbType, String schema, String tableName) {
		String keySql = "";
		if (dbType.equals(DBType.mysql)) {
			keySql = "SELECT  CONSTRAINT_NAME, COLUMN_NAME,  ORDINAL_POSITION AS POSITION" + " FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE "
					+ "WHERE  TABLE_SCHEMA ='" + schema + "' AND TABLE_NAME='" + tableName.toLowerCase()
					+ "' AND CONSTRAINT_NAME = 'PRIMARY' ORDER BY ORDINAL_POSITION";
		} else if (dbType.equals(DBType.oracle)) {
			keySql = "SELECT T1.CONSTRAINT_NAME,T2.COLUMN_NAME,T2.POSITION" + " FROM USER_CONSTRAINTS T1,  USER_CONS_COLUMNS T2 "
					+ "WHERE T1.CONSTRAINT_NAME = T2.CONSTRAINT_NAME " + "AND T1.OWNER='" + schema + "' AND T1.CONSTRAINT_TYPE='P'"
					+ "AND T1.TABLE_NAME ='" + tableName + "'ORDER BY POSITION";
		} else if (dbType.equals(DBType.db2)) {
			keySql = "SELECT CONSTNAME AS CONSTRAINT_NAME,COLNAME AS COLUMN_NAME," + "COLSEQ AS POSITION FROM SYSCAT.KEYCOLUSE WHERE "
					+ "TABSCHEMA='" + schema.toUpperCase() + "' AND TABNAME='" + tableName + "'ORDER BY SYSCAT.KEYCOLUSE.COLSEQ";
		}
		return keySql;
	}

	public static String getIndexSql(DBType dbType, String schema, String tableName) {
		String indexSql = "";
		if (dbType.equals(DBType.mysql)) {
			indexSql = "show index from " + tableName.toLowerCase();
		} else if (dbType.equals(DBType.oracle)) {
			indexSql = "select t.INDEX_NAME,t.COLUMN_NAME,t.COLUMN_POSITION,t.DESCEND,i.UNIQUENESS from user_ind_columns t,user_indexes i "
					+ "where t.index_name = i.index_name and " + "t.table_name='" + tableName + "' ORDER BY INDEX_NAME,COLUMN_POSITION";
		} else if (dbType.equals(DBType.db2)) {
			indexSql = "SELECT INDNAME,UNIQUERULE,COLNAMES" + " FROM SYSCAT.INDEXES WHERE " + "TABSCHEMA='" + schema + "' AND TABNAME='" + tableName
					+ "'ORDER BY INDNAME";
		}
		return indexSql;
	}

	public static int blobUpdate(DataSource ds, String tableName, String whereStr, Map<String, String> blobMap) {
		DatabaseInfo dbInfo = JdbcSupportDAO.getDatabaseInfo();
		String fieldName = blobMap.get("NAME");
		String fieldType = blobMap.get("TYPE");
		final String value = blobMap.get("VALUE");
		if (!dbInfo.getDbType().equals(DBType.oracle)) {
			try {
				if ("CLOB".equals(fieldType)) {
					return clobUpdateForDB2(ds, tableName, fieldName, whereStr, value);
				} else {
					return blobUpdate4Other(ds, tableName, fieldName, whereStr, new ByteArrayInputStream(Base64.decode(value)));
				}
			} catch (SQLException e) {
				logger.error("更新字段" + fieldName + "出现异常", e);
				return 0;
			}
		}
		if ("CLOB".equals(fieldType)) {
			try {
				return clobUpdateForOracle2(ds, tableName, fieldName, whereStr, value);
			} catch (SQLException e) {
				logger.error("更新字段" + fieldName + "出现异常", e);
				return 0;
			}
		}
		if ("BLOB".equals(fieldType)) {
			try {
				return blobUpdateForOracle(ds, tableName, fieldName, whereStr, new OutputStreamWriter() {
					public void write(OutputStream outputStream) throws IOException {
						IOUtil.copy(new ByteArrayInputStream(Base64.decode(value)), outputStream);
					}
				});
			} catch (SQLException e) {
				logger.error("更新字段" + fieldName + "出现异常", e);
				return 0;
			}
		}
		return 0;
	}

	public static int clobUpdateForOracle2(DataSource ds, String tableName, String filedName, String whereStr, String value) throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdateForOracle(String, String, String, String, byte[]) - start"); //$NON-NLS-1$
		}
		String selectSql = "select " + filedName + " from " + tableName + " where " + whereStr + " for update";
		String updateSql = "UPDATE " + tableName + " SET " + filedName + "=EMPTY_CLOB() WHERE " + whereStr;
		Statement stmt = null;
		ResultSet rs = null;
		int result = -1;
		Connection cn = null;
		try {
			cn = ds.getConnection();
			cn.setAutoCommit(false);
			stmt = cn.createStatement();
			result = stmt.executeUpdate(updateSql);
			rs = stmt.executeQuery(selectSql);
			while (rs.next()) {
				BufferedWriter bfWritef = null;
				try {
					Clob clob = rs.getClob(1);
					bfWritef = new BufferedWriter(clob.setCharacterStream(1));
					bfWritef.write(value);
				} catch (Exception e) {
					cn.rollback();
					logger.warn("blobUpdateForOracle:", e);
					throw new SQLException("更新失败:" + e.getMessage());
				} finally {
					if (null != bfWritef) {
						bfWritef.close();
					}
				}
			}
			cn.commit();
		} catch (Exception e) {
			logger.warn("blobUpdateForOracle:" + whereStr, e);
			throw new SQLException("更新失败:" + e.getMessage());
		} finally {
			cn.setAutoCommit(true);
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
			if (cn != null) {
				try {
					cn.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdateForOracle(String, String, String, String, byte[]) - end"); //$NON-NLS-1$
		}
		return result;
	}

	public static int clobUpdateForDB2(DataSource ds, String tableName, String filedName, String whereStr, String value) throws SQLException {
		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdateForOracle(String, String, String, String, byte[]) - start"); //$NON-NLS-1$
		}
		String updateSql = "UPDATE " + tableName + " SET " + filedName + "=? WHERE " + whereStr;
		Statement stmt = null;
		ResultSet rs = null;
		int result = -1;
		Connection cn = null;
		try {
			cn = ds.getConnection();
			cn.setAutoCommit(false);
			stmt = cn.createStatement();
			PreparedStatement prestmt = null;
			prestmt = cn.prepareStatement(updateSql);
			Clob clob = new SerialClob(value.toCharArray());
			clob.setString(1L, value);
			prestmt.setClob(1, clob);
			prestmt.executeUpdate();
			cn.commit();
		} catch (Exception e) {
			logger.warn("blobUpdateForOracle:" + whereStr, e);
			throw new SQLException("更新失败:" + e.getMessage());
		} finally {
			cn.setAutoCommit(true);
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
			if (cn != null) {
				try {
					cn.close();
				} catch (Exception e) {
					logger.error("blobUpdateForOracle", e);
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("blobUpdateForOracle(String, String, String, String, byte[]) - end"); //$NON-NLS-1$
		}
		return result;
	}

	public static List<Map<String, Object>> transRC(List<Map<String, Object>> original) {
		if (original == null || original.size() == 0) {
			return null;
		}

		List<Map<String, Object>> r = new ArrayList<Map<String, Object>>();

		Set<String> keySet = original.get(0).keySet();
		for (int i = 0; i < keySet.size(); i++) {
			r.add(new LinkedHashMap<String, Object>());
		}
		int col = 1;
		for (Map<String, Object> map : original) {
			int row = 0;
			for (String key : keySet) {
				r.get(row).put("F" + col, map.get(key));
				row++;
			}
			col++;
		}
		return r;
	}
	public static List<Map<String, Object>> selectTableValues(String tableName, Map<String, String> keyValues) {
		StringBuffer conditionSql = new StringBuffer();
		for (Map.Entry<String, String> entry : keyValues.entrySet()) {
			if (!StringUtil.isEmpty(entry.getValue())) {
				conditionSql.append(entry.getKey());
				conditionSql.append("='");
				conditionSql.append(entry.getValue());
				conditionSql.append("',");
			}
		}
		conditionSql.deleteCharAt(conditionSql.length() - 1);
		return JdbcSupportDAO.getJdbcTemplate().queryForList("select * from " + tableName + " where " + conditionSql);
	}

	public static int haveRecord(String tableName, Map<String, String> keyValues) {
		StringBuffer conditionSql = new StringBuffer();
		for (Map.Entry<String, String> entry : keyValues.entrySet()) {
			if (!StringUtil.isEmpty(entry.getValue())) {
				conditionSql.append(" and ");
				conditionSql.append(entry.getKey());
				conditionSql.append("='");
				conditionSql.append(entry.getValue());
				conditionSql.append("'");
			}
		}
		return JdbcSupportDAO.getJdbcTemplate().queryForObject("select count(1) from " + tableName + " where 1=1" + conditionSql, Integer.class);
	}

	public static int insertTableValues(String tableName, Map<String,String> values) {
		StringBuffer insertSql = new StringBuffer();
		StringBuffer fileds = new StringBuffer();
		StringBuffer marks = new StringBuffer();
		insertSql.append("insert into ");
		insertSql.append(tableName.toLowerCase() + "(");
		for (Map.Entry<String, String> entry : values.entrySet()) {
			if (!StringUtil.isEmpty(entry.getValue()) && !entry.getValue().equals(tableName)) {
				fileds.append(entry.getKey() + ",");
				marks.append("'" + entry.getValue() + "',");
			}
		}
		fileds.deleteCharAt(fileds.length() - 1);
		marks.deleteCharAt(marks.length() - 1);
		insertSql.append(fileds).append(")").append(" values (").append(marks).append(")");
		return JdbcSupportDAO.getJdbcTemplate().update(insertSql.toString());
	}

	public static int updateTableValues(String tableName, Collection<String> keys, Map<String, String> values) {
		StringBuffer updateSql = new StringBuffer();
		updateSql.append("update ");
		updateSql.append(tableName.toLowerCase());
		updateSql.append(" set ");
		for (Map.Entry<String, String> entry : values.entrySet()) {
			if (!keys.contains((entry.getKey())) && !entry.getValue().equals(tableName)) {
				updateSql.append(entry.getKey());
				updateSql.append("='");
				updateSql.append(entry.getValue());
				updateSql.append("',");
			}
		}
		updateSql.deleteCharAt(updateSql.length() - 1);
		updateSql.append(" where (1=1) ");
		for (String key : keys) {
			updateSql.append(" and ");
			updateSql.append(key);
			updateSql.append("='");
			updateSql.append(values.get(key));
			updateSql.append("'");
		}
		return JdbcSupportDAO.getJdbcTemplate().update(updateSql.toString());
	}

	public static int deleteTableValues(String tableName, Map<String, String> keyValues) {
		StringBuffer conditionSql = new StringBuffer();
		for (Map.Entry<String, String> entry : keyValues.entrySet()) {
			if (!StringUtil.isEmpty(entry.getValue())) {
				conditionSql.append(" and ");
				conditionSql.append(entry.getKey());
				conditionSql.append("='");
				conditionSql.append(entry.getValue());
				conditionSql.append("'");
			}
		}
		return JdbcSupportDAO.getJdbcTemplate().update("delete from " + tableName + " where 1=1" + conditionSql);
	}
	public static void main(String[] args) {
		List<Map<String, Object>> r = new ArrayList<Map<String, Object>>();
		Map<String, Object> map1 = MapUtil.mapAnything("a", "1", "b", "2", "c", "3", "d", "4");
		Map<String, Object> map2 = MapUtil.mapAnything("a", "5", "b", "6", "c", "7", "d", "8");
		r.add(map1);
		r.add(map2);
		List<Map<String, Object>> transRC = transRC(r);
		System.out.println(transRC);
	}

}