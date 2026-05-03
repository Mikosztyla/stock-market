import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, STOCK_NAME, INITIAL_BANK_QUANTITY } from './config.js';

export const options = {
    scenarios: {
        spike: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '5s',  target: 5   },
                { duration: '5s',  target: 100 },
                { duration: '30s', target: 100 },
                { duration: '5s',  target: 0   },
            ],
        },
    },
    thresholds: {
        http_req_failed:   ['rate<0.50'],
        http_req_duration: ['p(95)<5000'],
    },
};

export function setup() {
    const res = http.post(`${BASE_URL}/stocks`,
        JSON.stringify({ stocks: [{ name: STOCK_NAME, quantity: INITIAL_BANK_QUANTITY }] }),
        { headers: { 'Content-Type': 'application/json' } }
    );
    check(res, { 'bank seeded': (r) => r.status === 200 });
}

export default function () {
    const walletId = `wallet-${__VU}-${__ITER}`;
    const headers  = { 'Content-Type': 'application/json' };

    const res = http.post(
        `${BASE_URL}/wallets/${walletId}/stocks/${STOCK_NAME}`,
        JSON.stringify({ type: 'buy' }),
        { headers }
    );

    check(res, {
        'status not 5xx': (r) => r.status < 500,
    });

    if (res.status >= 500) {
        console.error(`500 error: ${res.body}`);
    }

    sleep(0.05);
}