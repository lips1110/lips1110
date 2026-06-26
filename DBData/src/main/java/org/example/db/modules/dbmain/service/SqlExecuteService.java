package org.example.db.modules.dbmain.service;

import org.example.common.config.DataSourceWith;
import org.example.common.enums.DataSourceType;
import org.example.db.modules.dbmain.mapper.SqlExecuteMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SqlExecuteService {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private SqlExecuteMapper sqlExecuteMapper;
    
    public Object select(String sql) {
        return sqlExecuteMapper.select(sql);
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public List<Map<String, Object>> queryForListMaster(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public List<Map<String, Object>> queryForListMaster(String sql, int offset, int pageSize) {
        return jdbcTemplate.queryForList(sql, offset, pageSize);
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public int updateMaster(String sql) {
        return jdbcTemplate.update(sql);
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public Integer queryForObjectMaster(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
    
    @DataSourceWith(DataSourceType.SLAVE)
    public List<Map<String, Object>> queryForListSlave(String sql) {
        return jdbcTemplate.queryForList(sql);
    }
    
    @DataSourceWith(DataSourceType.SLAVE)
    public List<Map<String, Object>> queryForListSlave(String sql, int offset, int pageSize) {
        return jdbcTemplate.queryForList(sql, offset, pageSize);
    }
    
    @DataSourceWith(DataSourceType.MASTER)
    public Integer queryForObjectSlave(String sql) {
        return jdbcTemplate.queryForObject(sql, Integer.class);
    }
}
