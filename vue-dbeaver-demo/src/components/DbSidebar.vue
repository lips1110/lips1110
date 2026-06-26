<template>
  <div class="db-sidebar">
    <div class="search-box">
      <el-input
          v-model="filterText"
          placeholder="搜索数据库对象..."
          prefix-icon="el-icon-search"
          size="small"
          clearable
      />
    </div>
    <!--树形图-->
    <div class="tree-container" v-loading="loading">
      <el-tree
          ref="tree"
          :data="treeData"
          :props="treeProps"
          :filter-node-method="filterNode"
          node-key="id"
          highlight-current
          @node-click="handleNodeClick"
      >
  <span
      slot-scope="{ node, data }"
      class="tree-node"
      style="cursor: grab;"
  >
    <i :class="getNodeIcon(data)"/>
    <span class="tree-label">{{ node.label }}</span>
  </span>
      </el-tree>
    </div>
  </div>
</template>

<script>
import api from '../api';

export default {
  name: 'DbSidebar',
  data() {
    return {
      filterText: '',
      treeData: [],
      loading: false,
      treeProps: {
        children: 'children',
        label: 'label'
      }
    };
  },
  watch: {
    filterText(val) {
      this.$refs.tree.filter(val);
    }
  },
  mounted() {
    this.loadTree();
  },
  methods: {
    async loadTree() {
      this.loading = true;
      try {
        this.treeData = await api.getTree();
        let tables = this.treeData;
        //获取TABLE的列
        const tableNodes = tables.map((name) => ({
          id: `table-${name}`,
          label: name,
          type: 'table',
          tableName: name,
          children: [
            {id: `${name}-columns`, label: '列', type: 'folder', tableName: name, metaType: 'columns', children: []},
          ]
        }));
        this.treeData = [
          {
            id: 'database-root',
            label: '达梦数据库',
            type: 'database',
            children: [
              {
                id: 'tables-folder',
                label: '表',
                type: 'folder',
                children: tableNodes
              }
            ]
          }
        ];
      } catch (e) {
        this.$message.error('加载数据库树失败');
      } finally {
        this.loading = false;
      }
    },
    filterNode(value, data) {
      if (!value) return true;
      return data.label.toLowerCase().includes(value.toLowerCase());
    },
    handleNodeClick(data) {
      this.$emit('node-click', data);
    },
    getNodeIcon(data) {
      const icons = {
        database: 'el-icon-coin node-icon-db',
        folder: 'el-icon-folder node-icon-folder',
        table: 'el-icon-s-grid node-icon-table'
      };
      return icons[data.type] || 'el-icon-document node-icon-default';
    }
  }
};
</script>

<style scoped>
.db-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.search-box {
  padding: 8px;
  border-bottom: 1px solid #e0e0e0;
}

.tree-container {
  flex: 1;
  overflow-x: auto;
  overflow-y: auto;
  padding: 4px 0;
}
.tree-node {
  display: flex;
  align-items: center;
  font-size: 13px;
}

.tree-label {
  margin-left: 4px;
}
/* 关键 */
.tree-container ::v-deep .el-tree {
  min-width: max-content;
}

.node-icon-db {
  color: #409eff;
}

.node-icon-folder {
  color: #e6a23c;
}

.node-icon-table {
  color: #67c23a;
}

.node-icon-default {
  color: #909399;
}
</style>
