package org.lexize.lexauth.Commands;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lexize.lexauth.LexAuth;

public class ResetPassword implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player pl = (Player) commandSender;
        String uuid = pl.getUniqueId().toString();
        if (LexAuth.Accounts.containsKey(uuid) && LexAuth.Logged.get(uuid)) {
            LexAuth.Accounts.remove(uuid);
            LexAuth.Logged.remove(uuid);
            pl.kick(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("reregister_kick_message")));
        }
        return true;
    }
}
