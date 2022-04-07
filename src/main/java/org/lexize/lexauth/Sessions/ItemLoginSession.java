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
import java.util.logging.Level;

public class ItemLoginSession {
    public Inventory View;
    public Material[] ItemQueueToMatch;
    public Material[] CurrentItemQueue;
    public String UUIDOfPlayer;
    public int CurrentStep;
    private Player _player;
    private static final Material[] values;
    private boolean isMatch = true;

    static {
        var s = Arrays.stream(Material.values()).filter((e) -> !e.isLegacy() & e.isItem()).toList();
        values = new Material[s.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = s.get(i);
        }

    }

    public ItemLoginSession(Player pl) {
        _player = pl;

        CurrentStep = 0;
        UUIDOfPlayer = pl.getUniqueId().toString();
        UserPassword passwd = LexAuth.Accounts.get(UUIDOfPlayer);
        ItemQueueToMatch = passwd.ItemQueue;
        CurrentItemQueue = new Material[9];
        View = Bukkit.createInventory(null, 45);
        Render();
    }
    public void ComputeClick(int itemIndex) {
        if (itemIndex >= 18) {
            if (CurrentStep < 9) {
                Material mat = View.getItem(itemIndex).getType();
                CurrentItemQueue[CurrentStep] = mat;
                isMatch = mat.equals(ItemQueueToMatch[CurrentStep]);
                CurrentStep ++;
                if ((CurrentStep < 7 && ItemQueueToMatch[CurrentStep] == null && isMatch)|(CurrentStep == 8 && isMatch)) {
                    UserPassword passwd = LexAuth.Accounts.get(UUIDOfPlayer);
                    passwd.ItemQueue = CurrentItemQueue.clone();
                    passwd.LastIP = _player.getAddress().getHostString();
                    passwd.LastJoin = Instant.now().toEpochMilli();
                    LexAuth.Accounts.put(UUIDOfPlayer, passwd);
                    LexAuth.Logged.put(UUIDOfPlayer, true);
                    LexAuth.ItemLoginSessions.remove(UUIDOfPlayer);
                    Utils.SendLoginMessage(_player);
                    _player.closeInventory();
                }
                else {
                    Render();
                }
            }
        }
        else if (itemIndex == 9) {
            CurrentItemQueue = new Material[9];
            CurrentStep = 0;
            isMatch = true;
            Render();
        }
    }
    public void Render() {
        Material[] items = new Material[27];
        int curItemInd = random_range(0,26);
        items[curItemInd] = ItemQueueToMatch[CurrentStep];
        for (int i = 0; i < 27; i++) {
            if ((i != curItemInd | !isMatch)) {
                Material mtr = values[random_range(0, values.length-1)];

                while (Arrays.stream(items).anyMatch(mtr::equals) & mtr.isItem()) mtr = values[random_range(0, values.length-1)];
                items[i] = mtr;
            }
        }
        for (int i = 0; i < 9; i++) {
            Material m = CurrentItemQueue[i];
            if (m != null) {
                View.setItem(i, new ItemStack(m));
            }
            else {
                View.setItem(i, new ItemStack(Material.AIR));
            }
        }
        ItemStack eraseItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta eraseMeta = eraseItem.getItemMeta();
        eraseMeta.displayName(MiniMessage.miniMessage().deserialize(LexAuth.Config.getString("item_login_erase_queue")));
        eraseItem.setItemMeta(eraseMeta);
        View.setItem(9, eraseItem);
        for (int i = 1; i < 9; i++) {
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
