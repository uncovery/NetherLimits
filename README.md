# NetherLimits
Minecraft plugin to prevent users from getting onto the Nether roof

Once people in a nether dimension are caught on top of the Nether roof, action is taken.

There are two options:
1) Use EssentialsX to warp them to a dedicated warp point. Requires EssentialsX to be
installed. Create the warp point in EssentialsX, then input the warp point name in 
the configuration file of this plugin under "targetWarp"

2) Let this plugin handle it. To do so, set the "targetWarp" in this plugin to false.
If you do not have EssentialsX installed, there is no need to change the setting.
This plugin will then automatically try to find a solid ground below and around the
user and then place them there.