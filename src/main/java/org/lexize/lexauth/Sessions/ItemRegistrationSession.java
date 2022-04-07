package org.lexize.lexauth.Sessions;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.lexize.lexauth.DataHolders.UserPassword;
import org.lexize.lexauth.Enums.PasswordTypeEnum;
import org.lexize.lexauth.LexAuth;
import org.lexize.lexauth.Utils;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

public class ItemRegistrationSession {
    public Inventory View;
    public Material[] ItemQueueToMatch;
    public Material[] CurrentItemQueue;
    public String UUIDOfPlayer;
    public int CurrentStep;
    private Player _player;
    private static final Material[] values;

    static {
        var s = Arrays.stream(Material.values()).filter((e) -> !e.isLegacy() & e.isItem()).toList();
        values = new Material[s.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = s.get(i);
        }

    }

    public ItemRegistrationSession(Player pl) {
        _player = pl;

        CurrentStep = 0;
        UUIDOfPlayer = pl.getUniqueId().toString();
        ItemQueueToMatch = new Material[9];
        CurrentItemQueue = new Material[9];
        View = Bukkit.createInventory(null, 45);
        Render();
    }
    public void ComputeClick(int itemIndex) {
        if (itemIndex >= 18) {
            if (CurrentStep < 8) {
                CurrentItemQueue[CurrentStep] = View.getItem(itemIndex).getType();
                CurrentStep ++;
                Render();
            }
        }
        else if (itemIndex == 9) {
            CurrentItemQueue = new Material[9];
            CurrentStep = 0;
            Render();
        }
        else if (itemIndex == 17) {
            if (CurrentStep >= 2) {
                UserPassword passwd = new UserPassword();
                passwd.PasswordType = PasswordTypeEnum.ITEM;
                passwd.Password = null;
                passwd.ItemQueue = CurrentItemQueue.clone();
                passwd.LastIP = _player.getAddress().getHostString();
                passwd.LastJoin = Instant.now().toEpochMilli();
                LexAuth.Registrations.remove(UUIDOfPlayer);
                LexAuth.ItemRegistrationSessions.remove(UUIDOfPlayer);
                LexAuth.Accounts.put(UUIDOfPlayer, passwd);
                LexAuth.Logged.put(UUIDOfPlayer, true);
                Utils.SendCompleteRegMessage(_player);
                _player.closeInventory();
            }
        }
    }
    public void Render() {
        Material[] items = new Material[27];
        for (int i = 0; i < 27; i++) {
            Material mtr = values[random_range(0, values.length-1)];

            while (Arrays.stream(items).anyMatch(mtr::equals) & mtr.isItem()) mtr = values[random_range(0, values.length-1)];
            items[i] = mtr;
        }
        for (int i = 0; i < 9; i++) {
            Material m = CurrentItemQueue[i];
            if (m != null) {
                View.setItem(i, new ItemStack(m));
            }
        }
        ItemStack eraseItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta eraseMeta = eraseItem.getItemMeta();
        eraseMeta.displayName(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("item_login_erase_queue")));
        eraseItem.setItemMeta(eraseMeta);
        ItemStack confirmItem = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta confirmMeta = confirmItem.getItemMeta();
        confirmMeta.displayName(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("item_login_confirm_queue")));
        confirmItem.setItemMeta(confirmMeta);
        View.setItem(9, eraseItem);
        View.setItem(9+8, confirmItem);
        for (int i = 1; i < 8; i++) {
            View.setItem(i+9, new ItemStack(Material.BLACK_STAINED_GLASS_PANE));
        }
        for (int i = 0; i < 27; i++) {
            View.setItem(i+18, new ItemStack(items[i]));
        }
    }
    private int random_range(int from, int to) {
        return (int)(Math.round(from + Math.random() * (to-from)));
    }
}
