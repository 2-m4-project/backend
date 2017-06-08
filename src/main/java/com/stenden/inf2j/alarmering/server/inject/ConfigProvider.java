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
                writer.println("database {");
                writer.println("    jdbcUrl: \"\"");
                writer.println("    username: \"\"");
                writer.println("    password: \"\"");
                writer.println("}");
                writer.println();
                writer.println("http-server {");
                writer.println("    interfaces: {");
                writer.println("        \"0.0.0.0:8000\" {");
                writer.println("            ssl {");
                writer.println("                enabled = false");
                writer.println("            }");
                writer.println("        }");
                writer.println();
                writer.println("        \"0.0.0.0:8001\" {");
                writer.println("            ssl {");
                writer.println("                enabled = true");
                writer.println("                cert: \"localhost:8001.crt\"");
                writer.println("                cert-key: \"localhost:8001.key\"");
                writer.println();
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
                writer.flush();
                writer.close();
            }catch(IOException e){
                logger.error("Error while writing default config file", e);
            }
        }

        return ConfigFactory.parseFile(configFile);
    }
}
