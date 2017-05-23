package com.stenden.inf2j.alarmering.server.http;

import com.stenden.inf2j.alarmering.server.sql.SqlProvider;
import com.stenden.inf2j.alarmering.server.util.annotation.NonnullByDefault;
import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.RequestHandler;
import nl.jk5.jsonlibrary.JsonArray;
import nl.jk5.jsonlibrary.JsonObject;

import javax.inject.Inject;
import java.sql.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NonnullByDefault
public class ProfileHandler implements RequestHandler<DemoRequest, JsonObject> {

    @Inject
    private SqlProvider sqlProvider;

    @Inject
    private Executor executor;

    @Override
    public CompletableFuture<JsonObject> handle(RequestContext ctx, DemoRequest request) throws Exception {
        return this.getRowFromDatabase();

        //return CompletableFuture.completedFuture(new JsonObject()
        //        .add("success", true)
        //        .add("naam", request.naam()));
    }

    private CompletableFuture<JsonObject> getRowFromDatabase(){
        CompletableFuture<JsonObject> res = new CompletableFuture<>();
        this.executor.execute(() -> { // Onderstaande code asynchroon uitvoeren
            try(Connection conn = this.sqlProvider.getConnection()){ // Dit is een syntax trick om de verbinding automatisch terug naar de pool te geven als hij niet meer nodig is
                PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM iets WHERE naam=?");
                pstmt.setInt(1, 1);
                pstmt.setString(1, "");

                Statement stmt = conn.createStatement();

                JsonArray resArray = new JsonArray(); //De json array met het resultaat

                ResultSet rs = stmt.executeQuery("SELECT * FROM iets");
                while(rs.next()){ //Zolang er meer regels in het resultaat ding zitten
                    JsonObject row = new JsonObject();
                    row.add("name", rs.getString("name"));
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
}
