package org.lexize.lexauth;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.lexize.lexauth.Sessions.ItemLoginSession;
import org.lexize.lexauth.Sessions.ItemRegistrationSession;
import org.lexize.lexauth.Sessions.Registration;

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
        ClickEvent passwd = ClickEvent.runCommand("/reg tppasswd");
        ClickEvent pincode = ClickEvent.runCommand("/reg tppincode");
        ClickEvent item = ClickEvent.runCommand("/reg tpitem");
        Component message =
                MiniMessage.miniMessage()
                        .deserialize(LexAuth.Config.getString("registration_select_pass_type_message"))
                        .append(Component.newline())
                        .append(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("registration_select_password")).clickEvent(passwd))
                        .append(Component.newline())
                        .append(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("registration_select_pincode")).clickEvent(pincode))
                        .append(Component.newline())
                        .append(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("registration_select_item_queue")).clickEvent(item))

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

    public static void StartItemRegistrationSession(Player pl) {
        var session = new ItemRegistrationSession(pl);
        LexAuth.ItemRegistrationSessions.put(session.UUIDOfPlayer, session);
        pl.openInventory(session.View);
    }
    public static void StartItemLoginSession(Player pl) {
        var session = new ItemLoginSession(pl);
        LexAuth.ItemLoginSessions.put(session.UUIDOfPlayer, session);
        pl.openInventory(session.View);
    }
}
