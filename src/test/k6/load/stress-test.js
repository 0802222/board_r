import http from 'k6/http';
import { sleep, check } from 'k6';

// 시스템 한계 테스트
export const options = {
  stages: [
    { duration: '1m', target: 50 },   // 50명
    { duration: '2m', target: 100 },  // 100명
    { duration: '2m', target: 100 },  // 100명 (유지)
    { duration: '1m', target: 0 },    // 종료
  ],
};

export default function () {
  const url = 'http://localhost:8080/auth/signup';

  const uniqueEmail = `stress-${__VU}-${__ITER}-${Date.now()}@test.com`;

  const payload = JSON.stringify({
    email: uniqueEmail,
    password: 'Test1234!@',
    nickname: `stress-${__VU}`,
    name: '스트레스테스트',
  });

  const res = http.post(url, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}