<!--编辑器界面-->
<template>
  <div ref="container" class="script-editor-view">
    <div class="tab-bar">
      <div
          class="tab"
      >
        <i class="el-icon-document"/>
        当前sql查询页
      </div>

    </div>

    <div class="split-body">
      <div class="editor-area">

        <!-- 引入组件SqlEditor -->
        <SqlEditor
            ref="sqlEditor"
            v-model="sql"
            @execute-current="executeCurrent"
            @execute-all="executeAll"
        />
      </div>

      <div
          v-show="showResult"
          class="resize-bar"
          :class="{ active: isResizing }"
          @mousedown.prevent="startResize"
      >
        <span class="resize-grip"/>
      </div>

      <div
          class="result-panel"
          :class="{ collapsed: !showResult }"
          :style="resultPanelStyle"
      >
        <div class="result-header">
          <i :class="showResult ? 'el-icon-arrow-down' : 'el-icon-arrow-up'"/>
          <span @click="toggleResult">执行结果</span>
          <el-tag v-if="lastResult" size="mini" type="info">{{ resultSummary }}</el-tag>
          <span v-if="showResult" class="resize-hint">拖动上方分隔条调整高度</span>
          <div v-if="showResult" class="resize-hint">
            <span @click="exportCurrent">导出当前结果</span>&nbsp;&nbsp;
            <span @click="exportAll">导出全部结果</span>
          </div>

        </div>
        <div v-show="showResult" class="result-body" v-loading="executing">
          <div v-if="error" class="result-error">
            <i class="el-icon-warning"/> {{ error }}
          </div>
          <div v-else-if="batchResults.length">
            <div v-for="(item, idx) in batchResults" :key="idx" class="batch-item">
              <div class="batch-sql">{{ item.sql }}</div>
              <ResultTable v-if="item.result.type === 'result'" :data="item.result"/>
              <div v-else class="execute-msg">影响行数: {{ item.result.changes }}</div>
            </div>
          </div>

          <ResultTable v-else-if="singleResult"
                       :data="singleResult"
                       :current-page="pageNum"
                       :page-size="pageSize"
                       @page-change="handlePageChange"/>


          <div v-else="singleResult" class="result-empty">执行 SQL 后结果将显示在这里</div>
        </div>
      </div>
    </div>


  </div>
</template>

<script>
import SqlEditor from '../components/SqlEditor.vue';
import ResultTable from '../components/ResultTable.vue';
import api from '../api';

const DEFAULT_SQL = `-- 注释`;
const MIN_EDITOR_HEIGHT = 120;
const MIN_RESULT_HEIGHT = 120;
const RESIZE_BAR_HEIGHT = 6;

async function extracted(par) {
  try {
    const res = await api.exportAllApi(par);
    const blob = new Blob([
      res
    ], {
      type:
          'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    });
    const link = document.createElement('a');
    link.href = window.URL.createObjectURL(blob);
    link.download = 'sql_result.xlsx';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(link.href);
  } catch (e) {
    console.error(e);
    this.$message.error('导出失败');
  }
}

export default {
  name: 'ScriptEditor',
  components: {SqlEditor, ResultTable},
  data() {
    return {
      activeTab: 1,
      resultHeight: 240,
      isResizing: false,
      sql: DEFAULT_SQL,
      executing: false,
      showResult: true,
      error: null,
      singleResult: null,
      batchResults: [],
      lastResult: null,
      pageNum: 1,
      pageSize: 10,
      exportSql: ''
    };
  },
  computed: {
    resultSummary() {
      if (!this.lastResult) return '';
      if (this.lastResult.type === 'batch') {
        return `${this.lastResult.count} 条语句`;
      }
      if (this.lastResult.type === 'result') {
        if (this.lastResult.rowCount == null) {
          return '0行';
        }
        return `${this.lastResult.rowCount} 行`;
      }
      return `影响 ${this.lastResult.changes} 行`;
    },
    resultPanelStyle() {
      if (!this.showResult) {
        return {};
      }
      return {
        height: `${this.resultHeight}px`,
        flexShrink: 0
      };
    }
  },
  watch: {
    '$route.query.sql': {
      immediate: true,
      handler(sql) {
        if (sql) {
          this.sql = sql;
        }
      }
    }
  },
  mounted() {
    this.initResultHeight();
    window.addEventListener('resize', this.clampResultHeight);
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.clampResultHeight);
    this.stopResizeListeners();
  },
  methods: {
    //导出当前分页数据
    async exportCurrent() {
      let par = {
        sql: this.exportSql,
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }
      await extracted.call(this, par);
    },
    //导出全部数据
    async exportAll() {
      let par = {
        sql: this.exportSql,
      }
      await extracted.call(this, par);
    },
    async handlePageChange(param) {
      // 更新分页参数
      this.pageNum = param.pageNum;
      this.pageSize = param.pageSize;

      await this.executeCurrent();
      console.log(this.pageNum, this.pageSize)
    },


    initResultHeight() {
      const container = this.$refs.container;
      if (!container) return;
      this.resultHeight = Math.round(container.clientHeight * 0.35);
      this.clampResultHeight();
    },
    getMaxResultHeight() {
      const container = this.$refs.container;
      if (!container) return 600;
      const tabBarHeight = 28;
      const resultHeaderHeight = 32;
      const available =
          container.clientHeight -
          tabBarHeight -
          RESIZE_BAR_HEIGHT -
          resultHeaderHeight -
          MIN_EDITOR_HEIGHT;

      return Math.max(MIN_RESULT_HEIGHT, available);
    },
    clampResultHeight() {
      const max = this.getMaxResultHeight();
      this.resultHeight = Math.min(max, Math.max(MIN_RESULT_HEIGHT, this.resultHeight));
    },
    toggleResult() {
      this.showResult = !this.showResult;
      if (this.showResult) {
        this.$nextTick(() => this.clampResultHeight());
      }
    },
    startResize(e) {
      this.isResizing = true;
      const startY = e.clientY;
      const startHeight = this.resultHeight;

      document.body.style.cursor = 'row-resize';
      document.body.style.userSelect = 'none';

      this._moveHandler = (event) => {
        const delta = startY - event.clientY;
        this.resultHeight = startHeight + delta;
        this.clampResultHeight();
      };

      this._upHandler = () => {
        this.stopResizeListeners();
        this.$nextTick(() => {
          if (this.$refs.sqlEditor) {
            this.$refs.sqlEditor.refresh();
          }
        });
      };

      document.addEventListener('mousemove', this._moveHandler);
      document.addEventListener('mouseup', this._upHandler);
    },
    stopResizeListeners() {
      this.isResizing = false;
      document.body.style.cursor = '';
      document.body.style.userSelect = '';

      if (this._moveHandler) {
        document.removeEventListener('mousemove', this._moveHandler);
        this._moveHandler = null;
      }
      if (this._upHandler) {
        document.removeEventListener('mouseup', this._upHandler);
        this._upHandler = null;
      }
    },
    async executeCurrent(resetPage = true) {
      if (resetPage) {
        this.pageNum = 1;
      }

      const sql = this.$refs.sqlEditor.getCurrentStatement();

      if (!sql) {
        this.$message.warning('没有可执行的 SQL');
        return;
      }

      await this.runSql(sql, false);
    },


    async executeAll() {
      if (!this.sql.trim()) {
        this.$message.warning('编辑器为空');
        return;
      }
      await this.runSql(this.sql, true);
    },


    async runSql(sql, isAll) {
      this.executing = true;
      this.error = null;
      this.singleResult = null;
      this.batchResults = [];

      function getFirstKeyword(upper) {
        if (!upper) {
          return '';
        }
        // 去掉 /* ... */
        upper = upper.replace(/\/\*[\s\S]*?\*\//g, '');
        // 去掉 -- 注释
        upper = upper.replace(/^\s*--.*$/gm, '');
        // 去掉首尾空格
        upper = upper.trim();
        const match = upper.match(/^(\w+)/i);
        return match ? match[1].toUpperCase() : '';
      }

      try {
        let par = {
          sql: sql,
          pageNum: this.pageNum,
          pageSize: this.pageSize
        }
        if (isAll) {
          const data = await api.executeAll(par);
          this.batchResults = data.results;
          this.lastResult = {type: 'batch', count: data.results.length};
          this.$message.success(`已执行 ${data.results.length} 条语句`);
        } else {
          let data;
          const trimmed = sql.trim();
          if (!trimmed) {
            throw new Error('SQL 语句不能为空');
          }
          const upper = trimmed.toUpperCase();
          const firstKeyword = getFirstKeyword(upper);
          const isSelect =
              firstKeyword.startsWith('SHOW') ||
              firstKeyword.startsWith('SELECT') ||
              firstKeyword.startsWith('PRAGMA') ||
              firstKeyword.startsWith('WITH');
          console.log(firstKeyword)
          if (firstKeyword == '') {
            this.$message.error(`查询不能为空`);
            return;
          }
          let code = 200;
          let total = 0;
          let msg = '';
          if (isSelect) {
            const stmt = await api.executeSql(par);
            const rows = stmt.rows;
            const columns = rows.length > 0 ? Object.keys(rows[0]) : [];
            data = {type: 'result', columns, rows, rowCount: stmt.total};
            code = stmt.code;
            total = stmt.total;
            console.log(total)
            msg = stmt.message;
          } else {
            const result = await api.executeSql(par);
            data = {
              type: 'execute',
              changes: result.changes,
              lastInsertRowid: result.lastInsertRowid
            };
          }
          this.singleResult = data;
          this.lastResult = data;
          if (code === 200) {
            if (data.type === 'result') {
              this.exportSql = this.sql
              this.$message.success(`查询成功,返回 ${total} 行`);
            } else if (data.type === 'execute') {
              this.$message.success(`影响 ${data.changes} 行`);
            }
          } else {
            this.$message.info('查询出错:' + msg);
          }

        }
        this.showResult = true;
        this.$nextTick(() => this.clampResultHeight());
      } catch (e) {
        this.error = e.message;
        this.$message.error(this.error);
      } finally {
        this.executing = false;
      }
    }
  }
};
</script>

<style scoped>
.script-editor-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.tab-bar {
  display: flex;
  flex-shrink: 0;
  background: #e8e8e8;
  border-bottom: 1px solid #c0c0c0;
  height: 28px;
}

.tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  font-size: 12px;
  background: #fff;
  border-right: 1px solid #c0c0c0;
  border-bottom: 1px solid #fff;
  margin-bottom: -1px;
  color: #333;
  font-weight: 500;
}

.split-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.editor-area {
  flex: 1;
  display: flex;
  min-height: 120px;
  overflow: hidden;
}

.resize-bar {
  flex-shrink: 0;
  height: 6px;
  cursor: row-resize;
  background: #dcdfe6;
  position: relative;
  z-index: 2;
  transition: background 0.15s;
}

.resize-bar:hover,
.resize-bar.active {
  background: #409eff;
}

.resize-grip {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  width: 36px;
  height: 2px;
  border-radius: 1px;
  background: rgba(255, 255, 255, 0.8);
  box-shadow: 0 -2px 0 rgba(255, 255, 255, 0.5), 0 2px 0 rgba(255, 255, 255, 0.5);
}

.result-panel {
  display: flex;
  flex-direction: column;
  background: #fafafa;
  border-top: 1px solid #c0c0c0;
  min-height: 0;
}

.result-panel.collapsed {
  flex-shrink: 0;
  height: auto !important;
}

.tab {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 12px;
  font-size: 12px;
  background: #f5f5f5;
  border-right: 1px solid #c0c0c0;
  border-bottom: 1px solid #c0c0c0;
  color: #666;
  cursor: pointer;
}

.tab.active {
  background: #fff;
  border-bottom: 2px solid #409EFF;
  color: #409EFF;
  font-weight: 500;
}

.result-header {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  height: 32px;
  padding: 0 12px;
  font-size: 12px;
  cursor: pointer;
  background: #f0f0f0;
  border-bottom: 1px solid #ddd;
  user-select: none;
}

.resize-hint {
  margin-left: auto;
  font-size: 11px;
  color: #909399;
}

.result-body {
  flex: 1;
  overflow: auto;
  padding: 8px;
  min-height: 0;
}

.result-error {
  color: #f56c6c;
  font-size: 13px;
}

.result-empty {
  color: #909399;
  font-size: 13px;
  text-align: center;
  padding: 20px;
}

.batch-item {
  margin-bottom: 12px;
}

.batch-sql {
  font-family: Consolas, monospace;
  font-size: 12px;
  color: #606266;
  margin-bottom: 4px;
  padding: 4px 8px;
  background: #f0f0f0;
  border-radius: 3px;
}

.execute-msg {
  font-size: 13px;
  color: #67c23a;
  padding: 4px 0;
}
</style>
