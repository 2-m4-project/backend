package com.stenden.inf2j.alarmering.server.auth.directory.sql;

import com.lambdaworks.crypto.SCryptUtil;
import com.stenden.inf2j.alarmering.api.auth.AuthenticationResult;
import com.stenden.inf2j.alarmering.api.auth.DirectoryUser;
import com.stenden.inf2j.alarmering.api.sql.SqlProvider;
import nl.jk5.jsonlibrary.JsonObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executor;

public class UserDirectorySqlTest {

    @Test
    public void testAuthenticate() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        SqlProvider sqlProvider = Mockito.mock(SqlProvider.class);
        PreparedStatement stmt = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);

        Executor executor = Mockito.mock(Executor.class);

        String passwordHash = SCryptUtil.scrypt("password-string", 16384, 8, 1);

        Mockito.when(sqlProvider.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(stmt);
        Mockito.when(stmt.executeQuery()).thenReturn(rs);
        Mockito.when(rs.getString("username")).thenReturn("usernamestring");
        Mockito.when(rs.getString("password_hash")).thenReturn(passwordHash);
        Mockito.when(rs.next()).thenReturn(true);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(stmt).setString(Mockito.eq(1), usernameCaptor.capture());
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.doNothing().when(stmt).setInt(Mockito.eq(2), idCaptor.capture());

        UserDirectorySql directory = new UserDirectorySql("test-sql", sqlProvider, executor, new JsonObject().set("id", 15));
        AuthenticationResult<DirectoryUser> result = directory.authenticateSync("usernamestring", "password-string");
        Assert.assertNotNull(result);

        Assert.assertEquals("usernamestring", usernameCaptor.getValue());
        Assert.assertEquals(Integer.valueOf(15), idCaptor.getValue());
        Assert.assertTrue(result instanceof AuthenticationResult.Success);

        AuthenticationResult.Success<DirectoryUser> successResult = (AuthenticationResult.Success<DirectoryUser>) result;
        Assert.assertNotNull(successResult.getUser());
        DirectoryUser user = successResult.getUser();
        
        Assert.assertEquals("usernamestring", user.username());
    }

    @Test
    public void testIncorrectCredentials() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        SqlProvider sqlProvider = Mockito.mock(SqlProvider.class);
        PreparedStatement stmt = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);

        Executor executor = Mockito.mock(Executor.class);

        String passwordHash = SCryptUtil.scrypt("passwordstring", 16384, 8, 1);

        Mockito.when(sqlProvider.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(stmt);
        Mockito.when(stmt.executeQuery()).thenReturn(rs);
        Mockito.when(rs.getString("username")).thenReturn("usernamestring");
        Mockito.when(rs.getString("password_hash")).thenReturn(passwordHash);
        Mockito.when(rs.next()).thenReturn(true);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(stmt).setString(Mockito.eq(1), usernameCaptor.capture());
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.doNothing().when(stmt).setInt(Mockito.eq(2), idCaptor.capture());

        UserDirectorySql directory = new UserDirectorySql("test-sql", sqlProvider, executor, new JsonObject().set("id", 15));
        AuthenticationResult<DirectoryUser> result = directory.authenticateSync("usernamestring", "wrong-password");
        Assert.assertNotNull(result);

        Assert.assertEquals("usernamestring", usernameCaptor.getValue());
        Assert.assertEquals(Integer.valueOf(15), idCaptor.getValue());
        Assert.assertTrue(((AuthenticationResult) result) instanceof AuthenticationResult.IncorrectCredentials);
    }

    @Test
    public void testUserDoesNotExist() throws SQLException {
        Connection connection = Mockito.mock(Connection.class);
        SqlProvider sqlProvider = Mockito.mock(SqlProvider.class);
        PreparedStatement stmt = Mockito.mock(PreparedStatement.class);
        ResultSet rs = Mockito.mock(ResultSet.class);

        Executor executor = Mockito.mock(Executor.class);

        Mockito.when(sqlProvider.getConnection()).thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.anyString())).thenReturn(stmt);
        Mockito.when(stmt.executeQuery()).thenReturn(rs);
        Mockito.when(rs.next()).thenReturn(false);

        ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.doNothing().when(stmt).setString(Mockito.eq(1), usernameCaptor.capture());
        ArgumentCaptor<Integer> idCaptor = ArgumentCaptor.forClass(Integer.class);
        Mockito.doNothing().when(stmt).setInt(Mockito.eq(2), idCaptor.capture());

        UserDirectorySql directory = new UserDirectorySql("test-sql", sqlProvider, executor, new JsonObject().set("id", 15));
        AuthenticationResult<DirectoryUser> result = directory.authenticateSync("usernamestring", "password-string");
        Assert.assertNotNull(result);

        Assert.assertEquals("usernamestring", usernameCaptor.getValue());
        Assert.assertEquals(Integer.valueOf(15), idCaptor.getValue());
        Assert.assertTrue(((AuthenticationResult) result) instanceof AuthenticationResult.UserDoesNotExist);
    }
}
