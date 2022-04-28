package org.lexize.lexauth.Commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Instrument;
import org.bukkit.Note;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lexize.lexauth.*;
import org.lexize.lexauth.DataHolders.UserPassword;
import org.lexize.lexauth.Enums.PasswordTypeEnum;
import org.lexize.lexauth.Sessions.LoginSession;
import org.lexize.lexauth.Sessions.Registration;

import java.time.Instant;

public class RegCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player pl = (Player) commandSender;
        String uuid = pl.getUniqueId().toString();
        pl.playNote(pl.getLocation(), Instrument.BIT, Note.sharp(2, Note.Tone.F));
        if (strings.length != 1) {
            pl.sendMessage(Component.text("This command is system only and cannot be used by players. Use clickable buttons instead").color(TextColor.color(168, 24, 0)));
        }
        else {
            if (LexAuth.Accounts.containsKey(uuid)) {
                LoginSession sess = LexAuth.LoginSession.get(uuid);
                if (strings[0].equals("erase")) {
                    sess.CurrentPassword = "";
                }
                else {
                    sess.CurrentPassword += strings[0];
                    if (sess.NeededPassword.equals(sess.CurrentPassword)) {
                        Utils.OnLoginComplete(pl);
                    }
                    else {
                        LexAuth.LoginSession.put(uuid, sess);
                    }
                }
            }
            else {
                Registration reg = LexAuth.Registrations.get(uuid);
                if (reg.Step == 0) {
                    switch (strings[0]) {
                        case "tppincode":
                            reg.PasswordType = PasswordTypeEnum.PIN;
                            Utils.SendRegPincodeMessage(pl);
                            break;
                        case "tpauth":
                            reg.PasswordType = PasswordTypeEnum.AUTH;
                            Utils.SendRegAuthMessage(pl);
                            break;
                    }
                    reg.Step = 1;
                    LexAuth.Registrations.put(uuid, reg);
                }
                else if (reg.Step == 1 && reg.PasswordType.equals(PasswordTypeEnum.PIN)) {
                    if (strings[0].equals("erase")) {
                        reg.Password = "";
                        LexAuth.Registrations.put(uuid, reg);
                    }
                    else if (strings[0].equals("confirm")) {
                        if (reg.Password.length() >= 4) {
                            UserPassword passwd = new UserPassword();
                            passwd.PasswordType = PasswordTypeEnum.PIN;
                            passwd.Password = reg.Password;
                            passwd.LastIP = pl.getAddress().getHostString();
                            passwd.LastJoin = Instant.now().toEpochMilli();
                            LexAuth.Registrations.remove(uuid);
                            LexAuth.Accounts.put(uuid, passwd);
                            LexAuth.Logged.put(uuid, true);
                            Utils.SendCompleteRegMessage(pl);
                        }
                        else {
                            pl.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("pin_length_too_small")));
                        }
                    }
                    else {
                        reg.Password += strings[0];
                        LexAuth.Registrations.put(uuid, reg);
                    }
                }

            }
        }
        return true;
    }
}
