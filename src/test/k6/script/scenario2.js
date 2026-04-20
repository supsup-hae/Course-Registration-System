import http from 'k6/http';
import {check, sleep} from 'k6';

export const options = {
    vus: 2000,
    duration: '10s',
};

export default function () {
    const userId = __VU + 100;
    const params = {
        headers: {
            'X-User-Id': `${userId}`,
            'X-User-Role': 'STUDENT',
        },
        tags: {api_type: 'read'},
    };

    const res = http.get('http://localhost:8080/api/v1/courses/1', params);

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(1);
}