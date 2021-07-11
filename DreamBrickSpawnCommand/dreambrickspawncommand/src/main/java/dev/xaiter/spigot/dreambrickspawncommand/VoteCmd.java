package dev.xaiter.spigot.dreambrickspawncommand;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class VoteCmd implements Listener, CommandExecutor {

    private final String PERMISSIONS_VOTE = "dreambrickspawncommand.vote";
    private final String VOTE_TELLRAW_FORMAT_STRING = "[\"\",{\"text\":\"Vote Links: \",\"color\":\"gray\"},{\"text\":\"[\",\"color\":\"gray\"},{\"text\":\"PMC\",\"color\":\"gold\",\"bold\":true,\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://www.planetminecraft.com/server/dream-s-end-survival/vote/?username={PLAYER_NAME}\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Planet Minecraft\",\"color\":\"white\"}]}}},{\"text\":\"]\",\"color\":\"gray\",\"bold\":false,\"underlined\":false},{\"text\":\" [\",\"color\":\"gray\"},{\"text\":\"MCSL\",\"color\":\"gold\",\"bold\":true,\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://minecraft-server-list.com/server/441565/vote/\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Minecraft Server List\",\"color\":\"white\"}]}}},{\"text\":\"] [\",\"color\":\"gray\",\"bold\":false,\"underlined\":false},{\"text\":\"MCMP\",\"color\":\"gold\",\"bold\":true,\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://minecraft-mp.com/server/225223/vote/?username={PLAYER_NAME}\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Minecraft Multiplayer\",\"color\":\"white\"}]}}},{\"text\":\"]\",\"color\":\"gray\",\"bold\":false,\"underlined\":false}]";

    public VoteCmd(JavaPlugin plugin) {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Permissions check!
        if(!sender.hasPermission(PERMISSIONS_VOTE)) {
            return false;
        }

        String formattedVoteMessage = VOTE_TELLRAW_FORMAT_STRING.replaceAll("\\{PLAYER_NAME\\}", sender.getName());
        Server s = Bukkit.getServer();
        s.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + sender.getName() + " " + formattedVoteMessage);
        return true;
    }
}