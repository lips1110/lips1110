package org.example.db.modules.dbmain.controller;

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
        return sqlService.queryMaster(executeRequest);
    }
    
    @RequestMapping("/execute-all")
    public HashMap<String, Object> executeAll(@RequestBody ExecuteRequest executeRequest) {
        ResultBean resultBean = sqlService.queryMaster(executeRequest);
        List<Map<String, Object>> rows = resultBean.getRows();
        for (int i = 0; i < rows.size(); i++) {
            Map<String, Object> stringObjectMap = rows.get(i);
            stringObjectMap.put("type", "result");
            rows.set(i, stringObjectMap);
        }
        List<HashMap<String, Object>> result = new ArrayList<>();
        HashMap<String, Object> hashMap = new HashMap<>();
        HashMap<String, Object> hashMap2 = new HashMap<>();
        hashMap2.put("columns", "");
        hashMap2.put("rowCount", rows);
        hashMap2.put("result", rows);
        hashMap.put("type", executeRequest.getSql());
        hashMap.put("result", hashMap2);
        result.add(hashMap);
        
        return hashMap;
    }
    
    @PostMapping("/exportAllApi")
    public void exportAllApi(@RequestBody ExecuteRequest request, HttpServletResponse response) throws Exception {
        sqlService.export(request, response);
    }
    
}
