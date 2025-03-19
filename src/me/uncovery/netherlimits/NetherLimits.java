package me.uncovery.netherlimits;

import com.earth2me.essentials.IEssentials;
import net.ess3.api.IUser;
import com.earth2me.essentials.api.IAsyncTeleport;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import static org.bukkit.World.Environment.NETHER;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherLimits extends JavaPlugin implements Listener {

    static IEssentials essentials;
    static FileConfiguration config;
    static Boolean useWarp;
    static String targetWarp;
    
    @Override
    public void onEnable() {
        // config file management
        
        this.saveDefaultConfig();
        config = this.getConfig(); 
        targetWarp = config.getString("targetWarp");

        // Check if EssentialsX is available
        if (getServer().getPluginManager().getPlugin("Essentials") != null && !"false".equals(targetWarp)) {
            essentials = (IEssentials) getServer().getPluginManager().getPlugin("Essentials");
            getLogger().log(Level.INFO, "EssentialsX found! Using warp point {0}", targetWarp);
            useWarp = true;
        } else if (getServer().getPluginManager().getPlugin("Essentials") != null) {
            getLogger().info("EssentialsX found but targetWarp config is set to false! Using iterative teleport.");
            useWarp = false;
        } else {
            getLogger().warning("EssentialsX not found! Using iterative teleport.");
            useWarp = false;
        }
        getLogger().info("NetherLimits plugin enabled.");
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();

        // Check if the player is in the Nether and above Y = 125
        if (playerLocation.getWorld().getEnvironment().equals(NETHER) && playerLocation.getY() > 125) {
            player.sendMessage(ChatColor.RED + "You were detected on the nether roof, relocating you...");

            if (useWarp) {
                try {
                    teleportToWarp(player, targetWarp);
                    player.sendMessage("You have been warped to " + targetWarp + "!");
                } catch (Exception e) {
                    player.sendMessage("Warp point not found!");
                    getLogger().log(Level.SEVERE, "Tried to teleport player to warp point {0} but failed. Check if warp point exists!", targetWarp);
                }
            } else {
                player.teleport(findLocation(player.getLocation(), player));
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
        }
    }    
    
    public static Location findLocation(Location currentLocation, Player player) {
        Location newLocation = currentLocation;
        while (!isSafeLocation(newLocation) || newLocation.getY() > 127.0D) {
            for (Double Y=126.0D; Y>1; Y++) {
                // let's move the user 1 block down and check if it's a good location
                player.sendMessage(ChatColor.RED + "Lowering you to block height " + Y.toString());
                newLocation.setY(Y);
            }
            // once we iterated to the bottom, move back up, add one value to X and restart
            player.sendMessage(ChatColor.RED + "Moving your X by one block");
            Double currentZ = newLocation.getZ();
            newLocation.setZ(currentZ++);
        }
        return newLocation;
    }
    
    
    /**
     * Checks if a location is safe (solid ground with 2 breathable blocks)
     *
     * @param location Location to check
     * @return True if location is safe
     */
    public static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        if (feet.getType().isAir() && feet.getLocation().add(0, 1, 0).getBlock().getType().isAir()) {
            return false; // not transparent (will suffocate)
        }
        Block head = feet.getRelative(BlockFace.UP);
        if (head.getType().isAir()) {
            return false; // not transparent (will suffocate)
        }
        Block ground = feet.getRelative(BlockFace.DOWN);
        if (!ground.getType().isSolid()) {
            return false; // not solid
        }
        return true;
    }    
}