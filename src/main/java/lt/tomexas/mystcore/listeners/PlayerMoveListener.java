package lt.tomexas.mystcore.listeners;

import lt.tomexas.mystcore.MystPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    @EventHandler
    @SuppressWarnings("deprecation")
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        MystPlayer mystPlayer = MystPlayer.getMystPlayer(player);
        if (mystPlayer == null) return;
        if (player.getVelocity().getY() > 0) {
            double jumpVelocity = 0.42F;
            double jumpBoostOneVelocity = 0.52F;
            double jumpBoostTwoVelocity = 0.62F;

            if (!player.isOnGround() && !player.isFlying() && !player.isInvulnerable() && !player.isInWater() && player.getGameMode() != org.bukkit.GameMode.CREATIVE && player.getGameMode() != org.bukkit.GameMode.SPECTATOR) {
                if (Double.compare(player.getVelocity().getY(), jumpVelocity) == 0 || Double.compare(player.getVelocity().getY(), jumpBoostOneVelocity) == 0 || Double.compare(player.getVelocity().getY(), jumpBoostTwoVelocity) == 0) {
                    mystPlayer.setJumping(true);
                }
            }
        } else {
            mystPlayer.setJumping(false);
        }
    }
}
