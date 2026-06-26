package org.example.db.modules.dbmain.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.common.config.DataSourceWith;
import org.example.common.enums.DataSourceType;
import org.example.db.modules.dbmain.bean.*;
import org.example.db.modules.dbmain.util.excel.ExcelPoiUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlService {
    private final String IS_SELECT = "result";
    private final String IS_NOT_SELECT = "execute";
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SqlExecuteService sqlExecuteService;
    
    
    @DataSourceWith(DataSourceType.MASTER)
    public ResultBean queryMaster(ExecuteRequest executeRequest) {
        ResultBean resultBean = new ResultBean();
        List<Map<String, Object>> maps;
        String sql = executeRequest.getSql().replaceAll(";", "");
        Integer pageNum = executeRequest.getPageNum();
        Integer pageSize = executeRequest.getPageSize();
        Integer total = 0;
        int offset = (pageNum - 1) * pageSize;
        if (isSelectSql(sql)) {
            try {
                total = sqlExecuteService.queryForObjectMaster("select count(*) from (" + sql + ") t");
            } catch (Exception e) {
                sqlExecuteService.queryForObjectMaster(sql);
            }
            maps = sqlExecuteService.queryForListMaster("" + sql + "  LIMIT ?, ?", offset, pageSize);
            if (!maps.isEmpty()) {
                Map<String, Object> map = maps.get(0);
                resultBean.setColumns(getColumns(map));
            }
            resultBean.setTotal(total);
            resultBean.setType(IS_SELECT);
            dealMaps(maps);
            resultBean.setRows(maps);
            resultBean.setRowCount(maps.size());
            return resultBean;
        } else {
            return updateMaster(executeRequest);
        }
    }
    
    private static List<String> getColumns(Map<String, Object> map) {
        List<String> columns = new ArrayList<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            columns.add(entry.getKey());
        }
        return columns;
    }
    
    private void dealMaps(List<Map<String, Object>> maps) {
        for (Map<String, Object> row : maps) {
            row.replaceAll((k, v) -> {
                if (v instanceof BigDecimal
                        || v instanceof BigInteger
                        || v instanceof Long) {
                    return v.toString();
                }
                return v;
            });
        }
    }
    
    @DataSourceWith(DataSourceType.SLAVE)
    public List<Map<String, Object>> querySlave(String sql) {
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        for (Map<String, Object> row : maps) {
            row.forEach((key, value) -> {
                if (value instanceof Timestamp) {
                    row.put(key, ((Timestamp) value).toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
            });
        }
        return maps;
    }
    
    public void export(ExecuteRequest request, HttpServletResponse response) throws Exception {
        String sql = request.getSql();
        // 只允许 SELECT
        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        if (pageNum != null && pageSize != null) {
            int offset = (pageNum - 1) * pageSize;
            sql = sql + " LIMIT " + offset + "," + pageSize;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        String sheetName = "结果";
        ExcelPoiUtil.excelPort(sheetName,getColumns(rows.get(0)),rows,null,response);
    }
    
    private boolean isSelectSql(String sql) {
        if (sql == null) {
            return false;
        }
        sql = sql.replaceAll("(?s)/\\*.*?\\*/", "");
        sql = sql.replaceAll("(?m)^\\s*--.*$", "");
        sql = sql.trim();
        
        Matcher matcher = Pattern.compile("^(\\w+)", Pattern.CASE_INSENSITIVE)
                .matcher(sql);
        
        if (!matcher.find()) {
            return false;
        }
        String firstWord = matcher.group(1);
        return "SHOW".equalsIgnoreCase(firstWord)
                || "SELECT".equalsIgnoreCase(firstWord)
                || "PRAGMA".equalsIgnoreCase(firstWord)
                || "WITH".equalsIgnoreCase(firstWord);
    }
    
    public List<String> getTables() throws Exception {
        Connection conn = Objects.requireNonNull(jdbcTemplate.getDataSource()).getConnection();
        DatabaseMetaData metaData = conn.getMetaData();
        String dbName = metaData.getDatabaseProductName();
        String dbVersion = metaData.getDatabaseProductVersion();
        String url = metaData.getURL();
        String sql = "";
        if (dbName.equals("DM DBMS")) {
            sql = "SELECT TABLE_NAME\n" +
                    "FROM ALL_TABLES\n" +
                    "WHERE OWNER = 'EAS'\n" +
                    "  AND TABLE_NAME NOT LIKE '%20%'\n" +
                    "ORDER BY TABLE_NAME;";
        } else if (dbName.equals("MySQL")) {
            sql = "SELECT TABLE_NAME\n" +
                    "FROM information_schema.TABLES\n" +
                    "WHERE TABLE_SCHEMA = DATABASE()\n" +
                    "  AND TABLE_NAME NOT LIKE '%20%'\n" +
                    "ORDER BY TABLE_NAME;";
        }
        List<String> tables = jdbcTemplate.queryForList(sql, String.class);
        conn.close();
        return tables;
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public void getTableData(ExecuteRequest executeRequest) {
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public List<Map<String, Object>> getTableColumns(String tableName) {
        String sql = "SELECT\n" +
                "    c.COLUMN_NAME AS name,\n" +
                "    CASE\n" +
                "        WHEN c.DATA_TYPE IN ('VARCHAR', 'VARCHAR2', 'CHAR')\n" +
                "            THEN c.DATA_TYPE || '(' || c.DATA_LENGTH || ')'\n" +
                "        WHEN c.DATA_TYPE IN ('NUMBER', 'DECIMAL')\n" +
                "            THEN c.DATA_TYPE || '(' ||\n" +
                "                 NVL(TO_CHAR(c.DATA_PRECISION), '0') || ',' ||\n" +
                "                 NVL(TO_CHAR(c.DATA_SCALE), '0') || ')'\n" +
                "        ELSE c.DATA_TYPE\n" +
                "    END AS type,\n" +
                "    CASE\n" +
                "        WHEN c.NULLABLE = 'N' THEN 1\n" +
                "        ELSE 0\n" +
                "    END AS notnull,\n" +
                "    c.DATA_DEFAULT AS dflt_value,\n" +
                "    CASE\n" +
                "        WHEN pk.COLUMN_NAME IS NOT NULL THEN 1\n" +
                "        ELSE 0\n" +
                "    END AS pk\n" +
                "FROM ALL_TAB_COLUMNS c\n" +
                "LEFT JOIN (\n" +
                "    SELECT\n" +
                "        acc.OWNER,\n" +
                "        acc.TABLE_NAME,\n" +
                "        acc.COLUMN_NAME\n" +
                "    FROM ALL_CONSTRAINTS ac\n" +
                "    JOIN ALL_CONS_COLUMNS acc\n" +
                "      ON ac.OWNER = acc.OWNER\n" +
                "     AND ac.CONSTRAINT_NAME = acc.CONSTRAINT_NAME\n" +
                "    WHERE ac.CONSTRAINT_TYPE = 'P'\n" +
                ") pk\n" +
                "ON c.OWNER = pk.OWNER\n" +
                "AND c.TABLE_NAME = pk.TABLE_NAME\n" +
                "AND c.COLUMN_NAME = pk.COLUMN_NAME\n" +
                "WHERE c.OWNER = UPPER('EAS')\n" +
                "  AND c.TABLE_NAME = UPPER('" + tableName + "')\n" +
                "ORDER BY c.COLUMN_ID;";
        return jdbcTemplate.queryForList(sql);
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public ResultBean updateMaster(ExecuteRequest executeRequest) {
        ResultBean resultBean = new ResultBean();
        int update = sqlExecuteService.updateMaster(executeRequest.getSql());
        resultBean.setChanges(update);
        resultBean.setType(IS_NOT_SELECT);
        return resultBean;
        
        
    }
    
    public List<BatchResult> executeAll(ExecuteRequest executeRequest) {
        List<BatchResult> batchResults = new ArrayList<>();
        String[] split = executeRequest.getSql().split(";");
        int count = 0;
        Integer index = executeRequest.getIndex();
        for (String s : split) {
            executeRequest.setSql(s);
            ResultBean resultBean = queryMaster(executeRequest);
            BatchResult batchResult = new BatchResult();
            BatchRows batchRows = new BatchRows();
            batchRows.setTotal(resultBean.getTotal());
            batchRows.setRows(resultBean.getRows());
            batchRows.setType(IS_SELECT);
            batchRows.setRowCount(resultBean.getTotal());
            List<Map<String, Object>> map1 = resultBean.getRows();
            dealMaps(map1);
            if (!map1.isEmpty()) {
                batchRows.setColumns(getColumns(map1.get(0)));
            }
            batchResult.setResult(batchRows);
            batchResult.setSql(executeRequest.getSql());
            batchResults.add(batchResult);
            count++;
        }
        return batchResults;
    }
}
