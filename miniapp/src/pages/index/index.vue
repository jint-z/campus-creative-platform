<script setup lang="ts">
import { onLoad } from '@dcloudio/uni-app'
import { ref } from 'vue'
import { checkProbe, type Probe, type ProbeResult } from '../../api'

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

onLoad(() => void refresh())
</script>

<template>
  <view class="screen">
    <view class="heading">开发环境联通验证</view>
    <view class="probes">
      <view v-for="probe in probes" :key="probe.id" class="probe">
        <view class="probe-heading">
          <text>{{ probe.label }}</text>
          <text
            :class="
              results[probe.id]
                ? results[probe.id]?.ok
                  ? 'success'
                  : 'failure'
                : ''
            "
          >
            {{
              results[probe.id]
                ? results[probe.id]?.ok
                  ? '通过'
                  : '失败'
                : '检查中'
            }}
          </text>
        </view>
        <view v-if="results[probe.id]" class="detail">
          {{ results[probe.id]?.message }}（{{ results[probe.id]?.code }}）
        </view>
      </view>
    </view>
    <button :loading="pending" :disabled="pending" @tap="refresh">
      重新检查
    </button>
  </view>
</template>

<style scoped>
.screen {
  padding: 48rpx 32rpx;
}

.heading {
  margin-bottom: 32rpx;
  font-size: 44rpx;
  font-weight: 600;
}

.probes {
  margin-bottom: 32rpx;
  border: 1px solid #dce6e6;
  border-radius: 16rpx;
  background: #fff;
}

.probe {
  padding: 28rpx;
}

.probe + .probe {
  border-top: 1px solid #dce6e6;
}

.probe-heading {
  display: flex;
  justify-content: space-between;
  gap: 16rpx;
  font-weight: 600;
}

.detail {
  margin-top: 12rpx;
  color: #4a5b61;
  font-size: 26rpx;
}

.success {
  color: #166444;
}

.failure {
  color: #a12831;
}
</style>
