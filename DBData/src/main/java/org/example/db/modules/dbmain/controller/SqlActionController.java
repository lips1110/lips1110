package org.example.db.modules.dbmain.controller;

import org.example.db.modules.dbmain.bean.BatchResult;
import org.example.db.modules.dbmain.bean.BatchRows;
import org.example.db.modules.dbmain.bean.ExecuteRequest;
import org.example.db.modules.dbmain.bean.ResultBean;
import org.example.db.modules.dbmain.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/db")
public class SqlActionController {
    @Autowired
    private SqlService sqlService;
    
    @GetMapping("/health")
    public void getHealth() {
    }
    
    @GetMapping("/tables/{tableName}/columns")
    public List<Map<String, Object>> getTableColumns(@PathVariable String tableName) {
        return sqlService.getTableColumns(tableName);
    }
    
    @PostMapping("/tables/data")
    public void getTableData(@RequestBody ExecuteRequest executeRequest) {
        sqlService.getTableData(executeRequest);
    }
    
    @GetMapping("/tables")
    public List<String> tables() throws Exception {
        return sqlService.getTables();
    }
    
    @PostMapping("/execute")
    public ResultBean execute(@RequestBody ExecuteRequest executeRequest) {
        if (executeRequest.getSql().toUpperCase().contains("UPDATE")) {
            return sqlService.updateMaster(executeRequest);
        } else {
            return sqlService.queryMaster(executeRequest);
        }
        
    }
    
    @RequestMapping("/execute-all")
    public List<BatchResult> executeAll(@RequestBody ExecuteRequest executeRequest) {
        List<BatchResult> batchResults = new ArrayList<>();
        String[] split = executeRequest.getSql().split(";");
        for (String s : split) {
            executeRequest.setSql(s);
            ResultBean resultBean = sqlService.queryMaster(executeRequest);
            BatchResult batchResult = new BatchResult();
            BatchRows batchRows = new BatchRows();
            batchRows.setRows(resultBean.getRows());
            batchRows.setType("result");
            batchRows.setRowCount(resultBean.getTotal());
            List<String> cloumns = new ArrayList<>();
            Map<String, Object> map = resultBean.getRows().get(0);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                cloumns.add(entry.getKey());
            }
            batchRows.setColumns(cloumns);
            batchResult.setResult(batchRows);
            batchResult.setSql(executeRequest.getSql());
            batchResults.add(batchResult);
        }
        return batchResults;
    }
    
    @PostMapping("/exportAllApi")
    public void exportAllApi(@RequestBody ExecuteRequest request, HttpServletResponse response) throws Exception {
        String replace = request.getSql().replace(";", "");
        request.setSql(replace);
        sqlService.export(request, response);
    }
    
}
