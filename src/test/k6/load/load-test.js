import http from 'k6/http';
import { sleep, check } from 'k6';

// 트래픽 시뮬레이션
export const options = {
  stages: [
    { duration: '30s', target: 20 },  // 20명으로 증가
    { duration: '1m', target: 50 },   // 50명 유지
    { duration: '30s', target: 0 },   // 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.1'],
  },
};

export default function () {
  const url = 'http://localhost:8080/auth/signup';

  const uniqueEmail = `load-${__VU}-${__ITER}-${Date.now()}@test.com`;

  const payload = JSON.stringify({
    email: uniqueEmail,
    password: 'Test1234!@',
    nickname: `load-${__VU}`,
    name: '부하테스트',
  });

  const res = http.post(url, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });

  sleep(1);
}