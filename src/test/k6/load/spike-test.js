import http from 'k6/http';
import { sleep, check } from 'k6';

// 트래픽 급증 대응 테스트
export const options = {
  stages: [
    { duration: '10s', target: 100 }, // 갑자기 100명
    { duration: '1m', target: 100 },  // 1분 유지
    { duration: '10s', target: 0 },   // 갑자기 종료
  ],
};

export default function () {
  const url = 'http://localhost:8080/auth/signup';

  const uniqueEmail = `spike-${__VU}-${__ITER}-${Date.now()}@test.com`;

  const payload = JSON.stringify({
    email: uniqueEmail,
    password: 'Test1234!@',
    nickname: `spike-${__VU}`,
    name: '급증테스트',
  });

  const res = http.post(url, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(1);
}