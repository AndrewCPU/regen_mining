package net.andrewcpu;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MiningListener implements Listener {
    private OreRegen main;

    public MiningListener(OreRegen main) {
        this.main = main;
    }

    public void saveNeedingRegen(Location location, Material material){
        List<String> needToRegen = new ArrayList<>();
        if(main.getConfig().contains("ResetBlocksIfCrash")){
            needToRegen = main.getConfig().getStringList("ResetBlocksIfCrash");
        }
        needToRegen.add(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getWorld().getName() + "," + material.toString());
        main.getConfig().set("ResetBlocksIfCrash", needToRegen);
        main.saveConfig();
    }

    public void removeFromNeedRegen(Location location){
        List<String> needToRegen = new ArrayList<>();
        if(main.getConfig().contains("ResetBlocksIfCrash")){
            needToRegen = main.getConfig().getStringList("ResetBlocksIfCrash");
        }
        Optional<String> toRemove = needToRegen.stream().filter(s -> s.contains(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getWorld().getName())).findFirst();
        if(toRemove.isPresent()){
            needToRegen.remove(toRemove.get());
            main.getConfig().set("ResetBlocksIfCrash", needToRegen);
            main.saveConfig();
        }
        else{
            main.getLogger().info("There was an issue removing a block from the crash reset protection.");
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Location location = event.getBlock().getLocation();
        List<String> needToRegen = new ArrayList<>();
        if(main.getConfig().contains("ResetBlocksIfCrash")){
            needToRegen = main.getConfig().getStringList("ResetBlocksIfCrash");
        }
        Optional<String> toRemove = needToRegen.stream().filter(s -> s.contains(location.getBlockX() + "," + location.getBlockY() + "," + location.getBlockZ() + "," + location.getWorld().getName())).findFirst();
        if(toRemove.isPresent()){
            event.setCancelled(true);
            return;
        }


        if(main.getRegenMaterial().contains(event.getBlock().getType())){
            int wobbleTime = (int)(Math.random() * main.getDelayWobble());
            saveNeedingRegen(event.getBlock().getLocation(), event.getBlock().getType());
            BlockState blockState = event.getBlock().getState();
            Bukkit.getScheduler().scheduleSyncDelayedTask(main, ()->{
                event.getBlock().setType(main.getTemporaryMaterial());
                Bukkit.getScheduler().scheduleSyncDelayedTask(main, ()->{
                    blockState.update(true);
                    Particle.DustOptions dustOptions = new Particle.DustOptions(Color.fromRGB(148,0,211), 1);
                    event.getBlock().getWorld().spawnParticle(Particle.REDSTONE, event.getBlock().getLocation(), 25, 1, 1, 1, dustOptions);
                    removeFromNeedRegen(event.getBlock().getLocation());
                }, (main.getDelayInSeconds() + wobbleTime) * 20);
            }, (main.getTimeToStone() + wobbleTime) * 20);
        }
    }
}
