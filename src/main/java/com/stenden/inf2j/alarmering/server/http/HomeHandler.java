package com.stenden.inf2j.alarmering.server.http;

import com.stenden.inf2j.alarmering.server.sql.SqlProvider;
import com.stenden.inf2j.alarmering.server.util.annotation.NonnullByDefault;
import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.RequestHandler;
import nl.jk5.http2server.api.annotation.Path;
import nl.jk5.jsonlibrary.JsonArray;
import nl.jk5.jsonlibrary.JsonObject;

import javax.inject.Inject;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NonnullByDefault
public class HomeHandler implements RequestHandler<HomeRequest, JsonObject> {

    @Inject
    private SqlProvider sqlProvider;

    @Inject
    private Executor executor;

    private CompletableFuture<JsonObject> getRowFromDatabase(){
        CompletableFuture<JsonObject> res = new CompletableFuture<>();
        this.executor.execute(() -> { // Onderstaande code asynchroon uitvoeren
            try(Connection conn = this.sqlProvider.getConnection()){ // Dit is een syntax trick om de verbinding automatisch terug naar de pool te geven als hij niet meer nodig is
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Nieuws ORDER BY ID DESC");

                Statement stmt = conn.createStatement();

                JsonArray resArray = new JsonArray(); //De json array met het resultaat

                ResultSet rs = pstmt.executeQuery();
                while(rs.next()){ //Zolang er meer regels in het resultaat ding zitten
                    JsonObject row = new JsonObject();
                    row.add("Afbeelding", rs.getString("Afbeelding"));
                    row.add("Text", rs.getString("Text"));
                    resArray.add(row);
                }

                rs.close();
                stmt.close();

                //Het resultaat in de future zetten
                res.complete(new JsonObject().add("result", resArray));

            } catch (SQLException e) {
                //De foutmelding in de future zetten, zodat hij hogerop afgehandeld kan worden
                res.completeExceptionally(e);
            }
        });
        return res; //Returnen van de future. Op dit moment is zijn waarde nog niks, maar dat zal hij ooit in de toekomst (future) wel worden
    }

    @Override
    public CompletableFuture<JsonObject> handle(RequestContext ctx, HomeRequest request) throws Exception {
        return this.getRowFromDatabase();
    }
}
