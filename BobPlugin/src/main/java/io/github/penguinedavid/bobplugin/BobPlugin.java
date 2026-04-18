/*
 * BobPlugin - A simple plugin to apply an offset bobbing animation to an ItemDisplay
 * using the new transformation system added in Minecraft 1.19.4.
 * with a sine wave and client side lerps with a configurable speed in a decimal number of bobs per tick.
 * plus a config option for the height and amplitude of the bobbing.
*/

package io.github.penguinedavid.bobplugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class BobPlugin extends JavaPlugin {
    private BukkitTask bobTask;
    private double bobsPerTick;
    private double amplitude;
    private static final int INTERP_TICKS = 5;

    @Override
    public void onEnable() {
        String version = Bukkit.getServer().getBukkitVersion().split("-")[0]; // remove any suffix like "-SNAPSHOT"
        
        if (!isVersionAtLeast(version, "1.19.4")) {
            getLogger().severe("BobPlugin requires 1.19.4+. Disabling.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();
        reloadConfig();
        FileConfiguration config = getConfig();

        bobsPerTick = config.getDouble("bobs-per-tick", 0.1);
        amplitude = config.getDouble("amplitude", 0.5);

        getLogger().info("Bobs per tick: " + bobsPerTick + " (" + (bobsPerTick * 20) + " bobs/s)");
        getLogger().info("Amplitude: " + amplitude + " blocks");

        bobTask = new BukkitRunnable() {
            private int tick = 0;

            @Override
            public void run() {
                if (tick++ % INTERP_TICKS != 0)
                    return;

                for (var world : Bukkit.getWorlds()) {
                    for (ItemDisplay display : world.getEntitiesByClass(ItemDisplay.class)) {
                        if (!display.isValid() || !display.getScoreboardTags().contains("bob"))
                            continue;

                        Transformation t = display.getTransformation();
                        // stop treating the bobsPerTick as radians
                        // as a full bob is 2*PI radians, we can calculate 
                        // the phase of the bob as a fraction of a full 
                        // bob and use that to get the sine wave offset
                        double phaseRadians = tick * bobsPerTick * 2 * Math.PI;
                        double bobOffset = Math.sin(phaseRadians) * amplitude;


                        // Apply the bobbing offset to the Y translation of the display's transformation
                        Vector3f translation = t.getTranslation();
                        Vector3f newTranslation = new Vector3f(
                                translation.x(),
                                translation.y() + (float) bobOffset,
                                translation.z());

                        display.setInterpolationDelay(0);
                        display.setInterpolationDuration(INTERP_TICKS);
                        display.setTransformation(new Transformation(
                                newTranslation,
                                t.getLeftRotation(),
                                t.getScale(),
                                t.getRightRotation()));
                    }
                }
            }
        }.runTaskTimer(this, 0L, 1L);

        getLogger().info("BobPlugin enabled.");
    }

    @Override
    public void onDisable() {
        if (bobTask != null) {
            bobTask.cancel();
        }
        getLogger().info("BobPlugin disabled.");
    }

    private boolean isVersionAtLeast(String current, String required) {
        try {
            // Split version strings into parts and compare each part as an integer
            String[] c = current.split("\\.");
            String[] r = required.split("\\.");
            // Compare each part of the version numbers
            for (int i = 0; i < Math.max(c.length, r.length); i++) {
                int cv = i < c.length ? Integer.parseInt(c[i]) : 0;
                int rv = i < r.length ? Integer.parseInt(r[i]) : 0;
                if (cv < rv)
                    return false;
                if (cv > rv)
                    return true;
            }
            // Versions are equal
            return true;
        } catch (NumberFormatException e) {
            // If version strings are not in the expected format, assume it's not compatible
            return false;
        }
    }
}