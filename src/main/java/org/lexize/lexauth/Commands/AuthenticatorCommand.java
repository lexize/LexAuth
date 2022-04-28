package org.lexize.lexauth.Commands;

import com.google.common.io.BaseEncoding;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lexize.lexauth.DataHolders.UserPassword;
import org.lexize.lexauth.Enums.PasswordTypeEnum;
import org.lexize.lexauth.LexAuth;
import org.lexize.lexauth.Sessions.Registration;
import org.lexize.lexauth.Utils;

import java.time.Instant;
import java.util.Base64;

public class AuthenticatorCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player pl = (Player) commandSender;
        String uuid = pl.getUniqueId().toString();
        if (LexAuth.Accounts.containsKey(uuid)) {
            UserPassword passwd = LexAuth.Accounts.get(uuid);
            if (passwd.PasswordType.equals(PasswordTypeEnum.AUTH)) {
                boolean codeIsRigth = Integer.parseInt(strings[0]) == Utils.GenerateTotp(BaseEncoding.base32().decode(passwd.Password), Instant.now().getEpochSecond());
                if (codeIsRigth) {
                    Utils.OnLoginComplete(pl);
                }
                else {
                    pl.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("auth_code_is_wrong")));
                }
            }
        }
        else {
            Registration reg = LexAuth.Registrations.get(uuid);

            boolean codeIsRigth = Integer.parseInt(strings[0]) == Utils.GenerateTotp(BaseEncoding.base32().decode(reg.Password), Instant.now().getEpochSecond());
            if (codeIsRigth) {
                pl.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("login_complete")));
                UserPassword passwd = new UserPassword();
                passwd.PasswordType = PasswordTypeEnum.AUTH;
                passwd.Password = reg.Password;
                passwd.LastIP = pl.getAddress().getHostString();
                passwd.LastJoin = Instant.now().toEpochMilli();
                LexAuth.Accounts.put(uuid, passwd);
                LexAuth.Registrations.remove(uuid);
                LexAuth.Logged.put(uuid, true);
            }
            else {
                pl.sendMessage(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("auth_code_is_wrong")));
            }
        }

        return true;
    }
}
