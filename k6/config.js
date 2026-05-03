export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const THRESHOLDS = {
    http_req_duration: ['p(95)<500'],
    'http_req_duration{type:buy}': ['p(99)<1000'],
    'http_req_duration{type:sell}': ['p(99)<1000'],
    http_req_failed: ['rate<0.01'],
};

export const STOCK_NAME = 'AAPL';
export const INITIAL_BANK_QUANTITY = 10000;