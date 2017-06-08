package com.stenden.inf2j.alarmering.server.auth.session.sql;

import com.stenden.inf2j.alarmering.api.auth.User;
import com.stenden.inf2j.alarmering.api.auth.UserService;
import com.stenden.inf2j.alarmering.api.session.Session;
import com.stenden.inf2j.alarmering.api.session.SessionStorage;
import com.stenden.inf2j.alarmering.api.sql.SqlProvider;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import nl.jk5.http2server.api.RequestContext;
import nl.jk5.http2server.exception.RequestException;

import javax.inject.Inject;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.sql.*;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadLocalRandom;

public class SqlSessionStore implements SessionStorage {

    @Inject
    private CryptUtils cryptUtils;

    @Inject
    private SqlProvider sqlProvider;

    @Inject
    private Executor executor;

    @Inject
    private UserService userService;

    @Override
    public CompletableFuture<Session> getSession(RequestContext ctx) {
        Optional<String> cookie = ctx.request().cookie("alarmering-session")
                .flatMap(SerializationUtils::bytesFromUrlSafeBase64)
                .flatMap(i -> cryptUtils.decrypt(i, "tjCt16rEszOVodrpCvZSL3cmdQ2FS66FErHGtm03JxVAvt2eE8GMjLWNXkI5kPJy7T0QvuaB4eWWSu9ycqGL8tPCeqLog9dxyQ2O")) //TODO: make this encryption key configurable
                .map(GzipUtils::mayUncompress)
                .map(data -> new String(data, CharsetUtil.UTF_8));

        return cookie
                .map(s -> this.updateSession(ctx, s))
                .orElseGet(() -> this.createNewSession(ctx));
    }

    @Override
    public CompletableFuture<Session> authenticateSession(Session session, User user) {
        CompletableFuture<Session> promise = new CompletableFuture<>();
        this.executor.execute(() -> {
            try(Connection conn = this.sqlProvider.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET authenticated=TRUE, user_id=? WHERE session_key=?");
                stmt.setInt(1, user.id());
                stmt.setString(2, session.getSessionKey());

                stmt.executeUpdate();

                Session newSession = new SqlSession(session.getSessionKey(), true, session.getLastSeen(), user, session.getCsrfToken(), session.getIp());
                promise.complete(newSession);

            } catch (SQLException e) {
                promise.completeExceptionally(e);
            }
        });

        return promise;
    }

    @Override
    public CompletableFuture<Session> deauthenticateSession(Session session) {
        CompletableFuture<Session> promise = new CompletableFuture<>();
        this.executor.execute(() -> {
            try(Connection conn = this.sqlProvider.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET authenticated=FALSE, user_id=NULL WHERE session_key=?");
                stmt.setString(1, session.getSessionKey());

                stmt.executeUpdate();

                Session newSession = new SqlSession(session.getSessionKey(), false, session.getLastSeen(), null, session.getCsrfToken(), session.getIp());
                promise.complete(newSession);

            } catch (SQLException e) {
                promise.completeExceptionally(e);
            }
        });

        return promise;
    }

    private CompletableFuture<Session> createNewSession(RequestContext ctx){
        String sessionKey = randomString(64);
        CompletableFuture<Session> promise = new CompletableFuture<>();
        
        this.executor.execute(() -> {
            Savepoint savepoint = null;
            Connection conn = null;
            try {
                conn = this.sqlProvider.getConnection();
                conn.setAutoCommit(false);
                savepoint = conn.setSavepoint();

                String remoteIp = getRemoteIp(ctx);

                PreparedStatement stmt = conn.prepareStatement("INSERT INTO sessions(session_key, csrf_token, ip) VALUES (?, '', ?) RETURNING session_key, authenticated, last_seen");
                stmt.setString(1, sessionKey);
                stmt.setString(2, remoteIp);

                ResultSet rs = stmt.executeQuery();
                if(rs.next()){
                    Session session = new SqlSession(rs.getString("session_key"), rs.getBoolean("authenticated"), rs.getTimestamp("last_seen").toInstant(), null, "", remoteIp);

                    String cookieValue = this.encryptSessionCookie(session.getSessionKey());
                    Cookie cookie = new DefaultCookie("alarmering-session", cookieValue);
                    cookie.setSecure(true);
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(60 * 60 * 24 * 365 * 5);
                    cookie.setPath("/");

                    String csrfToken = createNewCsrfCookie(conn, session);
                    Cookie csrfCookie = new DefaultCookie("csrf-token", csrfToken);
                    csrfCookie.setSecure(true);
                    csrfCookie.setPath("/");

                    ctx.responseHeaders().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(csrfCookie) + "; SameSite=strict");
                    ctx.responseHeaders().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie) + "; SameSite=strict");
                    conn.commit();

                    promise.complete(session);
                }else{
                    try{
                        conn.rollback(savepoint);
                    }catch(SQLException ignored){}
                    promise.completeExceptionally(new RuntimeException("Error inserting the session into the database. The database returned 0 rows"));
                }

            } catch (SQLException | GeneralSecurityException | RequestException e) {
                if(conn != null){
                    try{
                        conn.rollback(savepoint);
                    }catch(SQLException ignored){}
                }
                promise.completeExceptionally(e);
            } finally {
                if(conn != null){
                    try{
                        conn.close();
                    }catch(SQLException ignored){}
                }
            }
        });
        return promise;
    }

    private CompletableFuture<Session> updateSession(RequestContext ctx, String sessionKey){
        CompletableFuture<Session> promise = new CompletableFuture<>();
        this.executor.execute(() -> {
            try(Connection conn = this.sqlProvider.getConnection()) {
                String remoteIp = getRemoteIp(ctx);

                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM sessions WHERE session_key=?");
                stmt.setString(1, sessionKey);
                
                ResultSet rs = stmt.executeQuery();
                if(!rs.next()){
                    this.createNewSession(ctx).handle((ses, e) -> {
                        if(e != null){
                            promise.completeExceptionally(e);
                        }else{
                            promise.complete(ses);
                        }
                        return null;
                    });
                    return;
                }

                String realSessionKey = rs.getString("session_key");
                boolean authenticated = rs.getBoolean("authenticated");
                Instant lastSeen = rs.getTimestamp("last_seen").toInstant();
                String csrfToken = rs.getString("csrf_token");
                String userUuid = rs.getString("user_id");
                int userId;
                if(userUuid == null){
                    userId = 0;
                }else{
                    try{
                        userId = Integer.parseInt(userUuid);
                    }catch(NumberFormatException e){
                        promise.completeExceptionally(new IllegalStateException("Session has invalid user"));
                        return;
                    }
                }

                CompletableFuture<Session> sessionFuture;
                if(rs.getInt("user_id") == 0){
                    //No user
                    sessionFuture = CompletableFuture.completedFuture(new SqlSession(realSessionKey, authenticated, lastSeen,null, csrfToken, remoteIp));
                }else{
                    sessionFuture = this.userService.getUser(userId).thenCompose(user -> {
                        CompletableFuture<Session> ret = new CompletableFuture<>();
                        if(!user.isPresent()){
                            ret.completeExceptionally(new IllegalStateException("User with id " + userId + " has a session, but does not exist. Resetting it"));
                            //TODO: reset session in database. Remove user ref
                        }else{
                            ret.complete(new SqlSession(realSessionKey, authenticated, lastSeen, user.get(), csrfToken, remoteIp));
                        }
                        return ret;
                    });
                }

                sessionFuture.thenCompose(session -> {
                    CompletableFuture<Session> promise2 = new CompletableFuture<>();
                    this.executor.execute(() -> {
                        try(Connection conn2 = this.sqlProvider.getConnection()) {
                            PreparedStatement stmt2 = conn2.prepareStatement("UPDATE sessions SET last_seen=NOW(), ip=? WHERE session_key=?");
                            stmt2.setString(1, remoteIp);
                            stmt2.setString(2, sessionKey);
                            stmt2.executeUpdate();

                            //promise2.complete(session);

                            CompletableFuture<Void> csrfPromise;
                            if(ctx.request().cookie("csrf-token").isPresent()){
                                csrfPromise = CompletableFuture.completedFuture(null);
                            }else{
                                csrfPromise = this.createNewCsrfCookie(ctx, sessionKey);
                            }

                            csrfPromise.thenCompose((v) -> this.checkCsrf(ctx, session)).handle((res, ex) -> {
                                if(ex != null){
                                    promise2.completeExceptionally(ex);
                                }else{
                                    promise2.complete(session);
                                }
                                return null;
                            });

                        } catch (SQLException e) {
                            promise.completeExceptionally(e);
                        }
                    });
                    return promise2;
                }).handle((session, e) -> {
                    if(e != null){
                        promise.completeExceptionally(e);
                    }else{
                        promise.complete(session);
                    }
                    return null;
                });

            } catch (SQLException e) {
                promise.completeExceptionally(e);
            }
        });
        
        return promise;
    }

    private CompletableFuture<Void> createNewCsrfCookie(RequestContext ctx, String sessionKey){
        CompletableFuture<Void> promise = new CompletableFuture<>();
        this.executor.execute(() -> {
            String csrfToken = randomString(32);
            try(Connection conn = this.sqlProvider.getConnection()){
                PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET csrf_token=? WHERE session_key=?");
                stmt.setString(1, csrfToken);
                stmt.setString(2, sessionKey);
                stmt.executeUpdate();

                Cookie csrfCookie = new DefaultCookie("csrf-token", csrfToken);
                csrfCookie.setSecure(true);
                csrfCookie.setPath("/");
                ctx.responseHeaders().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(csrfCookie) + "; SameSite=strict");

                promise.complete(null);
            } catch (SQLException e) {
                promise.completeExceptionally(e);
            }
        });
        return promise;
    }

    private String createNewCsrfCookie(Connection conn, Session session) throws SQLException, RequestException {
        String token = randomString(32);
        PreparedStatement stmt = conn.prepareStatement("UPDATE sessions SET csrf_token=? WHERE session_key=?");
        stmt.setString(1, token);
        stmt.setString(2, session.getSessionKey());

        if(stmt.executeUpdate() == 1){
            return token;
        }else{
            throw new RequestException(HttpResponseStatus.INTERNAL_SERVER_ERROR, "Error while creating csrf token");
        }
    }

    private CompletableFuture<Void> checkCsrf(RequestContext ctx, Session session){
        if(ctx.request().method().equals(HttpMethod.POST.name()) || ctx.request().method().equals(HttpMethod.PUT.name()) || ctx.request().method().equals(HttpMethod.PATCH.name()) || ctx.request().method().equals(HttpMethod.DELETE.name())){
            CharSequence header = ctx.request().headers().get("x-csrf-token");
            if(header == null || !Objects.equals(header.toString(), session.getCsrfToken())){
                CompletableFuture<Void> res = new CompletableFuture<>();
                res.completeExceptionally(new RequestException(HttpResponseStatus.UNAUTHORIZED, "Invalid CSRF Token"));
                return res;
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    private String encryptSessionCookie(String value) throws GeneralSecurityException {
        String key = "tjCt16rEszOVodrpCvZSL3cmdQ2FS66FErHGtm03JxVAvt2eE8GMjLWNXkI5kPJy7T0QvuaB4eWWSu9ycqGL8tPCeqLog9dxyQ2O"; //TODO: make this key configurable in the settings
        byte[] bytes = value.getBytes(CharsetUtil.UTF_8);
        boolean shouldCompress = bytes.length > 4096;

        byte[] maybeCompressed = bytes;
        if(shouldCompress){
            maybeCompressed = GzipUtils.compress(bytes);
        }

        byte[] encrypted = this.cryptUtils.encrypt(maybeCompressed, key);
        String base64 = SerializationUtils.bytesToUrlSafeBase64(encrypted);

        if(base64.length() <= 4096){
            return base64;
        }

        if(shouldCompress){
            return base64;
        }

        byte[] compressed = GzipUtils.compress(bytes);
        encrypted = this.cryptUtils.encrypt(compressed, key);
        return SerializationUtils.bytesToUrlSafeBase64(encrypted);
    }
    
    private static String randomString(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVW0123456789";
        StringBuilder sessionKey = new StringBuilder();
        for (int i = 0; i < length; i++) { //TODO: Use secure key generation
            sessionKey.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sessionKey.toString();
    }

    private static String getRemoteIp(RequestContext ctx){
        if(ctx.remoteAddress() instanceof InetSocketAddress){
            return ((InetSocketAddress) ctx.remoteAddress()).getHostString();
        }
        return "";
    }
}
