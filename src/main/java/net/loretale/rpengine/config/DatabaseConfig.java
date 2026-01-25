package net.loretale.rpengine.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class DatabaseConfig {
    public static final BuilderCodec<DatabaseConfig> CODEC =
            BuilderCodec.builder(DatabaseConfig.class, DatabaseConfig::new)
                    .append(new KeyedCodec<>("Url", Codec.STRING),
                            (c, s, __) -> c.url = s,
                            (c, __) -> c.url).add()
                    .append(new KeyedCodec<>("Username", Codec.STRING),
                            (c, s, __) -> c.username = s,
                            (c, __) -> c.username).add()
                    .append(new KeyedCodec<>("Password", Codec.STRING),
                            (c, s, __) -> c.password = s,
                            (c, __) -> c.password).add()
                    .build();

    private String url = "jdbc:postgresql://localhost:5432/loretale";
    private String username = "dev";
    private String password = "devpassword";

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
