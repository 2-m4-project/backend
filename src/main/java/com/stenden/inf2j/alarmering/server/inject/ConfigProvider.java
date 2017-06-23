package com.stenden.inf2j.alarmering.server.inject;

import com.stenden.inf2j.alarmering.api.util.annotation.NonnullByDefault;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

@Singleton
@NonnullByDefault
public class ConfigProvider implements Provider<Config> {

    private static final Logger logger = LoggerFactory.getLogger(ConfigProvider.class);

    @Override
    public Config get() {
        File configFile = new File("settings.conf");
        if(configFile.exists()){
            return ConfigFactory.parseFile(configFile);
        }else{
            try {
                PrintWriter writer = new PrintWriter("settings.conf", "UTF-8");
                writer.println("# Database configuration. You can configure an JDBC url here, along with username and password");
                writer.println("# MySQL is officially supported, other databases may work, but may need some additional work");
                writer.println("database {");
                writer.println("    # The JDBC url for the database");
                writer.println("    jdbcUrl: \"jdbc:mysql://10.2.33.181/alarmering\"");
                writer.println("    # The database username");
                writer.println("    username: \"alarmering\"");
                writer.println("    # The database password");
                writer.println("    password: \"2GDmj3h05N4bDSdisTgYBWBTQ5PBG4kHFKbAdnPwAPJmbJB0ZocyumQl436WVgdh\"");
                writer.println("}");
                writer.println("");
                writer.println("# Classes that should be preloaded, for SQL migrations.");
                writer.println("# In a normal environment you don't need to touch this");
                writer.println("# If you add custom modules for authentication, you need to add them here");
                writer.println("preload: [");
                writer.println("    \"com.stenden.inf2j.alarmering.server.auth.directory.sql.Preloader\"");
                writer.println("    \"com.stenden.inf2j.alarmering.server.auth.session.sql.SqlSessionStoreMigrator\"");
                writer.println("    \"com.stenden.inf2j.alarmering.server.auth.FirstBootSetupPreloader\"");
                writer.println("    \"com.stenden.inf2j.alarmering.server.history.SqlHistoryService\"");
                writer.println("]");
                writer.println("");
                writer.println("# Configuration for the http server");
                writer.println("http-server {");
                writer.println("    # In this list you can define interfaces where the webserver should listen.");
                writer.println("    # You can add as many interfaces as you want. Both IPv4 and IPv6 are supported");
                writer.println("    # For ipv6, you need to use the url format, with square brackets, for example");
                writer.println("    #   [2001:DB8::1]:8000");
                writer.println("    interfaces: {");
                writer.println("        \"0.0.0.0:8000\" {");
                writer.println("            # SSL config. SSL/TLS is disabled on this interface");
                writer.println("            ssl {");
                writer.println("                enabled = false");
                writer.println("            }");
                writer.println("        }");
                writer.println("        ");
                writer.println("        \"0.0.0.0:8001\" {");
                writer.println("            ssl {");
                writer.println("                # Enable SSL/TLS on this interface. When this is enabled, this server will support HTTP/2");
                writer.println("                enabled = true");
                writer.println("                # The certificate public key chain. Supports PEM format");
                writer.println("                cert: \"localhost:8001.crt\"");
                writer.println("                # The certificate private key. Supports PEM format");
                writer.println("                cert-key: \"localhost:8001.key\"");
                writer.println("                ");
                writer.println("                # The TLS ciphers to use for the TLS server");
                writer.println("                ciphers: [");
                writer.println("                    \"TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384\"");
                writer.println("                    \"TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256\"");
                writer.println("                    \"TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384\"");
                writer.println("                    \"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256\"");
                writer.println("                    \"TLS_DHE_RSA_WITH_AES_128_GCM_SHA256\"");
                writer.println("                    \"TLS_DHE_DSS_WITH_AES_128_GCM_SHA256\"");
                writer.println("                ]");
                writer.println("            }");
                writer.println("        }");
                writer.println("    }");
                writer.println("}");
                writer.println("");
                writer.println("# The service to use to store local user data. Currently only 1 implementation is present, but you can add more");
                writer.println("users {");
                writer.println("    service {");
                writer.println("        \"com.stenden.inf2j.alarmering.server.auth.user.sql.SqlUserService\" {");
                writer.println("            ");
                writer.println("        }");
                writer.println("    }");
                writer.println("}");
                writer.flush();
                writer.close();
            }catch(IOException e){
                logger.error("Error while writing default config file", e);
            }
        }

        return ConfigFactory.parseFile(configFile);
    }
}
