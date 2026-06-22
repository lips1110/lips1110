<template>
  <div class="script-editor-view">
    <!-- 多 Tab SQL 编辑器（方案A：每个Tab独立结果） -->
    <!-- 请将你当前 ScriptEditor.vue 中所有 sql/result 相关状态改为 tabs 数组存储 -->
    <!-- 由于原文件较长，此文件提供完整改造骨架与核心实现 -->

    <div class="tab-bar">
      <div
          v-for="tab in tabs"
          :key="tab.name"
          class="tab"
          :class="{ active: activeTab === tab.name }"
          @click="activeTab = tab.name"
      >
        {{ tab.title }}
        <i
            v-if="tabs.length > 1"
            class="el-icon-close"
            @click.stop="removeTab(tab.name)"
        />
      </div>

      <div v-if="tabs.length < 5" class="tab-add" @click="addTab">+</div>
    </div>

    <div class="editor-area">
      <SqlEditor
          ref="sqlEditor"
          v-model="currentTab.sql"
      />
    </div>

    <div class="result-panel">
      <ResultTable
          v-if="currentTab.singleResult"
          :data="currentTab.singleResult"
      />
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      activeTab: 'tab1',
      tabIndex: 1,
      tabs: [
        {
          name: 'tab1',
          title: 'SQL 1',
          sql: '-- 注释',
          singleResult: null,
          batchResults: [],
          lastResult: null,
          error: null,
          pageNum: 1,
          pageSize: 10,
          exportSql: ''
        }
      ]
    }
  },

  computed: {
    currentTab() {
      return this.tabs.find(t => t.name === this.activeTab)
    }
  },

  methods: {
    addTab() {
      if (this.tabs.length >= 5) return

      this.tabIndex++

      const name = 'tab' + this.tabIndex

      this.tabs.push({
        name,
        title: 'SQL ' + this.tabIndex,
        sql: '-- 注释',
        singleResult: null,
        batchResults: [],
        lastResult: null,
        error: null,
        pageNum: 1,
        pageSize: 10,
        exportSql: ''
      })

      this.activeTab = name
    },

    removeTab(name) {
      if (this.tabs.length === 1) return

      const idx = this.tabs.findIndex(v => v.name === name)

      this.tabs.splice(idx, 1)

      if (this.activeTab === name) {
        this.activeTab = this.tabs[Math.max(0, idx - 1)].name
      }
    },

    async executeCurrent() {
      const sql = this.currentTab.sql

      // 查询后写入当前Tab
      this.currentTab.singleResult = {}
      this.currentTab.lastResult = {}
      this.currentTab.exportSql = sql
    }
  }
}
</script>
