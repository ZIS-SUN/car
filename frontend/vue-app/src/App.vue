<template>
  <div id="app" class="harmony-app">
    <AppHeader v-if="!hidePublicLayout" />
    <main class="harmony-main" :class="{ 'harmony-main--embedded': hidePublicLayout }">
      <router-view />
    </main>
    <AppFooter v-if="!hidePublicLayout" />
  </div>
</template>

<script>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import AppFooter from './components/AppFooter.vue'

export default {
  name: 'App',
  components: {
    AppHeader,
    AppFooter
  },
  setup() {
    const route = useRoute()
    const hidePublicLayout = computed(() => {
      const path = route.path || ''
      // 只有 /shop/ 开头的路径才隐藏公共布局，/shops 是用户端门店列表页
      return path.startsWith('/shop/') || path.startsWith('/admin')
    })

    return {
      hidePublicLayout
    }
  }
}
</script>

<style>
/* 引入鸿蒙主题 */
@import './assets/css/harmonyos-theme.css';

#app {
  font-family: -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Helvetica Neue',
               'Segoe UI', 'Roboto', Arial, 'Noto Sans', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  color: var(--harmony-text-primary);
  background: var(--harmony-bg-secondary);
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.harmony-app {
  animation: harmony-fadeIn 0.6s ease-out;
}

.harmony-main {
  flex: 1;
  min-height: calc(100vh - 140px);
}

.harmony-main--embedded {
  min-height: 100vh;
  padding: 0;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
}

::-webkit-scrollbar-track {
  background: var(--harmony-bg-tertiary);
  border-radius: var(--harmony-radius-sm);
}

::-webkit-scrollbar-thumb {
  background: var(--harmony-primary);
  border-radius: var(--harmony-radius-sm);
}

::-webkit-scrollbar-thumb:hover {
  background: var(--harmony-primary-dark);
}
</style>