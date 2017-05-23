package com.stenden.inf2j.alarmering.server.inject;

import com.google.inject.Inject;
import com.stenden.inf2j.alarmering.server.sql.SqlProvider;
import com.stenden.inf2j.alarmering.server.util.annotation.NonnullByDefault;
import com.typesafe.config.Config;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.inject.Singleton;
import java.sql.Connection;
import java.sql.SQLException;

@Singleton
@NonnullByDefault
class HikariSqlProvider implements SqlProvider {

    private final HikariDataSource ds;

    @Inject
    public HikariSqlProvider(Config config) {
        HikariConfig c = new HikariConfig();
        Config databaseConfig = config.getConfig("database");
        c.setJdbcUrl(databaseConfig.getString("jdbcUrl"));
        c.setUsername(databaseConfig.getString("username"));
        c.setPassword(databaseConfig.getString("password"));
        c.addDataSourceProperty("cachePrepStmts", "true");
        c.addDataSourceProperty("prepStmtCacheSize", "250");
        c.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.ds = new HikariDataSource(c);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
