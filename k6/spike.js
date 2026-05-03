import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';
import { BASE_URL, STOCK_NAME, INITIAL_BANK_QUANTITY } from './config.js';

const rateLimited  = new Counter('rate_limited_429');
const conflicted   = new Counter('optimistic_lock_409');
const serverErrors = new Counter('server_errors_5xx');

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
        'server_errors_5xx': ['count<5'],
        'http_req_duration{expected_response:true}': ['p(95)<2000'],
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
    const walletId = `wallet-${__VU}`;
    const headers  = { 'Content-Type': 'application/json' };

    const res = http.post(
        `${BASE_URL}/wallets/${walletId}/stocks/${STOCK_NAME}`,
        JSON.stringify({ type: 'buy' }),
        { headers }
    );

    check(res, {
        'not a server error': (r) => r.status < 500,
        'valid response code': (r) => [200, 400, 409, 429].includes(r.status),
    });

    if (res.status === 429) rateLimited.add(1);
    if (res.status === 409) conflicted.add(1);
    if (res.status >= 500) {
        serverErrors.add(1);
        console.error(`500 error: ${res.body}`);
    }

    sleep(0.05);
}

export function teardown() {
    const res    = http.get(`${BASE_URL}/stocks`);
    const bank   = JSON.parse(res.body);
    const aapl   = (bank.stocks || []).find(s => s.name === STOCK_NAME);
    const logRes = http.get(`${BASE_URL}/log`);
    const log    = (JSON.parse(logRes.body).log || []);
    const buys   = log.filter(e => e.type === 'buy').length;
    const sells  = log.filter(e => e.type === 'sell').length;
    const expected = INITIAL_BANK_QUANTITY - buys + sells;

    console.log(`\n=== Results ===`);
    console.log(`Rate limited (429):       ${rateLimited.name}`);
    console.log(`Optimistic conflicts (409): ${conflicted.name}`);
    console.log(`Server errors (5xx):      ${serverErrors.name}`);
    console.log(`Buys logged:              ${buys}`);
    console.log(`Sells logged:             ${sells}`);
    console.log(`Bank quantity:            ${aapl ? aapl.quantity : 'NOT FOUND'}`);
    console.log(`Expected bank:            ${expected}`);

    if (aapl && aapl.quantity === expected) {
        console.log('Stock conservation invariant: PASSED ✅');
    } else {
        console.error('Stock conservation invariant: FAILED ❌ — race condition present!');
    }
}