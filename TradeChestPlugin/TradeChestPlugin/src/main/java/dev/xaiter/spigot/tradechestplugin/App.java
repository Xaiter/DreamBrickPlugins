package dev.xaiter.spigot.tradechestplugin;

import java.util.ArrayList;
import java.util.EnumSet;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class App extends JavaPlugin implements Listener {

    private static final EnumSet<InventoryAction> ALLOWED_INVENTORY_ACTIONS = EnumSet.of(InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_SOME, InventoryAction.PICKUP_ONE, 
                                                                                         InventoryAction.PLACE_ALL,  InventoryAction.PLACE_SOME,  InventoryAction.PLACE_ONE);

    private static final String TRADE_CHEST_NAME = "Trade Chest";

    private static final int SPAWN_ZONE_SCAN_X_MIN = 28000000;

    private static final int NO_OWNER_ID = 8;
    private static final int[] SLOT_ID_OWNERS = new int[] { 4, 4, 4, 4, NO_OWNER_ID, 2, 2, 2, 2,
                                                            4, 4, 4, 4, NO_OWNER_ID, 2, 2, 2, 2,
                                                            4, 4, 4, 5, NO_OWNER_ID, 3, 2, 2, 2 };
    private static final int ACCEPT_TRADE_TOKEN_MASK = 1;
    private static final int ACCEPT_TRADE_TOKEN_SLOT_A = 21;
    private static final int ACCEPT_TRADE_TOKEN_SLOT_B = 23;

    @Override
    public void onEnable() {
        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((Listener) this);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent e) {
        // No dragging allowed at all in Trade Chests!
        if (ShouldInterceptEvent(e)) {
            e.setCancelled(true);
            e.setResult(Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {

        // Most basic check - did you actually click an item?
        if(e.getCurrentItem() == null) {
            return;
        }

        // Next, is the action NOTHING?
        InventoryAction action = e.getAction();
        if (action == null || action == InventoryAction.NOTHING) {
            return;
        }

        // Okay, are you looking at a Trade Chest?
        if (!ShouldInterceptEvent(e))
            return;

        // When looking at the trade chest, only allow the most basic actions (PICK/PLACE)
        if (!ALLOWED_INVENTORY_ACTIONS.contains(action)) {
            CancelClickEvent(e);
            return;
        }

        // If the inventory being clicked isn't the chest, just let it happen (we've
        // already filtered out dangerous actions, like COLLECT_TO_CURSOR)
        if (e.getClickedInventory() != e.getView().getTopInventory()) {
            return;
        }

        // Copy slotId into the local, we'll be using it quite a bit.
        int slotId = e.getSlot();

        // Cancel and leave if they're messing with the NO_OWNER slots
        if (SLOT_ID_OWNERS[slotId] == NO_OWNER_ID) {
            CancelClickEvent(e);
            return;
        }

        // Grab the player, chest inventory, and their locations.
        HumanEntity player = e.getWhoClicked();
        Inventory chestInv = e.getView().getTopInventory();
        Location playerLoc = player.getLocation();
        Location chestLoc = chestInv.getLocation();

        // Prepare a variable to hold the player's X or Z value...
        double playerCompareAxisValue = 0;
        double chestCompareAxisValue = 0;

        // Now check the direction the chest is facing against the player's position...
        BlockInventoryHolder chest = (BlockInventoryHolder)chestInv.getHolder();
        BlockData blockData = chest.getBlock().getBlockData();
        Chest dc = (Chest) blockData;

        // Check Chest facing, grab perpendicular h-axis
        BlockFace facing = dc.getFacing();
        switch (facing) {
            case NORTH: {
                playerCompareAxisValue = playerLoc.getX();
                chestCompareAxisValue = chestLoc.getX();
                break;
            }

            case SOUTH: {
                // When comparing for the opposite side, sneak in a inversion for the ID lookup
                playerCompareAxisValue = -playerLoc.getX();
                chestCompareAxisValue = -chestLoc.getX();
                break;
            }

            case EAST: {
                playerCompareAxisValue = playerLoc.getZ();
                chestCompareAxisValue = chestLoc.getZ();
                break;
            }

            default: {
                // When comparing for the opposite side, sneak in a inversion for the ID lookup
                playerCompareAxisValue = -playerLoc.getZ();
                chestCompareAxisValue = -chestLoc.getZ();
                break;
            }
        }

        // Figure out which slots the player should "own"...
        int playerOwnerId = chestCompareAxisValue - playerCompareAxisValue < 0 ? 4 : 2;

        // PERMISSION CHECK: Verify the player is standing on the correct side of the
        // chest, cancel the event if they aren't!
        if ((playerOwnerId & SLOT_ID_OWNERS[slotId]) != playerOwnerId) {
            CancelClickEvent(e);
            return;
        }

        // They're modifying items they control (but not the trade token), so we'll
        // allow it and reset the trade token
        if ((SLOT_ID_OWNERS[slotId] & ACCEPT_TRADE_TOKEN_MASK) != ACCEPT_TRADE_TOKEN_MASK) {
            SetTokenState(chestInv, ACCEPT_TRADE_TOKEN_SLOT_A, false);
            SetTokenState(chestInv, ACCEPT_TRADE_TOKEN_SLOT_B, false);
            return;
        }

        // They tried to touch their "Accept Trade" token slot, so cancel the event and
        // update it in the chest
        e.setResult(Result.DENY);
        ItemStack currentItem = chestInv.getItem(slotId);
        boolean isRed = currentItem.getType() == Material.RED_WOOL;
        SetTokenState(chestInv, slotId, isRed);

        // Now grab both tokens...
        ItemStack tokenA = chestInv.getItem(ACCEPT_TRADE_TOKEN_SLOT_A);
        ItemStack tokenB = chestInv.getItem(ACCEPT_TRADE_TOKEN_SLOT_B);

        // If they aren't both green, nothing left to do here.
        if (tokenA.getType() != Material.GREEN_WOOL || tokenB.getType() != Material.GREEN_WOOL) {
            return;
        }

        // They're both green, time to swap
        ExecuteTrade(chestInv);

        // And reset the trade accept state
        SetTokenState(chestInv, ACCEPT_TRADE_TOKEN_SLOT_A, false);
        SetTokenState(chestInv, ACCEPT_TRADE_TOKEN_SLOT_B, false);
    }

    private static boolean ShouldInterceptEvent(InventoryEvent e) {
        Inventory topInv = e.getView().getTopInventory();
        String chestName = null;
        return
                // If topInv is null, this definitely isn't a chest
                topInv != null

                // Min X check - should filter out +99% of clicks quickly
                && topInv.getLocation().getBlockX() > SPAWN_ZONE_SCAN_X_MIN

                // Inventory type check - if not a chest interaction, we don't care. Quick.
                && topInv.getType() == InventoryType.CHEST

                // Name check for specific types of chests. Slow-ish.
                && (chestName = ((Nameable) topInv.getHolder()).getCustomName()) != null

                // Okay, confirm that if this is a named chest, it's a Trade Chest.
                && chestName.equals(TRADE_CHEST_NAME);
    }

    private static void SetTokenState(Inventory chestInv, int slotId, boolean acceptTrade) {
        chestInv.setItem(slotId, CreateAcceptTradeToken(acceptTrade));
    }

    private static ItemStack CreateAcceptTradeToken(boolean acceptTrade) {
        // Create the token, grab the metadata, and set a common friendly name
        ItemStack token = new ItemStack(acceptTrade ? Material.GREEN_WOOL : Material.RED_WOOL);
        ItemMeta tokenMeta = token.getItemMeta();
        tokenMeta.setDisplayName(ChatColor.GRAY + "Accept Trade");

        // Set the lore for comparison later... (also try to be user friendly)
        ArrayList<String> loreLines = new ArrayList<String>(1);
        loreLines.add(acceptTrade ? "Yes" : "No");
        tokenMeta.setLore(loreLines);
        token.setItemMeta(tokenMeta);

        return token;
    }

    private static void ExecuteTrade(Inventory chestInv) {
        int srcId = 0;
        int destId = 0;

        // Swappin' time
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 5; x++) {
                srcId = y * 9 + x;
                destId = y * 9 + 8 - x;
                SwapSlots(chestInv, srcId, destId);
            }
        }

        // Swappin' last row, odd case
        for (int x = 0; x < 4; x++) {
            srcId = 18 + x;
            destId = 26 - x;
            SwapSlots(chestInv, srcId, destId);
        }
    }

    private static void SwapSlots(Inventory inv, int srcId, int destId) {
        ItemStack tmp = inv.getItem(destId);
        inv.setItem(destId, inv.getItem(srcId));
        inv.setItem(srcId, tmp);
    }

    private static void CancelClickEvent(InventoryClickEvent e) {
        // I've had about enough of your shit, Spigot.
        e.setCancelled(true);
        e.setResult(Result.DENY);
    }
}