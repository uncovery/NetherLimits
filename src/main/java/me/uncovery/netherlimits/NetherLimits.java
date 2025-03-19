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
            getLogger().log(Level.WARNING, "Player {0} was found on the nether roof, locating them!", player.getDisplayName());

            if (useWarp) {
                try {
                    teleportToWarp(player, targetWarp);
                    player.sendMessage("You have been warped to " + targetWarp + "!");
                } catch (Exception e) {
                    player.sendMessage("Warp point not found!");
                    getLogger().log(Level.SEVERE, "Tried to teleport player to warp point {0} but failed. Check if warp point exists!", targetWarp);
                }
            } else {
                Location newLocation = findLocation(player.getLocation(), player);
                player.teleport(newLocation);
                getLogger().log(Level.WARNING, "Player {0}  was was relocated from location ({1}-{2}-{3}) to ({4}-{5}-{6})", 
                        new Object[]{player.getDisplayName(), playerLocation.getX(), playerLocation.getY(), playerLocation.getZ(), newLocation.getX(), newLocation.getY(), newLocation.getZ()});
                player.sendMessage("You have been warped to a nearby safe location. In case you were hurt during this process, please contact the admin.");
 
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
    
    public Location findLocation(Location currentLocation, Player player) {
        Location newLocation = currentLocation;
        Boolean hasSafeLocation = false;
        while (!hasSafeLocation) {
            for (Double Y=126.0D; Y>1; Y--) {
                // let's move the user 1 block down and check if it's a good location
                newLocation.setY(Y);
                hasSafeLocation = isSafeLocation(newLocation, player);
                // getLogger().log(Level.WARNING, "Trying Y-level {0}", Y);
                // the following should not be needed since the while loop should break
                // as soon as hasSafeLocation is true;
                if (hasSafeLocation) {
                    return newLocation;
                }
            }
            // once we iterated to the bottom, move back up, add one value to X and restart
            Double currentX = newLocation.getX();
            newLocation.setX(currentX + 1);
            Double currentY = newLocation.getY();
            newLocation.setY(currentY + 1);            
            // start from the top again
            newLocation.setY(127);
            getLogger().log(Level.WARNING, "Could not find valid Y for player {0}, shifting X from ({1}-{2}-{3}) to ({4}-{5}-{6})", 
                    new Object[]{player.getDisplayName(), currentLocation.getX(), currentLocation.getY(), currentLocation.getZ(), newLocation.getX(), newLocation.getY(), newLocation.getZ()});             
        }
        return newLocation;
    }
    
    
    /**
     * Checks if a location is safe (solid ground with 2 breathable blocks)
     *
     * @param location Location to check
     * @return True if location is safe
     */
    public boolean isSafeLocation(Location location, Player player) {
        Block feet = location.getBlock();
        if (!feet.getType().isAir()) {
            //getLogger().log(Level.INFO, "Feet not in air");
            return false; 
        }
        //getLogger().log(Level.INFO, "Valid feet location found");
        
        Block head = feet.getRelative(BlockFace.UP);
        if (!head.getType().isAir()) {
            //player.sendMessage("Head not in air");
            return false;
        }
        //getLogger().log(Level.INFO, "Valid head location found");
        
        Block ground = feet.getRelative(BlockFace.DOWN);
        if (!ground.getType().isSolid()) {
            //player.sendMessage("Ground not solid");
            return false; // not solid
        }
        //getLogger().log(Level.INFO, "Valid ground location found");
        
        return true;
    }    
}