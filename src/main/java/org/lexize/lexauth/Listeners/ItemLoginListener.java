package org.lexize.lexauth.Listeners;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.lexize.lexauth.LexAuth;
import org.lexize.lexauth.Sessions.ItemLoginSession;
import org.lexize.lexauth.Sessions.ItemRegistrationSession;

public class ItemLoginListener implements Listener {
    @EventHandler
    public void OnClick(InventoryClickEvent event) {
        String uuid = event.getWhoClicked().getUniqueId().toString();
        if (LexAuth.ItemLoginSessions.containsKey(uuid)) {
            event.setCancelled(true);
            ItemLoginSession session = LexAuth.ItemLoginSessions.get(uuid);
            session.ComputeClick(event.getSlot());
        }
    }
    @EventHandler
    public void OnClose(InventoryCloseEvent event) {
        String uuid = event.getPlayer().getUniqueId().toString();
        Player pl = (Player) (event.getPlayer());
        if (LexAuth.ItemLoginSessions.containsKey(uuid)) pl.kick(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("item_pass_close_kick_message")));
    }
}
