package com.notificationservice.unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SimpleUnitTest {

    @Test
    void basicTest() {
        assertTrue(true, "This should always pass");
    }

    @Test
    void mathTest() {
        assertEquals(4, 2 + 2, "Basic math should work");
    }

    @Test
    void stringTest() {
        String message = "Hello, Test!";
        assertNotNull(message);
        assertTrue(message.contains("Test"));
    }
}
