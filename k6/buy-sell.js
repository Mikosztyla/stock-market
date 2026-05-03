import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Counter } from 'k6/metrics';
import { BASE_URL, THRESHOLDS, STOCK_NAME, INITIAL_BANK_QUANTITY } from './config.js';

const buyDuration  = new Trend('buy_duration',  true);
const sellDuration = new Trend('sell_duration', true);
const buyErrors    = new Counter('buy_errors');
const sellErrors   = new Counter('sell_errors');

export const options = {
    thresholds: THRESHOLDS,
    scenarios: {
        ramp_up: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '10s', target: 10  },
                { duration: '20s', target: 10  },
                { duration: '10s', target: 50  },
                { duration: '20s', target: 50  },
                { duration: '10s', target: 0   },
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

    check(res, {
        'bank seeded successfully': (r) => r.status === 200,
    });

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

    const buyOk = check(buyRes, {
        'buy status is 200 or 400': (r) => r.status === 200 || r.status === 400,
        'buy not a server error':   (r) => r.status < 500,
    });

    if (!buyOk || buyRes.status >= 500) {
        buyErrors.add(1);
        console.error(`BUY failed: status=${buyRes.status} body=${buyRes.body}`);
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

        const sellOk = check(sellRes, {
            'sell status is 200 or 400': (r) => r.status === 200 || r.status === 400,
            'sell not a server error':   (r) => r.status < 500,
        });

        if (!sellOk || sellRes.status >= 500) {
            sellErrors.add(1);
            console.error(`SELL failed: status=${sellRes.status} body=${sellRes.body}`);
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

    const bank   = JSON.parse(res.body);
    const stocks = bank.stocks || [];
    const aapl   = stocks.find(s => s.name === STOCK_NAME);

    console.log(`\n=== Stock Conservation Check ===`);
    console.log(`Initial bank quantity: ${INITIAL_BANK_QUANTITY}`);
    console.log(`Final bank quantity:   ${aapl ? aapl.quantity : 'NOT FOUND'}`);

    if (!aapl) {
        console.error('INVARIANT VIOLATED: AAPL not found in bank after test!');
        return;
    }

    const logRes  = http.get(`${BASE_URL}/log`);
    const logData = JSON.parse(logRes.body);
    const log     = logData.log || [];
    const buys    = log.filter(e => e.type === 'buy').length;
    const sells   = log.filter(e => e.type === 'sell').length;

    console.log(`Total buys logged:  ${buys}`);
    console.log(`Total sells logged: ${sells}`);
    console.log(`Expected bank:      ${INITIAL_BANK_QUANTITY - buys + sells}`);
    console.log(`Actual bank:        ${aapl.quantity}`);

    if (aapl.quantity !== INITIAL_BANK_QUANTITY - buys + sells) {
        console.error('INVARIANT VIOLATED: Bank quantity does not match audit log!');
    } else {
        console.log('Stock conservation invariant: PASSED');
    }
}