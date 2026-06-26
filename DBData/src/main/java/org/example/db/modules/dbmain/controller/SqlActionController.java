package org.example.db.modules.dbmain.controller;

import org.example.db.modules.dbmain.bean.*;
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
    public BaseResult execute(@RequestBody ExecuteRequest executeRequest) {
        return BaseResult.success(sqlService.queryMaster(executeRequest));
    }
    
    @RequestMapping("/execute-all")
    public List<BatchResult> executeAll(@RequestBody ExecuteRequest executeRequest) {
        return sqlService.executeAll(executeRequest);
    }
    
    @PostMapping("/exportAllApi")
    public void exportAllApi(@RequestBody ExecuteRequest request, HttpServletResponse response) throws Exception {
        request.setSql(request.getSql().replace(";", ""));
        sqlService.export(request, response);
    }
    
}
