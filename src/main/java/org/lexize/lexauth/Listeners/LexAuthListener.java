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
    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        if (LexAuth.Accounts == null || LexAuth.Registrations == null || LexAuth.Logged == null)
        event.getPlayer().kick(Component.text("Login system not initialized"));
    }
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
                    LexAuth.Logged.put(pl.getUniqueId().toString(), false);
                    LoginSession session = new LoginSession();
                    session.NeededPassword = passwd.Password;
                    session.CurrentPassword = "";
                    LexAuth.LoginSession.put(pl.getUniqueId().toString(), session);
                    switch (passwd.PasswordType) {
                        case PIN: Utils.SendPincodeMessage(pl); break;
                        case TEXT: Utils.SendPasswordMessage(pl); break;
                        case ITEM: Utils.StartItemLoginSession(pl); break;
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
            String uuid = pl.getUniqueId().toString();
            String message = PlainTextComponentSerializer.plainText().serialize(event.originalMessage());
            if (!LexAuth.Accounts.containsKey(uuid)) {
                Registration reg = LexAuth.Registrations.get(uuid);
                if (reg.PasswordType.equals(PasswordTypeEnum.TEXT)) {
                    if (message.length() >= 4) {
                        reg.Password = message;
                        UserPassword passwd = new UserPassword();
                        passwd.PasswordType = PasswordTypeEnum.TEXT;
                        passwd.Password = reg.Password;
                        passwd.LastIP = pl.getAddress().getHostString();
                        passwd.LastJoin = Instant.now().toEpochMilli();
                        LexAuth.Registrations.remove(uuid);
                        LexAuth.Accounts.put(uuid, passwd);
                        LexAuth.Logged.put(uuid, true);
                        Utils.SendCompleteRegMessage(pl);
                    }
                    else {
                        pl.sendMessage(MiniMessage.miniMessage().deserialize("pass_length_too_small"));
                    }
                }

            }
            else {
                LoginSession logSess = LexAuth.LoginSession.get(uuid);
                if (logSess.NeededPassword.equals(message)) {
                    UserPassword passwd = LexAuth.Accounts.get(uuid);
                    passwd.LastIP = pl.getAddress().getHostString();
                    passwd.LastJoin = Instant.now().toEpochMilli();
                    LexAuth.Accounts.put(uuid, passwd);
                    LexAuth.Logged.put(uuid, true);
                    Utils.SendLoginMessage(pl);
                }
                else {
                    Utils.SendWrongPasswordMessage(pl);
                }
            }
        }
    }

}
