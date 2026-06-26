import Vue from 'vue';
import VueRouter from 'vue-router';
import Login from '../views/Login.vue';
import DB from "@/views/DB.vue";
import CalView from "@/components/util/CalView.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    redirect: '/DB'
  },
  {
    path: '/DB',
    name: 'DB',
    component: DB
  },
  {
    path: '/Cal',
    name: 'DB',
    component: CalView
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
    next();
    return;
  }
  // 未登录
  if (!token) {
    next('/login');
    return;
  }
  next();
});

export default router;
