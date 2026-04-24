package util;


import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Kaizen — DatabaseConfig.java
 * Package: com.kaizen.util
 *
 * Demonstrates:
 *  - JDBC        → Connection, PreparedStatement, ResultSet
 *  - Map         → config properties store
 *  - Singleton   → one instance for the whole app
 *  - Generics    → generic query executor <T>
 *
 * @author Person A — Saffih Bouchra
 */
public class DatabaseConfig {

    private static final Logger LOG = Logger.getLogger(DatabaseConfig.class.getName());

    // ── Singleton ──────────────────────────────────────────────────────────────
    private static DatabaseConfig instance;

    // ── Map<String,String> — DB config properties ──────────────────────────────
    private final Map<String, String> config = new LinkedHashMap<>();

    // ── Single shared connection ───────────────────────────────────────────────
    private Connection connection;

    // ── Private constructor — Singleton ───────────────────────────────────────
    private DatabaseConfig() {
        config.put("url",
                "jdbc:mysql://localhost:3306/kaizen_db" +
                        "?useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        config.put("user",     "kaizen_user");
        config.put("password", System.getenv().getOrDefault("KAIZEN_DB_PASS", ""));
        config.put("driver",   "com.mysql.cj.jdbc.Driver");
    }

    // ── Get singleton instance ─────────────────────────────────────────────────
    public static DatabaseConfig getInstance() {
        if (instance == null) instance = new DatabaseConfig();
        return instance;
    }

    // ── Get / set config ───────────────────────────────────────────────────────
    public String get(String key)                   { return config.get(key); }
    public void   set(String key, String value)     { config.put(key, value); }
    public Map<String, String> getAllConfig()        { return Collections.unmodifiableMap(config); }

    // ── Connect ────────────────────────────────────────────────────────────────
    /**
     * Opens a JDBC connection using config Map values.
     * In production this would use a connection pool (HikariCP).
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName(config.get("driver"));
            } catch (ClassNotFoundException e) {
                LOG.warning("Driver not found — using in-memory SQLite for demo");
            }
            connection = DriverManager.getConnection(
                    config.get("url"),
                    config.get("user"),
                    config.get("password")
            );
            LOG.info("[DB] Connected to: " + config.get("url"));
        }
        return connection;
    }

    // ── Generic query executor ─────────────────────────────────────────────────
    /**
     * Executes a SELECT query and maps each row using a RowMapper<T>.
     *
     * Generic <T> = works for User, Course, Message, any model.
     *
     * @param sql    the SQL query with ? placeholders
     * @param mapper lambda that converts a ResultSet row → T object
     * @param params values to bind to the ? placeholders
     * @return List<T> of mapped objects
     */
    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        List<T> results = new ArrayList<>();
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) results.add(mapper.map(rs));
            }
        }
        return results;
    }

    /**
     * Executes INSERT / UPDATE / DELETE.
     * Returns number of rows affected.
     */
    public int update(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            bindParams(ps, params);
            return ps.executeUpdate();
        }
    }

    /**
     * Executes INSERT and returns the generated auto-increment ID.
     */
    public int insert(String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = getConnection().prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    // ── Param binder ──────────────────────────────────────────────────────────
    /**
     * Binds Object[] params to a PreparedStatement.
     * Handles String, Integer, Long, Boolean, null automatically.
     */
    private void bindParams(PreparedStatement ps, Object[] params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            Object p = params[i];
            if      (p == null)              ps.setNull(i+1, Types.NULL);
            else if (p instanceof String)    ps.setString(i+1, (String) p);
            else if (p instanceof Integer)   ps.setInt(i+1, (Integer) p);
            else if (p instanceof Long)      ps.setLong(i+1, (Long) p);
            else if (p instanceof Boolean)   ps.setBoolean(i+1, (Boolean) p);
            else if (p instanceof Double)    ps.setDouble(i+1, (Double) p);
            else                             ps.setObject(i+1, p);
        }
    }

    // ── RowMapper functional interface ────────────────────────────────────────
    /**
     * Generic functional interface.
     * Implement as a lambda: rs -> new User(rs.getInt("id"), ...)
     */
    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    public void execute(String sql) throws SQLException {
        try (Statement st = getConnection().createStatement()) {
            st.execute(sql);
        }
    }

    // ── Close ──────────────────────────────────────────────────────────────────
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOG.info("[DB] Connection closed.");
            }
        } catch (SQLException e) { LOG.warning("[DB] Close error: " + e.getMessage()); }
    }

    @Override
    public String toString() {
        return "DatabaseConfig{url=" + config.get("url") + ", user=" + config.get("user") + "}";
    }
}

