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
          <i @click="toggleResult" :class="showResult ? 'el-icon-arrow-down' : 'el-icon-arrow-up'"/>
          <span style="height: 20px;width: 50px" @click="toggleResult">执行结果</span>
          <el-tag v-if="lastResult" size="mini" type="info">{{ resultSummary }}</el-tag>
          <span v-if="showResult" class="resize-hint">拖动上方分隔条调整高度</span>
          <div v-if="showResult" class="resize-hint">
            <span @click="exportCurrent(lastExecuteType,exportSql, pageNum, pageSize)">导出当前结果</span>&nbsp;&nbsp;
            <span @click="exportCurrent(lastExecuteType,exportSql)">导出全部结果</span>
          </div>
        </div>
        <div v-show="showResult" class="result-body" v-loading="executing">
          <div v-if="error" class="result-error"><i class="el-icon-warning"/> {{ error }}</div>
          <div v-else-if="batchResults.length">
            <div v-for="(item, idx) in batchResults" :key="idx" class="batch-item">
              <el-tooltip placement="top" :content="item.sql">
                <div class="batch-sql">{{ item.sql }}</div>
              </el-tooltip>
              <ResultTable v-if="item.result.type === 'result'"
                           :page-size="pageSize"
                           @page-change="(p) => handlePageChange(p, idx)"
                           :data="item.result"/>
              <div v-else class="execute-msg">影响行数: {{ item.result.changes }}</div>
            </div>
          </div>

          <ResultTable v-else-if="singleResult"
                       :data="singleResult"
                       :current-page="pageNum"
                       :page-size="pageSize"
                       @page-change="handlePageChange"/>
          <div v-else class="result-empty">执行 SQL 后结果将显示在这里</div>
        </div>
      </div>
    </div>

    <el-dialog
        style="
  overflow-y: auto;"
        title="请选择导出的sql"
        :visible.sync="dialogVisible"
        width="30%"
        height="60%"
        @close="handleClose"
    >
      <ExportTip @export-current="exportTip"
                 :export-tip-sql="exportSqlList"></ExportTip>
    </el-dialog>
  </div>
</template>

<script>
import SqlEditor from './SqlEditor.vue';
import ResultTable from './ResultTable.vue';
import api from '../api';
import ExportTip from "@/components/export/ExportTip.vue";

const MIN_EDITOR_HEIGHT = 120;
const MIN_RESULT_HEIGHT = 120;
const RESIZE_BAR_HEIGHT = 6;

export default {
  name: 'ScriptEditor',
  components: {ExportTip, SqlEditor, ResultTable},
  props: {
    sqlInput: {
      type: String,
      required: true
    },
  },
  data() {
    return {
      dialogVisible: false,
      exportSqlList: [],
      sql: '',
      lastExecuteType: 'single',
      activeTab: 1,
      resultHeight: 240,
      isResizing: false,
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
  methods: {
    handleClose() {
      this.dialogVisible = false;
    },
    //是否为查询语句
    isSelectSQL(sql) {
      const firstKeyword = this.getFirstKeyword(sql.trim().toUpperCase());
      const isSelect = firstKeyword.startsWith('SHOW') || firstKeyword.startsWith('SELECT') || firstKeyword.startsWith('PRAGMA') || firstKeyword.startsWith('WITH');
      return isSelect;
    },
    handleKeyDown(e) {
      if (e.key === 'F7') {
        e.preventDefault();
        this.executeAll();
      }
      if (e.key === 'F8') {
        e.preventDefault();
        this.executeCurrent();
      }
    },
    //导出当前分页数据
    async exportCurrent(lastExecuteType, sql, pageNum, pageSize) {
      //获取最后一次执行sql的语句
      if (!sql) {
        this.$message.warning('当前结果为空');
        return;
      }
      if (lastExecuteType === 'all') {
        this.dialogVisible = true;
        //
        this.exportSqlList = this.exportSql.trim().split(';').map(s => s.trim()).filter(Boolean);
        console.log(this.exportSqlList)
      } else {
        await this.exportTip(pageNum, pageSize, sql)
      }


    },
    async exportTip(pageNum, pageSize, sql) {
      let parameter = {
        sql: sql,
        pageNum: pageNum,
        pageSize: pageSize
      }
      if (pageNum == null || pageSize == null) {
        this.$confirm(
            `确定导出全部数据吗？！`,
            '提示',
            {
              confirmButtonText: '保存',
              cancelButtonText: '取消',
              type: 'warning'
            }
        ).then(async () => {
          await this.exportExcel(parameter, '批量导出语句')
        })
      } else {
        await this.exportExcel(parameter, sql.substring(0, 50))
      }
    },
    // async handlePageChange(param) {
    //   // 更新分页参数
    //   this.pageNum = param.pageNum;
    //   this.pageSize = param.pageSize;
    //   if (this.lastExecuteType === 'single') {
    //     await this.executeCurrent(true);
    //   } else {
    //     await this.executeAll(true);
    //   }
    // },

    async handlePageChange({ page, size }, idx) {
      if (this.lastExecuteType === 'single') {
        this.pageNum = page;
        this.pageSize = size;
        await this.executeCurrent(true);
        return;
      }
      const oldItem = this.batchResults[idx];

      const newItem = {
        ...oldItem,
        page,
        pageSize: size
      };

      // 1️⃣ 先更新 UI 状态
      this.$set(this.batchResults, idx, newItem);
      // 2️⃣ 再请求数据
      this.executeCurrentSQL(oldItem.sql, page, size)
          .then(res => {
            let re = res[idx].result;
            console.log(JSON.stringify(re))
            this.$set(this.batchResults, idx, {
              ...newItem,
              result: {
                type: re.type,
                rows:re.rows,
                columns: re.columns,
                changes: re.changes
              }
          });
    })
    },
    executeCurrentSQL(sql,page,pageSize,idx){
      let par = {
        sql: sql,
        pageNum: page,
        pageSize: pageSize,
        index:idx
      }
      return  api.executeAll(par).then(res => res.data).catch(error => {
        this.$message.error(error.response.data.message || '执行失败');
        this.executing = false
        this.error = error.response.data.message
      });
    },
    async executeCurrent(flag) {
      this.exportSql = null;
      if (!flag) {
        this.pageNum = 1
      }
      if (this.pageNum == null) {
        this.pageNum = 100
      }
      const sql = this.$refs.sqlEditor.getCurrentStatement();
      if (!sql) {
        this.$message.warning('没有可执行的 SQL');
        return;
      }
      if (sql.toUpperCase().includes('UPDATE')) {
        this.$confirm(
            `确定执行UPDATE操作吗？点击确认后无法撤销！请仔细检查`,
            '提示',
            {
              confirmButtonText: '保存',
              cancelButtonText: '取消',
              type: 'warning'
            }
        ).then(async () => {
          await this.runSql(sql, false);
        })
      } else {
        await this.runSql(sql, false);
      }

    },
    async executeAll(flag) {
      this.exportSql = null;
      const sql = this.$refs.sqlEditor.getCurrentStatement();
      let selectFlag = true;
      sql.trim().split(';').map(s => {
        let trim = s.trim();
        if (this.getFirstKeyword(trim.toUpperCase()).startsWith('UPDATE')) {
          this.$message.warning('查询全部语句禁止包含UPDATE操作');
          selectFlag = false
        }
      })
      if (!selectFlag) {
        return;
      }
      if (!flag) {
        this.pageNum = 1
      }
      if (!this.sql.trim()) {
        this.$message.warning('编辑器为空');
        return;
      }

      await this.runSql(this.sql, true);
    },
    getFirstKeyword(upper) {
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
    },

    async runSql(sql, isAll) {
      this.executing = true;
      this.error = null;
      //单行查询
      this.singleResult = null;
      //多行查询
      this.batchResults = [];

      let par = {
        sql: sql.trim(),
        pageNum: this.pageNum,
        pageSize: this.pageSize
      }

      //查询全部 只能进行查询
      if (isAll) {
        let data;
        //是否是查询语句
        const isSelect = this.isSelectSQL(sql);
        if (!isSelect) {
          this.$message.error(`不能执行查询以外的操作`);
          return;
        }
        data = await api.executeAll(par).then(res => res.data).catch(err => {
          this.$message.error(err.response.data.message || '执行失败');
          this.executing = false
          this.error = err.response.data.message
        });
        this.batchResults = data;
        this.lastExecuteType = 'all';
        this.exportSql = sql.trim();
        this.lastResult = {type: 'batch', count: data.length};
        this.$message.success(`已执行 ${data.length} 条语句`);
      } else {
        let data;
        let code = 200;
        let total = 0;
        let msg = '';
        let result = {};
        //是否是查询语句
        const isSelect = this.isSelectSQL(sql);
        if (isSelect) {
          result = await api.executeSql(par).then(res => res.data).catch(error => {
            this.$message.error(error.response.data.message || '执行失败');
            this.executing = false
            this.error = error.response.data.message
          });
          this.lastExecuteType = 'single';
          code = result.code;
          total = result.data.total;
          data = result.data;
          msg = result.message;
          this.singleResult = data;
        } else if (this.getFirstKeyword(sql.trim().toUpperCase()).startsWith('UPDATE')) {
          result = await api.executeSql(par).then(res => res.data).catch(error => {
            this.$message.error(error.response.data.message || '执行失败');
            this.executing = false
            this.error = error.response.data.message
          });
        } else {
          this.$message.error('目前不执行其它操作,请检查SQL语句');
          return
        }
        this.lastResult = data;
        let item = localStorage.getItem('sqlContent');
        localStorage.setItem('sqlContent', this.sql + " \n" + item);
        if (data.type === 'result') {
          this.exportSql = this.sql
          this.$message.success(`查询成功,返回 ${total} 行`);
        } else if (data.type === 'execute') {
          this.$message.success(`影响 ${data.changes} 行`);
        }
      }
      this.showResult = true;
      this.$nextTick(() => this.clampResultHeight());
      this.executing = false
    },
    async exportExcel(par, titleName) {
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
        link.download = '' + titleName + '.xlsx';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(link.href);
      } catch (e) {
        this.$message.error('导出失败');
      }
    },

    initResultHeight() {
      const container = this.$refs.container;
      if (!container) return;
      this.resultHeight = Math.round(container.clientHeight * 0.45);
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
    watch: {
      sqlInput: {
        immediate: true,
        handler(val) {
          this.sql = val || ''
        }
      }
    },
    '$route.query.sql': {
      immediate: true,
      handler(sql) {
        if (sql) {
          this.sql = sql;
        }
      }
    },
    sql: {
      immediate: true,
      handler(val) {
        this.$refs.sqlEditor?.setValue(val)
      }
    }
  },
  mounted() {
    this.initResultHeight();
    window.addEventListener('resize', this.clampResultHeight);
    window.addEventListener('keydown', this.handleKeyDown)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.clampResultHeight);
    this.stopResizeListeners();
    window.removeEventListener('keydown', this.handleKeyDown);
  },
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
  min-height: 100px;
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

  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.execute-msg {
  font-size: 13px;
  color: #67c23a;
  padding: 4px 0;
}
</style>
