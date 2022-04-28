package org.lexize.lexauth.Listeners;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.lexize.lexauth.DataHolders.UserPassword;
import org.lexize.lexauth.Enums.PasswordTypeEnum;
import org.lexize.lexauth.LexAuth;
import org.lexize.lexauth.Sessions.LoginSession;
import org.lexize.lexauth.Sessions.Registration;
import org.lexize.lexauth.Utils;

import java.time.Instant;

public class LexAuthListener implements Listener {
    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnJoin(PlayerJoinEvent event) {
        //Checking if logon system is initialized
            //Getting player
            Player pl = event.getPlayer();
            //Check is player registered
            if (LexAuth.Accounts.containsKey(pl.getUniqueId().toString())) {
                //Get account
                UserPassword passwd = LexAuth.Accounts.get(pl.getUniqueId().toString());
                String ip = pl.getAddress().getHostString();
                if (!passwd.LastIP.equals(ip) || passwd.LastJoin < Instant.now().toEpochMilli() - (1000*360)) {
                    String uuid = pl.getUniqueId().toString();
                    LexAuth.PlayerIventorySaves.put(uuid, pl.getInventory().getContents());
                    pl.getInventory().clear();
                    LexAuth.Logged.put(pl.getUniqueId().toString(), false);
                    LoginSession session = new LoginSession();
                    session.NeededPassword = passwd.Password;
                    session.CurrentPassword = "";
                    LexAuth.LoginSession.put(pl.getUniqueId().toString(), session);
                    switch (passwd.PasswordType) {
                        case PIN: Utils.SendPincodeMessage(pl); break;
                        case AUTH: Utils.SendAuthLoginMessage(pl);
                    }
                }
                else {
                    LexAuth.Logged.put(pl.getUniqueId().toString(), true);
                }
            }
            else {
                Utils.StartRegistration(pl);
            }

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void OnLeave(PlayerQuitEvent event) {
        Player pl = event.getPlayer();
        String uuid = pl.getUniqueId().toString();
        if (LexAuth.PlayerIventorySaves.containsKey(uuid)) {
            try {
                pl.getInventory().setContents(LexAuth.PlayerIventorySaves.get(uuid));
            }
            catch (Exception ignored) {

            }
        }
    }

    @EventHandler
    public void OnPlayerEvent(PlayerMoveEvent event) {
        Player pl = event.getPlayer();
        if (!LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) || !LexAuth.Logged.get(pl.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerEvent(PlayerDropItemEvent event) {
        Player pl = event.getPlayer();
        if (!LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) || !LexAuth.Logged.get(pl.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerEvent(PlayerAttemptPickupItemEvent event) {
        Player pl = event.getPlayer();
        if (!LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) || !LexAuth.Logged.get(pl.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerEvent(PlayerInteractEvent event) {
        Player pl = event.getPlayer();
        if (!LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) || !LexAuth.Logged.get(pl.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerEvent(PlayerItemConsumeEvent event) {
        Player pl = event.getPlayer();
        if (!LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) || !LexAuth.Logged.get(pl.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerEvent(PlayerDeathEvent event) {
        Player pl = event.getPlayer();
        if (!LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) || !LexAuth.Logged.get(pl.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerEvent(EntityDamageByEntityEvent event) {
        if (event.getEntityType().equals(EntityType.PLAYER)) {
            Player pl = (Player) (event.getEntity());
            if (!LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) || !LexAuth.Logged.get(pl.getUniqueId().toString())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void OnPlayerEvent(AsyncChatEvent event) {
        Player pl = event.getPlayer();
        if (!LexAuth.Accounts.containsKey(pl.getUniqueId().toString()) || !LexAuth.Logged.get(pl.getUniqueId().toString())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void OnPlayerEvent(PlayerCommandPreprocessEvent event) {
        Player pl = event.getPlayer();
        String uuid = pl.getUniqueId().toString();
        if (!(LexAuth.Accounts.containsKey(uuid) && LexAuth.Logged.get(uuid)) && !(event.getMessage().startsWith("/reg") || event.getMessage().startsWith("/auth"))) {
            event.setCancelled(true);
        }
    }
}
