package com.stenden.inf2j.alarmering.server.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface SqlProvider {

    Connection getConnection() throws SQLException;
}
