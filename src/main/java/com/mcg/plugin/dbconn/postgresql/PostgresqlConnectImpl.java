/*
 * @Copyright (c) 2018 缪聪(mcg-helper@qq.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");  
 * you may not use this file except in compliance with the License.  
 * You may obtain a copy of the License at  
 *     
 *     http://www.apache.org/licenses/LICENSE-2.0  
 *     
 * Unless required by applicable law or agreed to in writing, software  
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
 * See the License for the specific language governing permissions and  
 * limitations under the License.
 */

package com.mcg.plugin.dbconn.postgresql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import com.mcg.entity.flow.data.DataRecord;
import com.mcg.entity.flow.gmybatis.Table;
import com.mcg.plugin.dbconn.AbstractConnect;
import com.mcg.plugin.dbconn.McgConnect;
import com.mcg.util.JDBCTypesUtils;
import com.mcg.util.Tools;

public class PostgresqlConnectImpl extends AbstractConnect implements McgConnect {

    public PostgresqlConnectImpl(DataSource dataSource) {
        super(dataSource);
    }
    
    @Override
    public List<DataRecord> getTableInfo(String tableName) throws Exception {
        List<DataRecord> result = new ArrayList<DataRecord>();
        Connection conn = dbConnect.getConnection();
        
        DatabaseMetaData dbmd = conn.getMetaData();
        ResultSet resultSet = dbmd.getTables(null, "%", tableName, new String[] { "TABLE" });
        
        ResultSet primaryKeyResultSet = dbmd.getPrimaryKeys(conn.getCatalog() ,null,tableName);  
        Map<String, String> primaryMap = new HashMap<String, String>();
        while(primaryKeyResultSet.next()){  
            String primaryKeyColumnName = primaryKeyResultSet.getString("COLUMN_NAME"); 
            primaryMap.put(primaryKeyColumnName, primaryKeyColumnName);
        }          
        
        while (resultSet.next()) {
            String existTableName = resultSet.getString("TABLE_NAME");
            if(existTableName.equals(tableName)){
                ResultSet rs = conn.getMetaData().getColumns(null, null, existTableName, "%");
                while(rs.next()){
                    DataRecord dataRecord = new DataRecord();
                    dataRecord.setTableField(rs.getString("COLUMN_NAME")); //字段名
                    dataRecord.setClassField(Tools.convertFieldName(dataRecord.getTableField()));//变量名
                    dataRecord.setComment(rs.getString("REMARKS")); //字段注释
                    dataRecord.setTableFieldType(rs.getString("TYPE_NAME")); //表字段数据类型
                    dataRecord.setMandatory(!rs.getBoolean("NULLABLE"));//非空
                    dataRecord.setLength(rs.getInt("COLUMN_SIZE"));//列大小
                    dataRecord.setPrecision(rs.getInt("DECIMAL_DIGITS"));//精度
                    dataRecord.setInclude(JDBCTypesUtils.jdbcTypeToJavaType(rs.getInt("DATA_TYPE")).getName()); //java import类型 Constants.javaTypeMap.get(rs.getInt("DATA_TYPE"))
                    dataRecord.setDataType(Tools.splitLast(dataRecord.getInclude()));
                    
                    dataRecord.setPrimary(rs.getString("COLUMN_NAME").equalsIgnoreCase(primaryMap.get(rs.getString("COLUMN_NAME")))); //是否为主键
//                  rs.getString("COLUMN_DEF"); //默认值
//                  rs.getString("ORDINAL_POSITION")//序号
                    result.add(dataRecord);
                }
                
            }
        }
        dbConnect.freeConnection();
        
        try {
            String[] param = new String[]{ tableName }; 
            List<DataRecord> commentlist = dbConnect.querySql(DataRecord.class, 
                    "SELECT col_description(a.attrelid,a.attnum) as comment, a.attname as tableField FROM pg_class as c,pg_attribute as a where c.relname = ? and a.attrelid = c.oid and a.attnum>0" ,  param);
            
            if(commentlist != null && commentlist.size() > 0) {
                Map<String, String> mappedMap = new HashMap<String, String>();
                for(DataRecord dataRecord : commentlist) {
                    mappedMap.put(dataRecord.getTableField(), dataRecord.getComment());
                }                
/*                Map<String, String> mappedMap = commentlist.stream().collect(  
                        Collectors.toMap(DataRecord::getTableField, (p) -> p.getComment()));*/       
                for(int i=0; i<result.size(); i++) {
                    result.get(i).setComment(mappedMap.get(result.get(i).getTableField()));
                }                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }        
        
        return result;
    }


    @Override
    public List<Table> getTablesByDataSource(String dbName) throws SQLException {
        return dbConnect.querySql(Table.class, "SELECT tablename tableName FROM pg_tables WHERE tablename NOT LIKE 'pg%' AND tablename NOT LIKE 'sql_%' ",  null);
    }
    
}
