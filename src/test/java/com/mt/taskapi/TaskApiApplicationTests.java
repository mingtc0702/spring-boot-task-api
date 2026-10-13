package com.mt.taskapi;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "app.demo.password=test-only-password")
class TaskApiApplicationTests {

    @Test
    void contextLoads() {
    }

}
