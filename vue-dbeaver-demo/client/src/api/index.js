import axios from 'axios';

const http = axios.create({
  baseURL: '/db',
  timeout: 25000
});

http.interceptors.request.use(
    config => {

      const token = localStorage.getItem('token');

      if (token) {
        config.headers['token'] = token;
      }

      return config;
    },
    error => {
      return Promise.reject(error);
    }
);

export default {
  health() {
    return http.get('/health').then((r) => r.data);
  },
  getTree() {
    return http.get('/tables').then((r) => r.data);
  },
  getTableColumns(tableName) {
    return http.get(`/tables/${tableName}/columns`).then((r) => r.data);
  },
  getTableData(tableName,param) {
    return http.post(`/tables/data`,param).then((r) => r.data);
  },
  executeSql(para) {
    return http.post('/execute',para ).then((r) => r.data);
  },
  executeAll(sql) {
    return http.post('/execute-all', { sql }).then((r) => r.data);
  },
  exportAllApi(para) {
    return http.post(
        '/exportAllApi',
        para,
        {
          responseType: 'blob'
        }
    ).then((r) => r.data);
  },
  login(para) {
    return http.post('/login/submit',para ).then((r) => r.data);
  },
};
