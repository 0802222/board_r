import http from 'k6/http';
import { sleep, check } from 'k6';

// 기능 테스트 - 비동기 동작 확인
export const options = {
  vus: 5,          // 가상 사용자 (Virtual Users)
  iterations: 10,   // 총 몇 번 반복할지
}; // N명의 vus 가 N번의 iterations 를 나눠서 수행함 (10 / 5 = 2)

export default function () {
  const url = 'http://localhost:8080/auth/signup';

  const uniqueEmail = `test-${__VU}-${__ITER}-${Date.now()}@test.com`;

  const payload = JSON.stringify({
    email: uniqueEmail,
    password: 'Test1234!@',
    nickname: `tester-${__VU}-${__ITER}`,
    name: '테스터',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  const res = http.post(url, payload, params);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'success is true': (r) => JSON.parse(r.body).success === true,
  });

  sleep(1);
}
