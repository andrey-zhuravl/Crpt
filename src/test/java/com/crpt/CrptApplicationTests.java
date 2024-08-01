package com.crpt;

import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;


class CrptApplicationTests {


    @Test
    public void testSomeMethod() {
        CrptApi api = new CrptApi(TimeUnit.SECONDS, 5);
        CrptApi.Document document = CrptApi.CrptConverter.convert(api);
        try {
            api.createDocument(document);
            assert(true);
        } catch (Exception e) {
            e.printStackTrace();
            assert(false);
        }
    }

    @Test
    public void testRateLimiting() throws InterruptedException {
        CrptApi.RateLimiter rateLimiter = new CrptApi.RateLimiter(TimeUnit.SECONDS, 5);
        rateLimiter.acquire();
        assert(true);
    }

}
