package org.example.db.modules.dbmain.controller;

import org.example.db.modules.dbmain.bean.ExecuteRequest;
import org.example.db.modules.dbmain.bean.ResultBean;
import org.example.db.modules.dbmain.service.SqlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
        return sqlService.queryMaster(executeRequest);
    }
    
    @PostMapping("/exportAllApi")
    public void exportAllApi(@RequestBody ExecuteRequest request, HttpServletResponse response) throws Exception {
        sqlService.export(request, response);
    }
    
}
