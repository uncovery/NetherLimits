package me.uncovery.netherlimits;

import com.earth2me.essentials.IEssentials;
import net.ess3.api.IUser;
import com.earth2me.essentials.api.IAsyncTeleport;
import java.util.concurrent.CompletableFuture;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import static org.bukkit.World.Environment.NETHER;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherLimits extends JavaPlugin implements Listener {

    static IEssentials essentials;
    
    @Override
    public void onEnable() {
        // Check if EssentialsX is available
        if (getServer().getPluginManager().getPlugin("Essentials") != null) {
            essentials = (IEssentials) getServer().getPluginManager().getPlugin("Essentials");
            getLogger().info("EssentialsX found! NetherLimits plugin enabled.");
        } else {
            getLogger().warning("EssentialsX not found! NetherLimits plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Register events
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        // Check if the player is in the Nether and above Y = 125
        if (playerLocation.getWorld().getEnvironment().equals(NETHER) && playerLocation.getY() > 125) {
            player.sendMessage(ChatColor.RED + "You were detected on the nether roof, relocating you...");

            // Calculate a new location below the player
            Location newLocation = playerLocation.clone();
            newLocation.setY(10); // Start searching from Y = 10

            String warpLocation = "spawn";
            
            try {
                teleportToWarp(player, warpLocation);
                player.sendMessage("You have been warped to " + warpLocation + "!");
            } catch (Exception e) {
                player.sendMessage("Warp point not found!");

            }
        }
    }
    public static void teleportToWarp(Player player, String warpName) {
        try {
            // Get the IAsyncTeleport instance for the player
            IUser user = essentials.getUser(player);
            IAsyncTeleport asyncTeleporter = user.getAsyncTeleport();
            CompletableFuture<Boolean> future = new CompletableFuture();
            asyncTeleporter.warp(user, warpName, null, PlayerTeleportEvent.TeleportCause.PLUGIN, future);

        } catch (Exception e) {
            player.sendMessage("An error occurred while trying to teleport you to the warp: " + warpName);
            e.printStackTrace();
        }
    }    
}