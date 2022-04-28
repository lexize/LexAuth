package org.lexize.lexauth;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.lexize.lexauth.Commands.AdminResetPassword;
import org.lexize.lexauth.Commands.AuthenticatorCommand;
import org.lexize.lexauth.Commands.RegCommand;
import org.lexize.lexauth.Commands.ResetPassword;
import org.lexize.lexauth.DataHolders.UserPassword;
import org.lexize.lexauth.Listeners.LexAuthListener;
import org.lexize.lexauth.Sessions.Registration;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class LexAuth extends JavaPlugin {
    public static JavaPlugin Instance;
    public static HashMap<String, UserPassword> Accounts;
    public static HashMap<String, org.lexize.lexauth.Sessions.LoginSession> LoginSession = new HashMap<>();
    public static HashMap<String, Registration> Registrations = new HashMap<>();
    public static FileConfiguration Config;
    public static HashMap<String, Boolean> Logged = new HashMap<>();
    public Gson Json = new GsonBuilder().setPrettyPrinting().create();
    public static HashMap<String, ItemStack[]> PlayerIventorySaves = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Config = getConfig();
        Instance = this;

        File accountsFile = new File("accounts.lxa");
        if (accountsFile.exists()) {
            Type tp = new TypeToken<HashMap<String, UserPassword>>(){}.getType();
            Accounts = Json.fromJson(ReadFile(accountsFile), tp);
        }
        else {
            Accounts = new HashMap<>();
            WriteFile(accountsFile, Json.toJson(Accounts));
        }
        for (Map.Entry<String,UserPassword> passwd:
             Accounts.entrySet()) {
            Logged.put(passwd.getKey(), false);
        }
        getCommand("reg").setExecutor(new RegCommand());
        getCommand("reregister").setExecutor(new ResetPassword());
        getCommand("areregister").setExecutor(new AdminResetPassword());
        getCommand("auth").setExecutor(new AuthenticatorCommand());
        Bukkit.getPluginManager().registerEvents(new LexAuthListener(), this);
    }

    @Override
    public void onDisable() {
        File accountsFile = new File("accounts.lxa");
        WriteFile(accountsFile, Json.toJson(Accounts));

        for (Player pl:
             Bukkit.getOnlinePlayers()) {
            String uuid = pl.getUniqueId().toString();
            if (LexAuth.PlayerIventorySaves.containsKey(uuid)) {
                try {
                    pl.getInventory().setContents(LexAuth.PlayerIventorySaves.get(uuid));
                }
                catch (Exception ignored) {

                }
            }
            pl.kick(MiniMessage.miniMessage().deserialize(Config.getString("plugin_disable_kick_message")));
        }
    }

    public static boolean WriteFile(File file, String content) {
        try {
            FileOutputStream str = new FileOutputStream(file);
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            str.write(bytes);
            str.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String ReadFile(File file) {
        try {
            FileInputStream str = new FileInputStream(file);
            byte[] bytes = str.readAllBytes();
            String st = new String(bytes);
            str.close();
            return st;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
