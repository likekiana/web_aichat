<!-- src/App.vue -->
<template>
  <a-config-provider>
    <router-view />
  </a-config-provider>
</template>

<script setup lang="ts">
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f0f2f5;
}

#app {
  min-height: 100vh;
}
</style>