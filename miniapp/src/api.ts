export type Probe = 'health' | 'db'
export type ProbeResult = { ok: boolean; code: string; message: string }

const baseUrl = (
  import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8080'
).replace(/\/+$/, '')

function readResponse(status: number, body: unknown): ProbeResult {
  if (
    typeof body === 'object' &&
    body !== null &&
    'code' in body &&
    'message' in body &&
    'data' in body &&
    typeof body.code === 'string' &&
    typeof body.message === 'string'
  ) {
    if (
      status >= 200 &&
      status < 300 &&
      body.code === 'OK' &&
      body.data !== null
    ) {
      return { ok: true, code: body.code, message: body.message }
    }
    if (body.code !== 'OK') {
      return { ok: false, code: body.code, message: body.message }
    }
  }
  return {
    ok: false,
    code: 'INVALID_RESPONSE',
    message: '接口返回与预期格式不符',
  }
}

export function checkProbe(probe: Probe): Promise<ProbeResult> {
  return new Promise((resolve) => {
    uni.request({
      url: baseUrl + '/api/dev/' + probe,
      method: 'GET',
      timeout: 6000,
      success: ({ statusCode, data }) =>
        resolve(readResponse(statusCode, data)),
      fail: () =>
        resolve({
          ok: false,
          code: 'REQUEST_FAILED',
          message: '无法访问后端，请检查服务和开发地址',
        }),
    })
  })
}
