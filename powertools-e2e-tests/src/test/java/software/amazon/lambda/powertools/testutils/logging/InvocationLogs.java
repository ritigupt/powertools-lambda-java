package software.amazon.lambda.powertools.testutils.logging;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.IntStream;

/**
 * Logs for a specific Lambda invocation
 */
public class InvocationLogs {
    private final String[] logs;
    private final String[] functionLogs;

    public InvocationLogs(String base64Logs, String requestId) {
        String rawLogs = new String(Base64.getDecoder().decode(base64Logs), StandardCharsets.UTF_8);
        this.logs = rawLogs.split("\n");

        String start = String.format("START RequestId: %s", requestId);
        String end = String.format("END RequestId: %s", requestId);
        int startPos = IntStream.range(0, logs.length)
                .filter(i -> logs[i].startsWith(start))
                .findFirst()
                .orElse(-1);
        int endPos = IntStream.range(0, logs.length)
                .filter(i -> logs[i].equals(end))
                .findFirst()
                .orElse(-1);
        this.functionLogs = Arrays.copyOfRange(this.logs, startPos + 1, endPos);
    }

    public String[] getAllLogs() {
        return logs;
    }

    /**
     * Return only logs from function, exclude START, END, and REPORT and other elements generated by Lambda service
     *
     * @return only logs generated by the function
     */
    public String[] getFunctionLogs() {
        return this.functionLogs;
    }

    public String[] getFunctionLogs(Level level) {
        String[] filtered = getFunctionLogs();

        return Arrays.stream(filtered).filter(log -> log.contains("\"level\":\"" + level.getLevel() + "\""))
                .toArray(String[]::new);
    }

    public enum Level {
        DEBUG("DEBUG"),
        INFO("INFO"),
        WARN("WARN"),
        ERROR("ERROR");

        private final String level;

        Level(String lvl) {
            this.level = lvl;
        }

        public String getLevel() {
            return level;
        }

        @Override
        public String toString() {
            return level;
        }
    }
}
