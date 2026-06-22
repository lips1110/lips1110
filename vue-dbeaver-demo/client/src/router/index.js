import Vue from 'vue';
import VueRouter from 'vue-router';
import ScriptEditor from '../views/ScriptEditor.vue';
import TableData from '../views/TableData.vue';
import TableColumns from '../views/TableColumns.vue';
import Login from '../views/Login.vue';
import ScriptEditor2 from "@/views/ScriptEditor2.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    redirect: '/script'
  },

  {
    path: '/script',
    name: 'Script',
    component: ScriptEditor
  },
  {
    path: '/script2',
    name: 'Script2',
    component: ScriptEditor2
  },
  {
    path: '/table/:tableName/data',
    name: 'TableData',
    component: TableData
  },
  {
    path: '/table/:tableName/columns',
    name: 'TableColumns',
    component: TableColumns
  }
];

const router = new VueRouter({
  mode: 'hash',
  routes
});

// 登录拦截
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  // 登录页直接放行
  if (to.path === '/login') {
    next('/script2');
    return;
  }
  // 未登录
  if (!token) {
    next('/login');
    return;
  }

  next('/script2');
});

export default router;
