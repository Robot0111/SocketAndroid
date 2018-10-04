package info.androidhive.webmobilegroupchat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JDBCUtil {
	private final static Logger logger = LoggerFactory.getLogger(JDBCUtil.class);
	
	private PreparedStatement pstmt;
	private Connection conn;
	private ResultSet rs;
	
	   /* 连接数据库
	    * @return
	    */
	   public static Connection getDBConnection()
	   {
	       // 1. 注册驱动
	       try {
	           Class.forName("com.mysql.jdbc.Driver");
	       } catch (ClassNotFoundException e) {
	           // TODO Auto-generated catch block
	           e.printStackTrace();
	       }
	       // 获取数据库的连接
	       try {
	           Connection conn  = java.sql.DriverManager.getConnection("jdbc:mysql://39.104.86.193/CHAT?useUnicode=true&characterEncoding=utf-8&useSSL=false", 
	        		   "root", "luqilai1993@!L");
	           return conn;
	       } catch (SQLException e1) {
	    	   logger.error("数据库连接失败");
	           e1.printStackTrace();
	       }
	       return null;
	   }
	   
	
	/**
	        * 执行修改添加操作
	        * @param coulmn
	        * @param type
	        * @param sql
	        * @return
	        * @throws SQLException
	        */
	   public   boolean updateOrAdd(String[] coulmn, int[] type, String sql) throws SQLException
	      {
		   conn = getDBConnection();
	          if(!setPstmtParam(coulmn, type, sql))
	              return false;
	          boolean flag = pstmt.executeUpdate()>0?true:false;
	          closeDB();
	          return flag;
	      }
	   /**
	        * 获取查询结果集
	        * @param coulmn
	        * @param type
	        * @param sql
	        * @throws SQLException
	        */
	   public String getResultData(String[] coulmn, int[] type, String sql) throws SQLException
	     {
		   conn = getDBConnection();
	        // DataTable dt = new DataTable();
	         
	         ArrayList<HashMap<String, String>>list = new ArrayList<HashMap<String, String>>();
	         
	         if(!setPstmtParam(coulmn, type, sql))
	             return null;
	         rs = pstmt.executeQuery();
	         ResultSetMetaData rsmd = rs.getMetaData();//取数据库的列名 
	         int numberOfColumns = rsmd.getColumnCount();
	         while(rs.next())
	         {
	             HashMap<String, String> rsTree = new HashMap<String, String>(); 
	             for(int r=1;r<numberOfColumns+1;r++)
	              {
	                rsTree.put(rsmd.getColumnName(r),rs.getObject(r).toString());
	              }
	             list.add(rsTree);
	         }
	         closeDB();
	       //  dt.setDataTable(list);
	         return JSONObject.valueToString(JSONObject.wrap(list));
	     }
	   /**
	       * 参数设置
	       * @param coulmn
	       * @param type
	       * @throws SQLException 
	       * @throws NumberFormatException 
	       */
	      private boolean setPstmtParam(String[] coulmn, int[] type, String sql) throws NumberFormatException, SQLException
	      {
	          if(sql== null) return false;
	          pstmt = conn.prepareStatement(sql);
	          if(coulmn != null && type != null && coulmn.length !=0 && type.length !=0   )
	          {        
	              for (int i = 0; i<type.length; i++) {
	                  switch (type[i]) {
	                  case Types.INTEGER:
	                      pstmt.setInt(i+1, Integer.parseInt(coulmn[i]));
	                      break;
	                  case Types.BOOLEAN:
	                      pstmt.setBoolean(i+1, Boolean.parseBoolean(coulmn[i]));
	                      break;
	                  case Types.VARCHAR:
	                      pstmt.setString(i+1, coulmn[i]);
	                      break;
	                  case Types.TIMESTAMP:
	                      pstmt.setTimestamp(i+1, Timestamp.valueOf(coulmn[i]));
	                      break;
	                  case Types.DOUBLE:
	                      pstmt.setDouble(i+1, Double.parseDouble(coulmn[i]));
	                      break;
	                  case Types.FLOAT:
	                      pstmt.setFloat(i+1, Float.parseFloat(coulmn[i]));
	                      break;
	                  default:
	                      break;
	                  }
	              }
	          }
	          return true;
	      }
	      
	      /**
	       * 关闭数据库
	       * @throws SQLException
	       */
	      private void closeDB() throws SQLException
	      {
	          if(rs != null)
	          {
	              rs.close();
	          }
	          if(pstmt != null)
	          {
	              pstmt.close();
	          }
	          if(conn != null)
	          {
	              conn.close();
	          }
	          
	      }
}
