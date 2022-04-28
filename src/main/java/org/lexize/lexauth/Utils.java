package org.lexize.lexauth;

import com.google.common.io.BaseEncoding;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.lexize.lexauth.DataHolders.UserPassword;
import org.lexize.lexauth.Sessions.Registration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.Base64;
import java.util.Random;

public class Utils {

    public static void SendPincodeMessage(Player player) {
        Component message =  MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("type_pincode_message"));

        for (int i = 0; i < 9; i++) {
            ClickEvent ce = ClickEvent.runCommand(String.format("/reg %s", i+1));
            if (i%3 == 0) message = message.append(Component.newline());
            message = message.append(Component.text(String.format("[%s]", i+1)).color(TextColor.color(0,100,0)).clickEvent(ce));
        }
        message = message.append(Component.newline());
        ClickEvent erase = ClickEvent.runCommand("/reg erase");
        message = message.append(Component.text("[❌]").color(TextColor.color(200,0,0)).clickEvent(erase));
        ClickEvent zero = ClickEvent.runCommand("/reg 0");
        message = message.append(Component.text("[0]").color(TextColor.color(0,100,0)).clickEvent(zero));
        player.sendMessage(message);
    }
    public static void SendRegPincodeMessage(Player player) {
        Component message =  MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("type_pincode_message"));

        for (int i = 0; i < 9; i++) {
            ClickEvent ce = ClickEvent.runCommand(String.format("/reg %s", i+1));
            if (i%3 == 0) message = message.append(Component.newline());
            message = message.append(Component.text(String.format("[%s]", i+1)).color(TextColor.color(0,100,0)).clickEvent(ce));
        }
        message = message.append(Component.newline());
        ClickEvent erase = ClickEvent.runCommand("/reg erase");
        message = message.append(Component.text("[❌]").color(TextColor.color(200,0,0)).clickEvent(erase));
        ClickEvent zero = ClickEvent.runCommand("/reg 0");
        message = message.append(Component.text("[0]").color(TextColor.color(0,100,0)).clickEvent(zero));
        ClickEvent confirm = ClickEvent.runCommand("/reg confirm");
        message = message.append(Component.text("[✔]").color(TextColor.color(0,255,0)).clickEvent(confirm));
        player.sendMessage(message);
    }
    public static void SendPasswordMessage(Player player) {
        Component message =  MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("type_password_message"));

        player.sendMessage(message);
    }
    public static void SendRegMessage(Player player) {
        ClickEvent passwd = ClickEvent.runCommand("/reg tpauth");
        ClickEvent pincode = ClickEvent.runCommand("/reg tppincode");
        Component message =
                MiniMessage.miniMessage()
                        .deserialize(LexAuth.Config.getString("registration_select_pass_type_message"))
                        .append(Component.newline())
                        .append(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("registration_select_authenticator")).clickEvent(passwd))
                        .append(Component.newline())
                        .append(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("registration_select_pincode")).clickEvent(pincode))
                ;
        player.sendMessage(message);
    }
    public static void SendCompleteRegMessage(Player player) {

        player.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("login_complete")));
    }
    public static void SendLoginMessage(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("login_message")));
    }
    public static void SendWrongPasswordMessage(Player player) {
        player.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("wrong_password_message")));
    }
    public static void StartRegistration(Player pl) {
        Registration reg = new Registration();
        reg.Password = "";
        LexAuth.Registrations.put(pl.getUniqueId().toString(), reg);
        Utils.SendRegMessage(pl);
    }


    private static byte[] hmacSha(byte[] keyBytes, byte[] longBytes) {
        try {
            Mac hmac;
            hmac = Mac.getInstance("HmacSHA1");
            SecretKeySpec macKey = new SecretKeySpec(keyBytes, "RAW");
            hmac.init(macKey);
            return hmac.doFinal(longBytes);
        } catch (GeneralSecurityException gse) {
            throw new UndeclaredThrowableException(gse);
        }
    }

    public static int GenerateTotp(byte[] secretKey, long counter) {
        byte[] longBytes = ByteBuffer.allocate(8).putLong(counter/30).array();
        byte[] stringBytes = secretKey;
        byte[] sha = hmacSha(stringBytes, longBytes);
        int offset = sha[sha.length - 1] & 0xf;
        long truncated = (sha[offset] & 0x7f) << 24 | (sha[offset+1] & 0xff) << 16 | (sha[offset+2] & 0xff) << 8 | (sha[offset+3] & 0xff);
        int finalOTP = (int) Math.round((truncated % (Math.pow(10,6))));
        return finalOTP;
    }

    public static void SendRegAuthMessage(Player pl) {
        Registration reg = LexAuth.Registrations.get(pl.getUniqueId().toString());
        byte[] code = new byte[10];
        Random rnd = new Random();
        rnd.nextBytes(code);
        reg.Step = 1;
        reg.Password = BaseEncoding.base32().encode(code);
        pl.sendMessage(MiniMessage.miniMessage().deserialize(String.format(LexAuth.Config.getString("auth_type_code"), reg.Password.replace("=",""))));
        LexAuth.Registrations.put(pl.getUniqueId().toString(), reg);
    }

    public static void OnLoginComplete(Player pl) {
        String uuid = pl.getUniqueId().toString();
        pl.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("login_message")));
        LexAuth.Logged.put(uuid, true);
        UserPassword passwd = LexAuth.Accounts.get(uuid);
        passwd.LastIP = pl.getAddress().getHostString();
        passwd.LastJoin = Instant.now().toEpochMilli();
        LexAuth.Accounts.put(uuid, passwd);
        if (LexAuth.PlayerIventorySaves.containsKey(uuid)) {
            try {
                pl.getInventory().setContents(LexAuth.PlayerIventorySaves.get(uuid));
                LexAuth.PlayerIventorySaves.remove(uuid);
            }
            catch (Exception e) {
                pl.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("failed_to_restore_items")));
            }
        }
    }

    public static void SendAuthLoginMessage(Player pl) {
        pl.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("type_auth_message")));
    }
}
