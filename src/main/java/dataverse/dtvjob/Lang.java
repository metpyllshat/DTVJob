package dataverse.dtvjob;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.bukkit.configuration.file.YamlConfiguration;

public class Lang {
    public static String lang = "ru";

    private static final LinkedHashMap<String, String> langMap = new LinkedHashMap<>();

    private final DTVJob plugin;

    public Lang(DTVJob plugin) {
        this.plugin = plugin;
    }

    public static String get(String key) {
        return langMap.containsKey(key) ? langMap.get(key) : "NULL";
    }

    public static String getKey(String val) {
        for (Map.Entry<String, String> entry : langMap.entrySet()) {
            if (((String)entry.getValue()).equals(val))
                return entry.getKey();
        }
        return "NULL";
    }

    public static String getCommandKey(String val) {
        for (Map.Entry<String, String> entry : langMap.entrySet()) {
            if (((String)entry.getValue()).equalsIgnoreCase(val) && ((String)entry.getKey()).toUpperCase().startsWith("COMMAND_"))
                return entry.getKey();
        }
        return "NULL";
    }

    public static void clearPhrases() {
        langMap.clear();
    }

    public static int getPhrases() {
        return langMap.size();
    }

    public static String getModified(String key, String[] tokens) {
        String orig = langMap.get(key);
        for (int i = 0; i < tokens.length; i++)
            orig = orig.replaceAll("%" + (i + 1), tokens[i]);
        return orig;
    }

    public void initPhrases() {
        langMap.put("startTruck", "§eДавай неси быстрее, уебок");
                langMap.put("getBox", "§eТы взял ещё один груз");
                        langMap.put("boxLimit", "§cЧо дохуя сильный? Больше 3-х нельзя нести");
                                langMap.put("noBoxes", "§cУ тебя нихуя нет, ебанутый");
                                        langMap.put("anotherWay", "§cТы куда бля принёс, ебанутый?");
                                                langMap.put("death", "§cЕбать ты лох, лол)");
                                                        langMap.put("soFar", "§cТы куда съебала, мразь?");
                                                                langMap.put("clickEnventory", "§cПока у тебя груз, хуй те, а не инвентарь");
                                                                        langMap.put("anotherBox", "§cТы коробки с другого пути несёшь ебанутый");
                                                                                langMap.put("deliverBox", "§eНа, лови свои нищие §a%money% ");
                                                                                        File file = new File(this.plugin.getDataFolder(), "/lang/" + lang + ".yml");
        YamlConfiguration langFile = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> e : (Iterable<Map.Entry<String, Object>>)langFile.getValues(true).entrySet())
            langMap.put(e.getKey(), (String)e.getValue());
    }

    public void saveNewLang() {
        YamlConfiguration yamlConfiguration = new YamlConfiguration();
        File dir = new File(this.plugin.getDataFolder(), "/lang");
        File file = new File(this.plugin.getDataFolder(), "/lang/ru.yml");
        for (Map.Entry<String, String> e : langMap.entrySet())
            yamlConfiguration.set(e.getKey(), e.getValue());
        try {
            if (!dir.exists() || !dir.isDirectory())
                dir.mkdir();
            PrintWriter out = new PrintWriter(file);
            for (String key : yamlConfiguration.getKeys(false))
                out.println(String.valueOf(key) + ": \"" + yamlConfiguration.getString(key) + "\"");
            out.close();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    public void loadLang() {
        File langFile = new File(this.plugin.getDataFolder(), "/lang/" + lang + ".yml");
        boolean newLangs = false;
        if (langFile.exists()) {
            LinkedHashMap<String, String> tempMap = new LinkedHashMap<>();
            LinkedHashMap<String, String> toPut = new LinkedHashMap<>();
            LinkedHashMap<String, String> newMap = new LinkedHashMap<>();
            YamlConfiguration yamlConfiguration = new YamlConfiguration();
            try {
                yamlConfiguration.load(langFile);
                for (String key : yamlConfiguration.getKeys(false))
                    tempMap.put(key, yamlConfiguration.getString(key));
                for (Map.Entry<String, String> entry : langMap.entrySet()) {
                    if (tempMap.containsKey(entry.getKey())) {
                        toPut.put(entry.getKey(), tempMap.get(entry.getKey()));
                        continue;
                    }
                    newLangs = true;
                    newMap.put(entry.getKey(), entry.getValue());
                }
                langMap.putAll(toPut);
                if (newLangs) {
                    File file = new File(this.plugin.getDataFolder(), "/lang/ru_new.yml");
                    if (file.exists())
                        file.delete();
                    YamlConfiguration yamlConfiguration1 = new YamlConfiguration();
                    try {
                        for (Map.Entry<String, String> entry : newMap.entrySet())
                            yamlConfiguration1.set(entry.getKey(), entry.getValue());
                        yamlConfiguration1.save(file);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
