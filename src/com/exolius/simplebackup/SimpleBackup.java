package com.exolius.simplebackup;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class SimpleBackup extends JavaPlugin {

    /*----------------------
     Variable declerations
     ----------------------*/
    double interval;
    boolean broadcast = true;
    public static String message = "[SimpleBackup]";
    public static String dateFormat = "yyyy-MM-DD-hh-mm-ss";
    public static String backupFile = "backups/";
    public static int intervalBetween = 100;
    List<String> backupWorlds;
    protected FileConfiguration config;


    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
        System.out.println("[SimpleBackup] Disabled SimpleBackup");
    }

    public void onEnable() {
        // When plugin is enabled, load the "config.yml"
        loadConfiguration();

        // Set the backup interval, 72000.0D is 1 hour, multiplying it by the value interval will change the backup cycle time
        double ticks = 72000.0D * this.interval;

        // Add the repeating task, set it to repeat the specfied time
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Runnable() {

            public void run() {
                // When the task is run, start the map backup
                doBackup();
            }
        }, 60L, (long) ticks);

        // After enabling, print to console to say if it was successful
        System.out.println("[SimpleBackup] Enabled. Backup interval " + this.interval + " hours");
        // Shameless self promotion in the source code :D
        System.out.println("[SimpleBackup] Developed by Exolius");
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // When command is run, do something
        // Does nothing at the moment because we don't need commands
        return false;
    }

    public void loadConfiguration() {
        // Set the config object
        config = getConfig();

        // Set default values for variables
        interval =          config.getDouble("backup-interval-hours", 1.0D);
        intervalBetween =   config.getInt("interval-between", intervalBetween);
        broadcast =         config.getBoolean("broadcast-message", true);
        backupFile =        config.getString("backup-file", "backups/");
        backupWorlds =      config.getStringList("backup-worlds");
        message =           config.getString("backup-message", message);
        dateFormat =        config.getString("backup-date-format", dateFormat);

        if (backupWorlds.size() == 0) {
            // If "backupWorlds" is empty then fill it with the worlds
            backupWorlds.add(((World) getServer().getWorlds().get(0)).getName());
        }

        // Generate the config.yml
        config.set("backup-worlds", backupWorlds);
        config.set("interval-between", Integer.valueOf(intervalBetween));
        config.set("backup-interval-hours", Double.valueOf(interval));
        config.set("broadcast-message", Boolean.valueOf(broadcast));
        config.set("backup-file", backupFile);
        config.set("backup-message", message);
        config.set("backup-date-format", dateFormat);
        saveConfig();
    }

    public void doBackup() {
        // Begin backup of worlds
        // Broadcast the backup initialization if enabled
        SimpleBackup.this.getServer().broadcastMessage(ChatColor.BLUE + "[SimpleBackup] Backup starting");
        // Loop through all the specified worlds and save then to a .zip
        for (World world : SimpleBackup.this.getServer().getWorlds()) {
            try {
                world.save();
                world.setAutoSave(false);

                BackupThread bt = SimpleBackup.this.backupWorld(world, null);
                if (bt != null) {
                    bt.start();
                    while (bt.isAlive()) ;
                    world.setAutoSave(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Broadcast the backup completion if enabled
        if (SimpleBackup.this.broadcast)
            SimpleBackup.this.getServer().broadcastMessage(ChatColor.BLUE + "[SimpleBackup] Backup complete");
    }

    public BackupThread backupWorld(World world, File file)
            throws Exception {
        if (world != null) {
            if ((this.backupWorlds.contains(world.getName()))) {
                return new BackupThread(new File(world.getName()));
            }
            return null;
        }
        if ((world == null) && (file != null)) {
            return new BackupThread(file);
        }
        return null;
    }

    public static String format() {
        //Set the naming format of the backed up file, based on the config values
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);
        return formatter.format(date);
    }

    public void printDebug(){
         // Debugging code, prints loaded variables to console
         // To see if the loading works after modification
         System.out.println(backupWorlds);
         System.out.println(intervalBetween);
         System.out.println(interval);
         System.out.println(broadcast);
         System.out.println(backupFile);
         System.out.println(message);
         System.out.println(dateFormat);
    }

}
