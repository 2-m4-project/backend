package com.stenden.inf2j.alarmering.server.auth.directory.sql;

import com.stenden.inf2j.alarmering.api.auth.UserService;
import org.junit.Test;
import org.mockito.Mockito;

public class PreloaderTest {

    @Test
    public void testRegisterType(){
        UserService userService = Mockito.mock(UserService.class);
        UserDirectoryTypeSql directoryType = Mockito.mock(UserDirectoryTypeSql.class);

        new Preloader(userService, directoryType);

        Mockito.verify(userService).registerType(directoryType);
    }
}
