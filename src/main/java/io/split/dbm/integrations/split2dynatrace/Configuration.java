package io.split.dbm.integrations.split2dynatrace;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;

public class Configuration {

    public String dynatraceApiKey;
    public String dynatraceUrl;
    public String dynatraceTag;
    
    public String[] entities;

    public static Configuration fromFile(String configFilePath) throws IOException {
        String configContents = Files.readString(Paths.get(configFilePath));
        return new Gson().fromJson(configContents, Configuration.class);
    }
}
