package com.stenden.inf2j.alarmering.server.http;

import com.stenden.inf2j.alarmering.server.sql.SqlProvider;
import com.stenden.inf2j.alarmering.server.util.annotation.NonnullByDefault;
import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.api.RequestHandler;
import nl.jk5.jsonlibrary.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@NonnullByDefault
public class AddHistoryHandler implements RequestHandler<AddHistoryRequest, JsonObject> {

    private static final Logger logger = LoggerFactory.getLogger(AddHistoryHandler.class);

    @Inject
    private SqlProvider sqlProvider;

    @Inject
    private Executor executor;

    private CompletableFuture<JsonObject> insertIntoDatabase(int client_id, float lat_pos, float long_pos, String melding){
        CompletableFuture<JsonObject> res = new CompletableFuture<>();
        this.executor.execute(() -> { // Onderstaande code asynchroon uitvoeren
            try(Connection conn = this.sqlProvider.getConnection()){ // Dit is een syntax trick om de verbinding automatisch terug naar de pool te geven als hij niet meer nodig is
                logger.debug("Adding history: {}, {}, {}, {}", client_id, lat_pos, long_pos, melding);
                PreparedStatement pstmt = conn.prepareStatement("INSERT INTO geschiedenis (client_id, lat, long, tijd, melding) VALUES (?,?,?,NOW(), ?)");
                pstmt.setInt(1, client_id);
                pstmt.setFloat(2, lat_pos);
                pstmt.setFloat(3, long_pos);
                pstmt.setString(4, melding);

                pstmt.executeUpdate();

                res.complete(new JsonObject().add("success", true));
            } catch (SQLException e) {
                //De foutmelding in de future zetten, zodat hij hogerop afgehandeld kan worden
                res.completeExceptionally(e);
            }
        });
        return res; //Returnen van de future. Op dit moment is zijn waarde nog niks, maar dat zal hij ooit in de toekomst (future) wel worden
    }

    @Override
    public CompletableFuture<JsonObject> handle(RequestContext ctx, AddHistoryRequest request) throws Exception {
        return this.insertIntoDatabase(request.id(), request.latPos(), request.longPos(), request.melding());
    }
}
