package me.donut.auth;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DonutAuth extends JavaPlugin implements Listener, CommandExecutor {

    private final Set<UUID> loggedInPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        getCommand("register").setExecutor(this);
        getCommand("login").setExecutor(this);

        // Repeating task every 1 second to keep screen text and black vision active
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (!loggedInPlayers.contains(player.getUniqueId())) {
                    // Apply Black Vision (Blindness)
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 255, false, false));

                    // Check if registered or not and show Screen Text
                    if (isRegistered(player.getUniqueId())) {
                        player.sendTitle("§6§lᴅᴏɴᴜᴛ §f§lᴀᴜᴛʜ", "§e/Login §f<password>", 0, 40, 0);
                    } else {
                        player.sendTitle("§6§lᴅᴏɴᴜᴛ §f§lᴀᴜᴛʜ", "§a/Register §f<password> <finalpassword>", 0, 40, 0);
                    }
                }
            }
        }, 0L, 20L);
    }

    private boolean isRegistered(UUID uuid) {
        return getConfig().contains("passwords." + uuid.toString());
    }

    private void authenticatePlayer(Player player) {
        loggedInPlayers.add(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.sendTitle("§a§lʟᴏɢɢᴇᴅ ɪɴ!", "§7ᴡᴇʟᴄᴏᴍᴇ ᴛᴏ ᴛʜᴇ sᴇʀᴠᴇʀ", 5, 40, 10);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) return true;
        UUID uuid = player.getUniqueId();

        if (loggedInPlayers.contains(uuid)) {
            player.sendMessage("§cʏᴏᴜ ᴀʀᴇ ᴀʟʀᴇᴀᴅʏ ʟᴏɢɢᴇᴅ ɪɴ!");
            return true;
        }

        // /REGISTER COMMAND
        if (cmd.getName().equalsIgnoreCase("register")) {
            if (isRegistered(uuid)) {
                player.sendMessage("§cʏᴏᴜ ᴀʀᴇ ᴀʟʀᴇᴀᴅʏ ʀᴇɢɪsᴛᴇʀᴇᴅ! ᴜsᴇ §e/login <password>");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }

            if (args.length != 2) {
                player.sendMessage("§cᴜsᴀɢᴇ: §f/register <password> <finalpassword>");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }

            if (!args[0].equals(args[1])) {
                player.sendMessage("§cᴘᴀssᴡᴏʀᴅs ᴅᴏ ɴᴏᴛ ᴍᴀᴛᴄʜ! ᴛʀʏ ᴀɢᴀɪɴ.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }

            getConfig().set("passwords." + uuid.toString(), args[0]);
            saveConfig();

            player.sendMessage("§a§l✔ §aʏᴏᴜ ʜᴀᴠᴇ sᴜᴄᴄᴇssғᴜʟʟʏ ʀᴇɢɪsᴛᴇʀᴇᴅ!");
            authenticatePlayer(player);
            return true;
        }

        // /LOGIN COMMAND
        if (cmd.getName().equalsIgnoreCase("login")) {
            if (!isRegistered(uuid)) {
                player.sendMessage("§cʏᴏᴜ ᴀʀᴇ ɴᴏᴛ ʀᴇɢɪsᴛᴇʀᴇᴅ! ᴜsᴇ §a/register <password> <finalpassword>");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }

            if (args.length != 1) {
                player.sendMessage("§cᴜsᴀɢᴇ: §f/login <password>");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return true;
            }

            String savedPassword = getConfig().getString("passwords." + uuid.toString());
            if (args[0].equals(savedPassword)) {
                player.sendMessage("§a§l✔ §aʏᴏᴜ ʜᴀᴠᴇ sᴜᴄᴄᴇssғᴜʟʟʏ ʟᴏɢɢᴇᴅ ɪɴ!");
                authenticatePlayer(player);
            } else {
                player.sendMessage("§c§l✖ §cᴡʀᴏɴɢ ᴘᴀssᴡᴏʀᴅ! ᴛʀʏ ᴀɢᴀɪɴ.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
            return true;
        }

        return false;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        loggedInPlayers.remove(player.getUniqueId());
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 100, 255, false, false));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        loggedInPlayers.remove(event.getPlayer().getUniqueId());
    }

    // FREEZE MOVEMENT
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!loggedInPlayers.contains(event.getPlayer().getUniqueId())) {
            if (event.getTo() != null && (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ())) {
                event.setTo(event.getFrom());
            }
        }
    }

    // BLOCK OTHER COMMANDS BEFORE LOGIN
    @EventHandler
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!loggedInPlayers.contains(event.getPlayer().getUniqueId())) {
            String msg = event.getMessage().toLowerCase();
            if (!msg.startsWith("/login") && !msg.startsWith("/register")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cʏᴏᴜ ᴍᴜsᴛ ᴀᴜᴛʜᴇɴᴛɪᴄᴀᴛᴇ ғɪʀsᴛ!");
            }
        }
    }

    // PREVENT CHAT, INTERACTIONS, AND DAMAGE BEFORE LOGIN
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (!loggedInPlayers.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!loggedInPlayers.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!loggedInPlayers.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!loggedInPlayers.contains(event.getPlayer().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!loggedInPlayers.contains(event.getWhoClicked().getUniqueId())) event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (!loggedInPlayers.contains(player.getUniqueId())) event.setCancelled(true);
        }
    }
}
