<script setup lang="ts">
import { ElButton, ElTag } from 'element-plus'
import { onMounted, ref } from 'vue'
import { checkProbe, type Probe, type ProbeResult } from './api'

const probes: { id: Probe; label: string }[] = [
  { id: 'health', label: '后端服务' },
  { id: 'db', label: 'MySQL 数据库' },
]
const results = ref<Partial<Record<Probe, ProbeResult>>>({})
const pending = ref(false)

async function refresh() {
  if (pending.value) return

  pending.value = true
  results.value = {}
  try {
    await Promise.all(
      probes.map(async ({ id }) => {
        results.value[id] = await checkProbe(id)
      }),
    )
  } finally {
    pending.value = false
  }
}

onMounted(refresh)
</script>

<template>
  <main>
    <p class="section-label">校园创作分享平台 · 管理后台</p>
    <h1>开发环境联通验证</h1>
    <ul aria-live="polite">
      <li v-for="probe in probes" :key="probe.id">
        <div class="probe-heading">
          <strong>{{ probe.label }}</strong>
          <el-tag
            :type="
              results[probe.id]
                ? results[probe.id]?.ok
                  ? 'success'
                  : 'danger'
                : 'info'
            "
          >
            {{
              results[probe.id]
                ? results[probe.id]?.ok
                  ? '通过'
                  : '失败'
                : '检查中'
            }}
          </el-tag>
        </div>
        <p v-if="results[probe.id]" class="detail">
          {{ results[probe.id]?.message }}（{{ results[probe.id]?.code }}）
        </p>
      </li>
    </ul>
    <el-button type="primary" :loading="pending" @click="refresh"
      >重新检查</el-button
    >
  </main>
</template>

<style>
body {
  margin: 0;
  color: #17252d;
  background: #f5f8f8;
  font-family: 'Microsoft YaHei UI', 'PingFang SC', sans-serif;
}

main {
  box-sizing: border-box;
  max-width: 720px;
  margin: 64px auto;
  padding: 0 24px;
}

.section-label,
.detail {
  color: #4a5b61;
}

h1 {
  margin: 8px 0 24px;
  font-size: clamp(24px, 5vw, 32px);
}

ul {
  margin: 0 0 24px;
  padding: 0;
  list-style: none;
  background: #fff;
  border: 1px solid #dce6e6;
  border-radius: 8px;
}

li {
  padding: 16px;
}

li + li {
  border-top: 1px solid #dce6e6;
}

.probe-heading {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.detail {
  margin: 8px 0 0;
  overflow-wrap: anywhere;
}
</style>
