package com.notificationservice;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SimpleSmokeTest {

    @Test
    void basicMathTest() {
        assertEquals(4, 2 + 2, "Basic math should work");
    }

    @Test
    void stringTest() {
        String message = "Hello, Test!";
        assertNotNull(message);
        assertTrue(message.contains("Test"));
    }

    @Test
    void objectCreationTest() {
        Object obj = new Object();
        assertNotNull(obj);
    }
}
