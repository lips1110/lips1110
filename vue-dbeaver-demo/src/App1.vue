<!--<template>-->
<!--  <div id="app">-->
<!--    &lt;!&ndash; 登录页 &ndash;&gt;-->
<!--    <router-view v-if="$route.path === '/login'" />-->

<!--    &lt;!&ndash; 主界面 &ndash;&gt;-->
<!--    <div-->
<!--        v-if="$route.path !== '/login'"-->
<!--        id="app2"-->
<!--        class="dbeaver-app"-->
<!--    >-->
<!--      <header class="top-toolbar">-->
<!--        <div class="toolbar-left">-->
<!--          <el-button-->
<!--              type="text"-->
<!--              icon="el-icon-connection"-->
<!--              title="连接数据库"-->
<!--              @click="checkConnection"-->
<!--          />-->

<!--          <el-button-->
<!--              type="text"-->
<!--              icon="el-icon-refresh"-->
<!--              title="刷新"-->
<!--              @click="refreshTree"-->
<!--          />-->

<!--          <el-button-->
<!--              type="text"-->
<!--              icon="el-icon-document"-->
<!--              title="新建脚本"-->
<!--              @click="openScript"-->
<!--          />-->
<!--        </div>-->

<!--        <div class="toolbar-title">(SQLite)</div>-->

<!--        <div class="toolbar-right">-->
<!--          <el-tag-->
<!--              :type="connected ? 'success' : 'danger'"-->
<!--              size="small"-->
<!--          >-->
<!--            {{ connected ? '已连接' : '未连接' }}-->
<!--          </el-tag>-->
<!--        </div>-->

<!--        <el-button-->
<!--            type="text"-->
<!--            icon="el-icon-switch-button"-->
<!--            title="退出登录"-->
<!--            @click="logout"-->
<!--        >-->
<!--          退出-->
<!--        </el-button>-->
<!--      </header>-->

<!--      <div class="main-layout">-->
<!--        <aside class="sidebar">-->
<!--          <DbSidebar-->
<!--              ref="sidebar"-->
<!--              @node-click="onNodeClick"-->
<!--          />-->
<!--        </aside>-->

<!--        <main class="workspace">-->
<!--          <keep-alive>-->
<!--            <router-view />-->
<!--          </keep-alive>-->
<!--        </main>-->
<!--      </div>-->
<!--    </div>-->
<!--  </div>-->
<!--</template>-->

<!--<script>-->
<!--import DbSidebar from './components/DbSidebar.vue'-->
<!--import api from './api'-->

<!--export default {-->
<!--  name: 'App',-->

<!--  components: {-->
<!--    DbSidebar-->
<!--  },-->

<!--  data() {-->
<!--    return {-->
<!--      connected: false-->
<!--    }-->
<!--  },-->

<!--  mounted() {-->
<!--    const token = localStorage.getItem('token')-->

<!--    if (token) {-->
<!--      this.checkConnection()-->
<!--    }-->
<!--  },-->

<!--  methods: {-->
<!--    logout() {-->
<!--      localStorage.removeItem('token')-->

<!--      this.connected = false-->

<!--      this.$message.success('已退出登录')-->

<!--      this.$router.replace('/login')-->
<!--    },-->

<!--    async checkConnection() {-->
<!--      const token = localStorage.getItem('token')-->

<!--      if (!token) {-->
<!--        this.connected = false-->
<!--        return-->
<!--      }-->

<!--      try {-->
<!--        await api.health()-->

<!--        this.connected = true-->
<!--      } catch (e) {-->
<!--        this.connected = false-->

<!--        this.$message.error('无法连接后端，请启动 server')-->
<!--      }-->
<!--    },-->

<!--    refreshTree() {-->
<!--      const token = localStorage.getItem('token')-->

<!--      if (!token) {-->
<!--        return-->
<!--      }-->

<!--      if (this.$refs.sidebar) {-->
<!--        this.$refs.sidebar.loadTree()-->
<!--      }-->

<!--      this.$message.success('已刷新')-->
<!--    },-->

<!--    openScript() {-->
<!--      if (this.$route.name !== 'Script') {-->
<!--        this.$router.push({-->
<!--          name: 'Script'-->
<!--        })-->
<!--      }-->
<!--    },-->

<!--    onNodeClick(node) {-->
<!--      if (node.type === 'table') {-->
<!--        if (-->
<!--            this.$route.name !== 'TableData' ||-->
<!--            this.$route.params.tableName !== node.tableName-->
<!--        ) {-->
<!--          this.$router.push({-->
<!--            name: 'TableData',-->
<!--            params: {-->
<!--              tableName: node.tableName-->
<!--            }-->
<!--          })-->
<!--        }-->

<!--      } else if (-->
<!--          node.type === 'folder' &&-->
<!--          node.metaType === 'columns' &&-->
<!--          node.tableName-->
<!--      ) {-->
<!--        if (-->
<!--            this.$route.name !== 'TableColumns' ||-->
<!--            this.$route.params.tableName !== node.tableName-->
<!--        ) {-->
<!--          this.$router.push({-->
<!--            name: 'TableColumns',-->
<!--            params: {-->
<!--              tableName: node.tableName-->
<!--            }-->
<!--          })-->
<!--        }-->

<!--      } else if (node.type === 'database') {-->

<!--        // ✔ 关键：防重复跳转-->
<!--        if (this.$route.name !== 'Script') {-->
<!--          this.$router.push({-->
<!--            name: 'Script'-->
<!--          })-->
<!--        }-->
<!--      }-->
<!--    }-->
<!--  }-->
<!--}-->
<!--</script>-->

<!--<style>-->
<!--* {-->
<!--  box-sizing: border-box;-->
<!--  margin: 0;-->
<!--  padding: 0;-->
<!--}-->

<!--html,-->
<!--body,-->
<!--#app {-->
<!--  height: 100%;-->
<!--  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;-->
<!--}-->

<!--.dbeaver-app {-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  height: 100%;-->
<!--  background: #f0f0f0;-->
<!--}-->

<!--.top-toolbar {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  height: 36px;-->
<!--  padding: 0 8px;-->
<!--  background: linear-gradient(180deg, #fafafa 0%, #e8e8e8 100%);-->
<!--  border-bottom: 1px solid #c0c0c0;-->
<!--}-->

<!--.toolbar-left,-->
<!--.toolbar-right {-->
<!--  display: flex;-->
<!--  align-items: center;-->
<!--  gap: 2px;-->
<!--}-->

<!--.toolbar-title {-->
<!--  flex: 1;-->
<!--  text-align: center;-->
<!--  font-size: 13px;-->
<!--  color: #333;-->
<!--}-->

<!--.main-layout {-->
<!--  display: flex;-->
<!--  flex: 1;-->
<!--  overflow: hidden;-->
<!--}-->

<!--.sidebar {-->
<!--  width: 260px;-->
<!--  min-width: 200px;-->
<!--  background: #fff;-->
<!--  border-right: 1px solid #c0c0c0;-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--}-->

<!--.workspace {-->
<!--  flex: 1;-->
<!--  display: flex;-->
<!--  flex-direction: column;-->
<!--  overflow: hidden;-->
<!--  background: #fff;-->
<!--}-->
<!--</style>-->
