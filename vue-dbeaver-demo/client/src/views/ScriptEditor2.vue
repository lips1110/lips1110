<template>
  <div>
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
            title="新建脚本"
            @click="openScript"
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
      >
        退出
      </el-button>
    </header>

    <div class="main-layout">
      <aside class="sidebar">
        <DbSidebar
            ref="sidebar"
            @node-click="onNodeClick"
        />
      </aside>

      <main class="workspace">
        <keep-alive>
          <router-view />
        </keep-alive>
      </main>
    </div>


  </div>
</template>

<script>
import {defineComponent} from "vue";
import DbSidebar from "@/components/DbSidebar.vue";

export default defineComponent({
  components: {DbSidebar}
})


</script>
