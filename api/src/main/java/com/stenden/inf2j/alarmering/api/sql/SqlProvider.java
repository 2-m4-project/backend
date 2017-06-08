package com.stenden.inf2j.alarmering.api.sql;

import com.stenden.inf2j.alarmering.api.util.annotation.NonnullByDefault;

import java.sql.Connection;
import java.sql.SQLException;

@NonnullByDefault
public interface SqlProvider {

    Connection getConnection() throws SQLException;
}
