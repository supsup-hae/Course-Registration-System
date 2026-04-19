import http from 'k6/http';
import {check, sleep} from 'k6';

// 조회 API: p50 < 50ms, p95 < 100ms, p99 < 200ms
// Error Rate (5xx): < 1%
// 처리량 목표: 기본 200 req/s → 목표 500 req/s
export const options = {
    thresholds: {
        'http_req_duration{api_type:read}': ['p(50)<50', 'p(95)<100', 'p(99)<200'],
        'http_req_failed{api_type:read}': ['rate<0.01'],
        'http_reqs{api_type:read}': ['rate>500'],
    },

    scenarios: {
        read_api: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 100 },  // 기본 처리량 (200 req/s 목표)
                { duration: '20s', target: 300 },  // 목표 처리량 (500 req/s 목표)
                { duration: '20s', target: 2000 }, // 개강 직전 burst 패턴
                { duration: '10s', target: 0 },    // 회복
            ],
        },
    },
};

export default function () {
    const userId = __VU + 100;
    const params = {
        headers: {
            'X-User-Id': `${userId}`,
            'X-User-Role': 'STUDENT',
        },
        tags: { api_type: 'read' },
    };

    const res = http.get('http://localhost:8080/api/v1/courses/1', params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}
