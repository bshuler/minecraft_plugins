package helloworld;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class helloworldTest {

    private ServerMock server;
    private helloworld plugin;
    private final List<LogRecord> records = new ArrayList<>();

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        // Attach the capturing handler to the server's logger BEFORE
        // MockBukkit.load() - loading the plugin triggers onEnable() (and
        // therefore its log call) as a side effect, so the handler must
        // already be in place to observe it. This lets the test assert
        // real message content rather than just "onEnable ran without
        // throwing".
        server.getLogger().addHandler(new Handler() {
            @Override
            public void publish(LogRecord record) {
                records.add(record);
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        });
        plugin = MockBukkit.load(helloworld.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void onEnableLogsHelloWorldOnMessageAtInfoLevel() {
        assertEquals(1, records.size());
        LogRecord record = records.get(0);
        assertEquals(Level.INFO, record.getLevel());
        assertTrue(record.getMessage().contains("Hello World ON"),
                () -> "expected onEnable's log message to mention 'Hello World ON', got: " + record.getMessage());
    }

    @Test
    void onDisableLogsHelloWorldOffMessageAtInfoLevel() {
        records.clear();

        assertDoesNotThrow(plugin::onDisable);

        assertEquals(1, records.size());
        LogRecord record = records.get(0);
        assertEquals(Level.INFO, record.getLevel());
        assertTrue(record.getMessage().contains("Hello World OFF"),
                () -> "expected onDisable's log message to mention 'Hello World OFF', got: " + record.getMessage());
    }
}
