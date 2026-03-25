import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import ApplicationList from '../components/ApplicationList.vue'
import ConfigList from '../components/ConfigList.vue'
import StatusList from '../components/StatusList.vue'
import UserList from '../views/UserList.vue'
import RoleList from '../views/RoleList.vue'
import MenuList from '../views/MenuList.vue'
import AppRegistryList from '../views/AppRegistryList.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/',
    component: Layout,
    redirect: '/applications',
    children: [
      {
        path: 'applications',
        name: 'Applications',
        component: ApplicationList
      },
      {
        path: 'configs',
        name: 'Configs',
        component: ConfigList
      },
      {
        path: 'status',
        name: 'Status',
        component: StatusList
      },
      {
        path: 'users',
        name: 'Users',
        component: UserList
      },
      {
        path: 'roles',
        name: 'Roles',
        component: RoleList
      },
      {
        path: 'menus',
        name: 'Menus',
        component: MenuList
      },
      {
        path: 'app-registry',
        name: 'AppRegistry',
        component: AppRegistryList
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/')
  } else {
    next()
  }
})

export default router