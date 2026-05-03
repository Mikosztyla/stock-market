import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { BASE_URL, STOCK_NAME, INITIAL_BANK_QUANTITY } from './config.js';

const buyDuration  = new Trend('buy_duration',  true);
const sellDuration = new Trend('sell_duration', true);
const buyErrors    = new Counter('buy_errors');
const sellErrors   = new Counter('sell_errors');
const rateLimited  = new Counter('rate_limited_429');

export const options = {
    thresholds: {
        'http_req_duration{expected_response:true}': ['p(95)<2000'],
        'buy_errors':  ['count<5'],
        'sell_errors': ['count<5'],
    },
    scenarios: {
        ramp_up: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 10 },
                { duration: '20s', target: 10 },
                { duration: '10s', target: 50 },
                { duration: '20s', target: 50 },
                { duration: '10s', target: 0  },
            ],
        },
    },
};

export function setup() {
    const payload = JSON.stringify({
        stocks: [{ name: STOCK_NAME, quantity: INITIAL_BANK_QUANTITY }]
    });
    const res = http.post(`${BASE_URL}/stocks`, payload, {
        headers: { 'Content-Type': 'application/json' },
    });
    check(res, { 'bank seeded successfully': (r) => r.status === 200 });
    console.log(`Bank seeded with ${INITIAL_BANK_QUANTITY} units of ${STOCK_NAME}`);
}

export default function () {
    const walletId = `wallet-${__VU}`;
    const headers  = { 'Content-Type': 'application/json' };

    const buyStart = Date.now();
    const buyRes = http.post(
        `${BASE_URL}/wallets/${walletId}/stocks/${STOCK_NAME}`,
        JSON.stringify({ type: 'buy' }),
        { headers, tags: { type: 'buy' } }
    );
    buyDuration.add(Date.now() - buyStart);

    if (buyRes.status === 429) {
        rateLimited.add(1);
    } else {
        const buyOk = check(buyRes, {
            'buy status is 200 or 400': (r) => r.status === 200 || r.status === 400,
            'buy not a server error':   (r) => r.status < 500,
        });
        if (!buyOk || buyRes.status >= 500) {
            buyErrors.add(1);
            console.error(`BUY failed: status=${buyRes.status} body=${buyRes.body}`);
        }
    }

    sleep(0.1);

    if (buyRes.status === 200) {
        const sellStart = Date.now();
        const sellRes = http.post(
            `${BASE_URL}/wallets/${walletId}/stocks/${STOCK_NAME}`,
            JSON.stringify({ type: 'sell' }),
            { headers, tags: { type: 'sell' } }
        );
        sellDuration.add(Date.now() - sellStart);

        if (sellRes.status === 429) {
            rateLimited.add(1);
        } else {
            const sellOk = check(sellRes, {
                'sell status is 200 or 400': (r) => r.status === 200 || r.status === 400,
                'sell not a server error':   (r) => r.status < 500,
            });
            if (!sellOk || sellRes.status >= 500) {
                sellErrors.add(1);
                console.error(`SELL failed: status=${sellRes.status} body=${sellRes.body}`);
            }
        }
    }

    sleep(0.1);
}

export function teardown() {
    const res = http.get(`${BASE_URL}/stocks`);
    if (res.status !== 200) {
        console.error('Could not fetch bank state in teardown');
        return;
    }

    const bank  = JSON.parse(res.body);
    const aapl  = (bank.stocks || []).find(s => s.name === STOCK_NAME);
    const logRes  = http.get(`${BASE_URL}/log`);
    const log     = (JSON.parse(logRes.body).log || []);
    const buys    = log.filter(e => e.type === 'buy').length;
    const sells   = log.filter(e => e.type === 'sell').length;
    const expected = INITIAL_BANK_QUANTITY - buys + sells;

    console.log(`\n=== Stock Conservation Check ===`);
    console.log(`Initial bank quantity: ${INITIAL_BANK_QUANTITY}`);
    console.log(`Total buys logged:     ${buys}`);
    console.log(`Total sells logged:    ${sells}`);
    console.log(`Expected bank:         ${expected}`);
    console.log(`Actual bank:           ${aapl ? aapl.quantity : 'NOT FOUND'}`);

    if (!aapl) {
        console.error('INVARIANT VIOLATED: stock not found in bank after test!');
    } else if (aapl.quantity !== expected) {
        console.error('INVARIANT VIOLATED: Bank quantity does not match audit log!');
    } else {
        console.log('Stock conservation invariant: PASSED ✅');
    }
}