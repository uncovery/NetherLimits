/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.uncovery.netherlimits;

import static com.earth2me.essentials.utils.LocationUtil.getSafeDestination;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import static org.bukkit.World.Environment.NETHER;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class NetherLimits extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, (Plugin)this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location playerLocation = player.getLocation();
        if (player.getLocation().getWorld().getEnvironment().equals(NETHER) && playerLocation.getY() > 125) {
            player.sendMessage(ChatColor.RED + "You were detected on the nether roof, relocating you...");
            try {
                // using https://github.com/EssentialsX/Essentials/blob/2.x/Essentials/src/com/earth2me/essentials/utils/LocationUtil.java#L200

                // we start with a high Y
                int Y = 128; // needs to be set so that the WHILE kicks in
                int X = 0; // does not matter
                int Z = 0; // does not matter

                int distance = 0; //we measure how far we relocated people
                boolean shift = false; // let's tell the user in case they were relocated.

                // in case the new location is still on the roof (nothing better was found), we re-do it.
                while (Y > 125) {

                    if (distance > 0) { // we shift location only in the second round of attempts to find a location
                        playerLocation.add(10, 0, 10);
                        shift = true;
                    }

                    playerLocation.setY(10); // we try at height 0, maybe we are lucky, the getSafeDestination is looking upwards from here

                    // try to find a location 10 blocks away
                    playerLocation = getSafeDestination(playerLocation);

                    // let's get the result from the shift
                    X = (int) playerLocation.getX();
                    Y = (int) playerLocation.getY(); // this should be a good number, if not it should loop
                    Z = (int) playerLocation.getZ();

                    distance = distance + 10;
                }

                // actually relocate
                player.teleport(playerLocation);
                // send info to
                String playername = player.getPlayerListName();

                // send reports to player and to console
                if (shift) {
                    Logger.getLogger(NetherLimits.this.getName()).log(Level.WARNING, "Found player {4} on nether roof, relocating south-east to X:{0} Y:{1} Z:{2}, Distance {3}", new Object[]{X, Y, Z, distance, playername});
                    player.sendMessage(ChatColor.RED + "Done! You where relocated within " + distance + " blocks south-east to find a safe space.");
                } else {
                    Logger.getLogger(NetherLimits.this.getName()).log(Level.WARNING, "Found player {3} on nether roof, relocating below to X:{0} Y:{1} Z:{2}", new Object[]{X, Y, Z, playername});
                    player.sendMessage(ChatColor.RED + "Done! You relocated to a lower level below your location.");
                }
            } catch (Exception ex) {
                Logger.getLogger(NetherLimits.this.getName()).log(Level.SEVERE, "Error getting safe destination!", ex);
            }
        }
    }
}



