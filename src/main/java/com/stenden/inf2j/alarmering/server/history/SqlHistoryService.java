package com.stenden.inf2j.alarmering.server.history;

import com.google.common.collect.ImmutableList;
import com.stenden.inf2j.alarmering.api.history.HistoryElement;
import com.stenden.inf2j.alarmering.api.history.HistoryService;
import com.stenden.inf2j.alarmering.api.sql.SqlProvider;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migration;
import com.stenden.inf2j.alarmering.server.sql.migrator.Migrator;

import javax.inject.Inject;
import java.sql.*;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SqlHistoryService implements HistoryService {

    private final SqlProvider sqlProvider;
    private final Executor executor;

    @Inject
    public SqlHistoryService(SqlProvider sqlProvider, Executor executor, Migrator migrator){
        this.sqlProvider = sqlProvider;
        this.executor = executor;

        migrator.addMigration(Migration.create("create history table", "CREATE TABLE alarmering.history(id INT PRIMARY KEY NOT NULL AUTO_INCREMENT,client_id INT NOT NULL,lat FLOAT NOT NULL,`long` FLOAT NOT NULL,`time` TIMESTAMP NOT NULL,message TEXT NOT NULL);"));
    }

    @Override
    public CompletableFuture<List<HistoryElement>> getHistoryForClient(int clientId) {
        CompletableFuture<List<HistoryElement>> res = new CompletableFuture<>();
        this.executor.execute(() -> { // Onderstaande code asynchroon uitvoeren
            try(Connection conn = this.sqlProvider.getConnection()){ // Dit is een syntax trick om de verbinding automatisch terug naar de pool te geven als hij niet meer nodig is
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM history WHERE client_id=? AND message IS NOT NULL");
                pstmt.setInt(1, clientId); // Hier waarde vanuit de GET in :id

                Statement stmt = conn.createStatement();

                ImmutableList.Builder<HistoryElement> builder = ImmutableList.builder();

                ResultSet rs = pstmt.executeQuery();
                while(rs.next()){ //Zolang er meer regels in het resultaat ding zitten
                    builder.add(new HistoryElement(rs.getInt("client_id"), rs.getFloat("lat"), rs.getFloat("long"), rs.getString("melding"), rs.getTimestamp("tijd").toInstant()));
                }

                rs.close();
                stmt.close();

                //Het resultaat in de future zetten
                res.complete(builder.build());
            } catch (SQLException e) {
                //De foutmelding in de future zetten, zodat hij hogerop afgehandeld kan worden
                res.completeExceptionally(e);
            }
        });
        return res; //Returnen van de future. Op dit moment is zijn waarde nog niks, maar dat zal hij ooit in de toekomst (future) wel worden
    }
}
