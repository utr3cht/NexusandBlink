package com.example.annihilationnexus;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

public class PistonListener implements Listener {

    private final AnnihilationNexus plugin;

    public PistonListener(AnnihilationNexus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (checkBlocks(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (checkBlocks(event.getBlocks())) {
            event.setCancelled(true);
        }
    }

    private boolean checkBlocks(List<Block> blocks) {
        for (Block block : blocks) {
            // Check for Nexus
            if (block.getType() == plugin.getNexusMaterial()) {
                return true;
            }

            // Check for Transporter Portal
            if (TransporterAbility.isPortalBlock(block)) {
                return true;
            }
        }
        return false;
    }
}
