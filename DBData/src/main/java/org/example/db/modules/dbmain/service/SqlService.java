package org.example.db.modules.dbmain.service;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.common.config.DataSourceWith;
import org.example.common.enums.DataSourceType;
import org.example.db.modules.dbmain.bean.ExecuteRequest;
import org.example.db.modules.dbmain.bean.ResultBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    public static String getFirstKeyword(String sql) {
        sql = sql.replaceAll("(?s)/\\*.*?\\*/", "");
        sql = sql.replaceAll("(?m)^\\s*--.*$", "");
        sql = sql.trim();
        Matcher m = Pattern.compile("^(\\w+)", Pattern.CASE_INSENSITIVE).matcher(sql);
        return m.find() ? m.group(1).toUpperCase() : "";
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public ResultBean queryMaster(ExecuteRequest executeRequest) {
        String sql = executeRequest.getSql().replaceAll(";", "");
        String firstKeyword = getFirstKeyword(sql);
        ResultBean resultBean = new ResultBean();
        List<Map<String, Object>> maps = new ArrayList<>();
        Integer pageNum = executeRequest.getPageNum();
        Integer pageSize = executeRequest.getPageSize();
        Long total = 0L;
        int offset = (pageNum - 1) * pageSize;
        String pageSql;
        Map<String, Object> map = new HashMap<>();
        try {
            if ("SELECT".equals(firstKeyword)) {
                // 统计总数
                String countSql = "select count(*) from (" + sql + ") t";
                total = jdbcTemplate.queryForObject(countSql, Long.class);
                pageSql = "" + sql + "  LIMIT ?, ?";
                maps = jdbcTemplate.queryForList(pageSql, offset, pageSize);
            } else {
                maps = jdbcTemplate.queryForList(sql);
            }
        } catch (Exception e) {
            resultBean.setCode(500);
            map.put("error", e.getMessage());
            maps.add(map);
            resultBean.setMessage(e.getMessage());
            resultBean.setRows(maps);
            return resultBean;
        }
        resultBean.setTotal(total);
        resultBean.setCode(200);
        resultBean.setRows(maps);
        return resultBean;
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
        if (!isSelectSql(sql)) {
            throw new RuntimeException("仅允许导出SELECT语句");
        }
        Integer pageNum = request.getPageNum();
        Integer pageSize = request.getPageSize();
        if (pageNum != null && pageSize != null) {
            int offset = (pageNum - 1) * pageSize;
            sql = sql + " LIMIT " + offset + "," + pageSize;
        }
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        exportExcel(rows, response);
    }
    
    private void exportExcel(List<Map<String, Object>> rows, HttpServletResponse response) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("result");
        if (!rows.isEmpty()) {
            List<String> columns = new ArrayList<>(rows.get(0).keySet());
            // 表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                headerRow.createCell(i).setCellValue(columns.get(i));
            }
            // 数据
            for (int i = 0; i < rows.size(); i++) {
                Row excelRow = sheet.createRow(i + 1);
                Map<String, Object> dataRow = rows.get(i);
                for (int j = 0; j < columns.size(); j++) {
                    Object value = dataRow.get(columns.get(j));
                    excelRow.createCell(j).setCellValue(value == null ? "" : String.valueOf(value));
                }
            }
        }
        String fileName = URLEncoder.encode("sql_result.xlsx", StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        workbook.write(response.getOutputStream());
        workbook.close();
    }
    
    private boolean isSelectSql(String sql) {
        sql = sql.replaceAll("(?s)/\\*.*?\\*/", "");
        sql = sql.replaceAll("(?m)^\\s*--.*$", "");
        sql = sql.trim();
        Matcher matcher = Pattern.compile("^(\\w+)", Pattern.CASE_INSENSITIVE).matcher(sql);
        return matcher.find() && "SELECT".equalsIgnoreCase(matcher.group(1));
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
//        List<Map<String, Object>> rows = jdbcTemplate.queryForList(executeRequest.getSql());
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
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        return rows;
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public ResultBean updateMaster(ExecuteRequest executeRequest) {
        ResultBean resultBean = new ResultBean();
        List<Map<String, Object>> maps = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        try {
            int update = jdbcTemplate.update(executeRequest.getSql());
            resultBean.setCode(200);
            resultBean.setMessage("已更新行数" + update);
            map.put("result", "已更新行数" + update);
            maps.add(map);
            resultBean.setRows(maps);
            resultBean.setTotal((long) update);
            return resultBean;
        } catch (Exception e) {
            resultBean.setCode(500);
            map.put("error", e.getMessage());
            maps.add(map);
            resultBean.setMessage(e.getMessage());
            resultBean.setRows(maps);
            return resultBean;
        }
        
        
    }
}
