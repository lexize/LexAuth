package org.lexize.lexauth.Commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lexize.lexauth.LexAuth;

import java.util.UUID;

public class AdminResetPassword implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        boolean allow = false;
        if (commandSender instanceof ConsoleCommandSender console) {
            allow = true;
        }
        else {
            Player pl = (Player) commandSender;
            if (pl.hasPermission("lexauth.areregister")) {
                if (LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) && LexAuth.Logged.get(pl.getUniqueId().toString())) {
                    allow = true;
                }
            }

        }

        String uuid = strings[0];
        Player p = Bukkit.getPlayer(uuid);
        if (p != null) {
            uuid = p.getUniqueId().toString();
        }

        if (allow) {
            LexAuth.Logged.remove(uuid);
            LexAuth.Accounts.remove(uuid);
            if (p != null) p.kick(Component.text(0));
        }

        return true;
    }
}
