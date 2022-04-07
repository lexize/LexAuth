package org.lexize.lexauth.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.lexize.lexauth.Sessions.ItemRegistrationSession;

public class TestWindowCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        Player pl = (Player) commandSender;
        var session = new ItemRegistrationSession(pl);
        pl.openInventory(session.View);
        return true;
    }
}
