package net.andrewcpu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class OreRegen extends JavaPlugin {
    private MiningListener miningListener;


    private int delayInSeconds = 0;
    private int delayWobble = 0;
    private int timeToStone = 0;
    private int radius = 0;
    private List<Material> regenMaterial;
    private Material temporaryMaterial;
    private Location centerLocation;
    public void onEnable(){
        this.miningListener = new MiningListener(this);
        getServer().getPluginManager().registerEvents(this.miningListener, this);
        getLogger().info("Listener has been registered.");
        saveDefaultConfig();
        loadFromConfig();
    }

    public void onDisable(){
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String header = ChatColor.WHITE + "[" + ChatColor.GOLD + "OreGen" + ChatColor.WHITE + "] ";
        List<String> validCommands = new ArrayList<>();
        validCommands.add("set delay timeInSeconds");
        validCommands.add("^ Set the delay of when the block will revert to the original ore after becoming " + temporaryMaterial.toString());
        validCommands.add("set wobble timeInSeconds");
        validCommands.add("^ Add a bit of randomness to the regeneration times");
        validCommands.add("set stoneTime timeInSeconds");
        validCommands.add("^ How long after breaking the block before it becomes " + temporaryMaterial.toString());
        validCommands.add("set temporaryMaterial spigotMaterial");
        validCommands.add("^ The block inbetween air and ore");
        validCommands.add("set radius distanceInBlocksFromCurrentLocation");
        validCommands.add("^ How far away from your current location do you want ores to regenerate");
        validCommands.add("get regenMaterials");
        validCommands.add("^ Displays the blocks that will regenerate when broken");
        validCommands.add("add material spigotMaterial");
        validCommands.add("^ Add a new block to the list of regenerating materials");
        validCommands.add("remove material spigotMaterial");
        validCommands.add("^ Remove a block from the list of regenerating materials");
        if(sender.hasPermission("oreregen.config")){
            if(command.getName().equalsIgnoreCase("oreregen")){
                if(args.length == 0 || (args.length == 1 && args[0].equalsIgnoreCase("help"))){
                    int n = 0;
                    for(String s : validCommands){
                        sender.sendMessage(header + (n % 2 == 0 ? ChatColor.YELLOW : ChatColor.GRAY) + (n % 2 == 0 ? "/oreregen " : "") + s);
                        n++;
                    }
                    return true;
                }
                else if(args.length == 2){
                    if(args[0].equalsIgnoreCase("get")){
                        if(args[1].equalsIgnoreCase("regenMaterials")){
                            for(Material material : this.regenMaterial){
                                sender.sendMessage(header + material.toString());
                            }
                            return true;
                        }
                    }
                    else{
                        sender.sendMessage(header + ChatColor.RED + "Invalid command.");
                        return true;
                    }
                }
                else if(args.length == 3){
                    if(args[0].equalsIgnoreCase("set")){
                        if(args[1].equalsIgnoreCase("delay")){
                            int delay;
                            try{
                                delay = Integer.parseInt(args[2]);
                            }catch (Exception ex){
                                sender.sendMessage(header + ChatColor.RED + "Please enter an amount of seconds.");
                                return true;
                            }
                            this.delayInSeconds = delay;
                            getConfig().set("regenerationDelay", delay);
                            saveConfig();
                            sender.sendMessage(header + ChatColor.GREEN + "Successfully updated " + args[1]);
                            return true;
                        }
                        else if(args[1].equalsIgnoreCase("wobble")){
                            int delay;
                            try{
                                delay = Integer.parseInt(args[2]);
                            }catch (Exception ex){
                                sender.sendMessage(header + ChatColor.RED + "Please enter an amount of seconds.");
                                return true;
                            }
                            this.delayWobble = delay;
                            getConfig().set("delayWobble", delay);
                            saveConfig();
                            sender.sendMessage(header + ChatColor.GREEN + "Successfully updated " + args[1]);
                            return true;
                        }
                        else if(args[1].equalsIgnoreCase("stoneTime")){
                            int delay;
                            try{
                                delay = Integer.parseInt(args[2]);
                            }catch (Exception ex){
                                sender.sendMessage(header + ChatColor.RED + "Please enter an amount of seconds.");
                                return true;
                            }
                            this.timeToStone = delay;
                            getConfig().set("timeUntilStone", delay);
                            saveConfig();
                            sender.sendMessage(header + ChatColor.GREEN + "Successfully updated " + args[1]);
                            return true;
                        }
                        else if(args[1].equalsIgnoreCase("temporaryMaterial")){
                            Material tempMat;
                            try{
                                tempMat = Material.valueOf(args[2]);
                            }
                            catch (Exception ex){
                                sender.sendMessage(header + ChatColor.RED + "Please enter a valid Spigot Material Type.");
                                return true;
                            }
                            this.temporaryMaterial = tempMat;
                            getConfig().set("temporaryMaterial", tempMat.toString());
                            saveConfig();
                            sender.sendMessage(header + ChatColor.GREEN + "Successfully updated " + args[1]);
                            return true;
                        }
                        else if (args[1].equalsIgnoreCase("radius")){
                            int radius;
                            Player player;
                            try{
                                radius = Integer.parseInt(args[2]);
                                player = (Player)sender;
                            }catch (Exception ex){
                                sender.sendMessage(header + ChatColor.RED + "Please enter a whole number.");
                                return true;
                            }
                            getConfig().set("radius", radius);
                            getConfig().set("Center.X", player.getLocation().getBlockX());
                            getConfig().set("Center.Y", player.getLocation().getBlockY());
                            getConfig().set("Center.Z", player.getLocation().getBlockZ());
                            getConfig().set("Center.World", player.getLocation().getWorld().getName());
                            centerLocation = player.getLocation();
                            saveConfig();
                            sender.sendMessage(header + ChatColor.GREEN + "Successfully set center point and radius to your current location.");
                            return true;
                        }
                        else{
                            sender.sendMessage(header + ChatColor.RED + "Invalid command.");
                            return true;
                        }


                    }
                    else if(args[1].equalsIgnoreCase("material")){
                        Material tempMat;
                        try{
                            tempMat = Material.valueOf(args[2]);
                        }
                        catch (Exception ex){
                            sender.sendMessage(header + ChatColor.RED + "Please enter a valid Spigot Material Type.");
                            return true;
                        }
                        if(args[0].equalsIgnoreCase("add")){
                            if(regenMaterial.contains(tempMat)){
                                sender.sendMessage(header + ChatColor.RED + "That material is already being regenerated.");
                                return true;
                            }
                            this.regenMaterial.add(tempMat);
                            getConfig().set("materialsToRegen", regenMaterial.stream().map(Material::toString).collect(Collectors.toList()));
                            saveConfig();
                            sender.sendMessage(header + ChatColor.GREEN + "Updated regenerating materials.");
                            return true;
                        }
                        else if(args[0].equalsIgnoreCase("remove")){
                            if(!regenMaterial.contains(tempMat)){
                                sender.sendMessage(header + ChatColor.RED + "That material is not being regenerated.");
                                return true;
                            }
                            this.regenMaterial.remove(tempMat);
                            getConfig().set("materialsToRegen", regenMaterial.stream().map(Material::toString).collect(Collectors.toList()));
                            saveConfig();
                            sender.sendMessage(header + ChatColor.GREEN + "Updated regenerating materials.");
                            return true;
                        }
                        else{
                            sender.sendMessage(header + ChatColor.RED + "Invalid command.");
                            return true;
                        }
                    }
                }
                else{
                    sender.sendMessage(header + ChatColor.RED + "Invalid command.");
                    return true;
                }
            }
        }
        return true;
    }

    public void loadFromConfig(){
        this.delayInSeconds = getConfig().getInt("regenerationDelay");
        this.timeToStone = getConfig().getInt("timeUntilStone");
        this.delayWobble = getConfig().getInt("delayWobble");
        this.regenMaterial = getConfig().getStringList("materialsToRegen").stream().map(a -> Material.valueOf(a.toUpperCase())).collect(Collectors.toList());
        this.temporaryMaterial = Material.valueOf(getConfig().getString("temporaryMaterial"));
        if(getConfig().contains("ResetBlocksIfCrash")){
            List<String> toReset = getConfig().getStringList("ResetBlocksIfCrash");
            for(String s : toReset){
                String[] args = s.split(",");
                int x = Integer.parseInt(args[0]);
                int y = Integer.parseInt(args[1]);
                int z = Integer.parseInt(args[2]);
                String worldName = args[3];
                Material material = Material.valueOf(args[4]);
                Bukkit.getWorld(worldName).getBlockAt(x,y,z).setType(material);
            }
            getConfig().set("ResetBlocksIfCrash", new ArrayList<String>());
            saveConfig();
        }
        if(getConfig().contains("Center")){
            centerLocation = new Location(Bukkit.getWorld(getConfig().getString("Center.World")), getConfig().getInt("Center.X"),  getConfig().getInt("Center.Y"),  getConfig().getInt("Center.Z"));
        }
        else{
            centerLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        }
        if(getConfig().contains("Radius")){
            radius = getConfig().getInt("Radius");
        }
        else{
            radius = 100;
        }
    }

    public int getRadius() {
        return radius;
    }

    public Location getCenterLocation() {
        return centerLocation;
    }

    public int getDelayInSeconds() {
        return delayInSeconds;
    }

    public int getDelayWobble() {
        return delayWobble;
    }

    public List<Material> getRegenMaterial() {
        return regenMaterial;
    }

    public Material getTemporaryMaterial() {
        return temporaryMaterial;
    }

    public int getTimeToStone() {
        return timeToStone;
    }
}
