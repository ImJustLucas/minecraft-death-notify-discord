package com.DeathEventPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class DeathEventPlugin extends JavaPlugin implements Listener {

    private static final String DISCORD_WEBHOOK_URL = "YOUR DISCORD WEBHOOK URL";


    @Override
    public void onEnable() {
        Bukkit.getLogger().info("DeathEventPlugin activé !");
        Bukkit.getServer().broadcastMessage("DeathEventPlugin, par @ImJustLucas, est activé");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().info("DeathEventPlugin désactivé !");
        Bukkit.getServer().broadcastMessage("DeathEventPlugin désactivé");

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String playerName = player.getName();
        String deathMessage = event.getDeathMessage();

        for (Player onlinePlayer : Bukkit.getServer().getOnlinePlayers()) {
            onlinePlayer.playSound(onlinePlayer.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1.0f, 1.0f);
        }

        int deathCount = getDeathCount(player);
        deathCount = deathCount + 1;

        sendDiscordEmbedMessage(playerName, playerName +" est mort !",  deathMessage + ". Nombre total de morts : " + deathCount, "16711680");
    }

    private int getDeathCount(Player player) {
        Scoreboard scoreboard = player.getScoreboard();

        Objective deathsObjective = scoreboard.getObjective("Morts");

        if (deathsObjective != null) {
            Score score = deathsObjective.getScore(player.getName());
            return score.getScore();
        } else {
            return 0;
        }
    }

    private void sendDiscordEmbedMessage(String playerName, String title, String description, String color) {
        try {
            URI uri = URI.create(DISCORD_WEBHOOK_URL);
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String headUrl = "https://minotar.net/avatar/" + playerName + "/100.png";

            String jsonPayload = "{"
                    + "\"embeds\": [{"
                    + "\"title\": \"" + title + "\","
                    + "\"description\": \"" + description + "\","
                    + "\"color\": " + color + ","
                    + "\"thumbnail\": { \"url\": \"" + headUrl + "\" }"
                    + "}]"
                    + "}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode != 204) { 
                Bukkit.getLogger().warning("Erreur lors de l'envoi du message embed à Discord. Code de réponse: " + responseCode);
            }

        } catch (Exception e) {
            Bukkit.getLogger().severe("Erreur lors de l'envoi du message embed à Discord: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
