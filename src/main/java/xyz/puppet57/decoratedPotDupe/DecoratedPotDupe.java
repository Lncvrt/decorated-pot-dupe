package xyz.puppet57.decoratedPotDupe;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DecoratedPotDupe extends JavaPlugin implements Listener {
    private final Map<UUID, Long> LastSneak = new HashMap<>();

    @Override
    public void onEnable() {
        // Register event listener
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("PotDupe enabled! Go break some decorated pots >:)");

        saveDefaultConfig();
        reloadConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("PotDupe disabled!");
    }

    @EventHandler
    public void onSneak(PlayerToggleSneakEvent event) {
        if (event.isSneaking()) {
            LastSneak.clear();
            LastSneak.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
        }
    }

    @EventHandler
    public void onPotBreak(BlockBreakEvent event) {
        float ItemMultiplier = Float.parseFloat(Objects.requireNonNull(getConfig().getString("item-multiplier")));

        Block block = event.getBlock();

        if (block.getType() != Material.DECORATED_POT) return;

        Player player = event.getPlayer();

        Long LastSneakTime = LastSneak.get(player.getUniqueId());
        if (LastSneakTime == null || LastSneakTime < System.currentTimeMillis() - 500) return;

        event.setDropItems(false);

        if (block.getState() instanceof org.bukkit.block.DecoratedPot pot) {
            for (ItemStack item : pot.getInventory().getContents()) {
                if (item == null || item.getType().isAir()) continue;

                int original = item.getAmount();
                int duped = (int) Math.ceil(original * ItemMultiplier);

                ItemStack dupedItem = item.clone();
                dupedItem.setAmount(duped);

                block.getWorld().dropItemNaturally(block.getLocation(), dupedItem);
            }
        }

        block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(Material.DECORATED_POT));
    }
}