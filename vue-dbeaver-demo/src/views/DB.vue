<template>
  <div id="app">
    <!-- 登录页 -->
    <router-view v-if="$route.path === '/login'"/>

    <!-- 主界面 -->
    <div
        v-if="$route.path !== '/login'"
        id="app2"
        class="dbeaver-app"
    >
      <header class="top-toolbar">
        <div class="toolbar-left">
          <el-button
              type="text"
              icon="el-icon-connection"
              title="连接数据库"
              @click="checkConnection"
          />

          <el-button
              type="text"
              icon="el-icon-refresh"
              title="刷新"
              @click="refreshTree"
          />

          <el-button
              type="text"
              icon="el-icon-document"
              title="查看历史记录"
              @click="openScript"
          />
          <el-button
              type="text"
              icon="el-icon-minus"
              title="计算器"
              @click="openCal"
          />
        </div>

        <div class="toolbar-title">(SQLite)</div>

        <div class="toolbar-right">
          <el-tag
              :type="connected ? 'success' : 'danger'"
              size="small"
          >
            {{ connected ? '已连接' : '未连接' }}
          </el-tag>
        </div>

        <el-button
            type="text"
            icon="el-icon-switch-button"
            title="退出登录"
            @click="logout"
            style="color: red"
        >
          退出
        </el-button>
      </header>

      <div class="main-layout">
        <!-- 侧边-->
        <aside class="sidebar">
          <DbSidebar
              ref="sidebar"
              @node-click="onNodeClick"
          />
        </aside>
        <el-dialog
            :title="titleName"
            :visible.sync="dialogVisible"
            :width="isTable==='cal' ? '400px' : '60%'"
            height="60%"
            @close="handleClose"
        >
            <!-- 👇 子组件 -->
            <TableColumns v-if="isTable ==='columns'"
                          :table-name="currentTableName || ''"
                          @success="handleSuccess"
                          @cancel="dialogVisible = false"
                          @open-sql="setSql"
            />
            <HistoryView :history-sql="historySql" v-else-if="isTable ==='history'"></HistoryView>
            <CalView  v-else></CalView>
        </el-dialog>
        <!--主界面-->
        <main class="workspace">
          <ScriptEditor :sqlInput="activeSql || ''"></ScriptEditor>
        </main>
      </div>
    </div>
  </div>
</template>

<script>
import DbSidebar from '../components/DbSidebar.vue'
import api from '../api'
import ScriptEditor from "@/components/ScriptEditor.vue";
import TableColumns from "@/components/TableColumns.vue";
import SqlEditor from "@/components/SqlEditor.vue";
import HistoryView from "@/components/HistoryView.vue";
import CalView from '@/components/util/CalView.vue'

export default {
  name: 'DB',

  components: {
    HistoryView,
    SqlEditor,
    TableColumns,
    ScriptEditor,
    DbSidebar,
    CalView
  },

  watch: {
    dialogVisible(val) {
    }
  },

  data() {
    return {
      titleName: '',
      isTable: '',
      connected: false,
      dialogVisible: false,
      currentTableName: null,
      activeSql: null,
      historySql: ""
    }
  },

  mounted() {
    const token = localStorage.getItem('token')
    if (token) {
      this.checkConnection()
    }
  },

  methods: {
    async copyToClipboard(text) {
      await navigator.clipboard.writeText(text);
      this.$message.success('已复制到粘贴板')
    },
    setSql(sql,columns) {
      this.copyToClipboard(sql);
    },
    openCal() {
      this.isTable = 'cal';
      this.titleName = '计算器';
      this.dialogVisible = true;
    },
    openDialog(tableName) {
      this.currentTableName = tableName;
      this.dialogVisible = true;
    },

    handleSuccess(data) {
      this.dialogVisible = false;
    },

    handleClose() {
      this.currentTableName = null;
      this.dialogVisible = false;
    },

    logout() {
      localStorage.removeItem('token')
      this.connected = false
      this.$message.success('已退出登录')
      this.$router.replace('/login')
    },

    async checkConnection() {
      const token = localStorage.getItem('token')
      if (!token) {
        this.connected = false
        return
      }
      try {
        await api.health()
        this.connected = true
      } catch (e) {
        this.connected = false
        this.$message.error('无法连接后端，请启动 server')
      }
    },

    refreshTree() {
      const token = localStorage.getItem('token')
      if (!token) {
        return
      }
      if (this.$refs.sidebar) {
        this.$refs.sidebar.loadTree()
      }
      this.$message.success('已刷新')
    },

    openScript() {
      this.isTable = 'history';
      this.titleName = '历史记录';
      this.dialogVisible = true;
      this.historySql = localStorage.getItem('sqlContent')
    },

    onNodeClick(node) {
      if (node.type === 'folder' && node.metaType && node.metaType === 'columns') {
        this.isTable = 'columns';
        this.titleName = '列信息';
        this.openDialog(node.tableName)
      }
    }
  }
}
</script>

<style>
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

html,
body,
#app {
  height: 100vh;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
}

.dbeaver-app {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f0f0f0;
}

.top-toolbar {
  display: flex;
  align-items: center;
  height: 38px;
  padding: 0 10px;
  background: linear-gradient(180deg, #fafafa 0%, #e8e8e8 100%);
  border-bottom: 2px solid #c0c0c0;
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 2px;
}

.toolbar-title {
  flex: 1;
  text-align: center;
  font-size: 13px;
  color: #333;
}

.main-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  width: 260px;
  background: #fff;
  border-right: 1px solid #c0c0c0;
  display: flex;
  flex-direction: column;

}

.workspace {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
}
</style>
