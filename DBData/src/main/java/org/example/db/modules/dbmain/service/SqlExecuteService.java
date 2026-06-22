package org.example.db.modules.dbmain.service;

import org.example.db.modules.dbmain.mapper.SqlExecuteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SqlExecuteService {
    
    @Autowired
    private SqlExecuteMapper sqlExecuteMapper;
    
    public Object select(String sql) {
      return   sqlExecuteMapper.select(sql);
    }
    
    public void getTableData(String tableName) {
    }
}
