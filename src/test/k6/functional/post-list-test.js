import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 50 },
    { duration: '1m', target: 100 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95%가 500ms 이하
  },
};

// 테스트 시작 전 1번만 실행 - 토큰 발급
export function setup() {
  const loginPayload = JSON.stringify({
    email: 'test@example.com',  // 실제 존재하는 계정
    password: 'Test1234!@'
  });

  const loginRes = http.post(
      'http://localhost:8080/auth/login',
      loginPayload,
      { headers: { 'Content-Type': 'application/json' } }
  );

  if (loginRes.status !== 200) {
    console.error('Login failed:', loginRes.status, loginRes.body);
    throw new Error('Failed to login');
  }

  const responseBody = JSON.parse(loginRes.body);
  const token = responseBody.data.accessToken;

  console.log('✅ Token acquired successfully');

  return { token }; // 반드시 객체로 반환
}

// 각 VU가 반복 실행하는 함수
export default function (data) {
  // data가 undefined인 경우 체크
  if (!data || !data.token) {
    console.error('❌ Token not found in data');
    return;
  }

  const params = {
    headers: {
      'Authorization': `Bearer ${data.token}`,
    },
  };

  const res = http.get('http://localhost:8080/posts?page=0&size=20', params);

  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
    'has posts': (r) => {
      try {
        const body = JSON.parse(r.body);
        return body.success === true && body.data;
      } catch (e) {
        return false;
      }
    },
  });

  sleep(1);
}