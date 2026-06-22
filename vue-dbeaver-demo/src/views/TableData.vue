<template>
  <div class="table-data-view">
    <div class="tab-bar">
      <div class="tab active">
        <i class="el-icon-s-grid"/>
        {{ tableName }} - 数据
      </div>
      <el-button size="mini" type="primary" icon="el-icon-refresh" @click="loadData">刷新</el-button>
      <el-button size="mini" icon="el-icon-edit-outline" @click="openInScript">在脚本中查询</el-button>
    </div>

    <div class="content" v-loading="loading">
      <el-table
          v-if="columns.length"
          :data="rows"
          border
          stripe
          size="small"
          height="100%"
          style="width: 100%"
      >
        <el-table-column
            v-for="col in columns"
            :key="col.name"
            :prop="col.name"
            :label="col.name"
            min-width="120"
            show-overflow-tooltip
        >
          <template slot="header">
            <span>{{ col.name }}</span>
            <el-tag size="mini" type="info" style="margin-left: 4px">{{ col.type }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else description="暂无数据"/>
    </div>
  </div>
</template>

<script>
import api from '../api';

export default {
  name: 'TableData',
  data() {
    return {
      loading: false,
      columns: [],
      rows: []
    };
  },
  computed: {
    tableName() {
      return this.$route.params.tableName;
    }
  },
  watch: {
    tableName: {
      immediate: true,
      handler() {
        this.loadData();
      }
    }
  },
  methods: {
    async loadData() {
      this.loading = true;
      try {

        let para = {
          pageNum:1,
          pageSize:10,
          sql:this.tableName
        }
        // await api.getTableData(para);
        // rows = db.prepare(`SELECT * FROM "${tableName}" LIMIT ?`).all(limit);

        //
        // this.columns = await api.getTableColumns(this.tableName);
        // this.rows = await api.getTableData(this.tableName);
      } catch (e) {
        this.$message.error('加载表数据失败');
      } finally {
        this.loading = false;
      }
    },
    openInScript() {
      this.$router.push({
        name: 'Script',
        query: {
          sql: `SELECT *
                FROM ${this.tableName};`
        }
      });
    }
  }
};
</script>

<style scoped>
.table-data-view {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.tab-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #e8e8e8;
  border-bottom: 1px solid #c0c0c0;
  height: 36px;
  padding-right: 8px;
}

.tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  height: 100%;
  font-size: 12px;
  background: #fff;
  border-right: 1px solid #c0c0c0;
  color: #333;
}

.content {
  flex: 1;
  overflow: hidden;
  padding: 8px;
}
</style>
