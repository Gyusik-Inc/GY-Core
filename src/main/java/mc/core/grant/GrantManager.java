package mc.core.grant;

import mc.core.utilites.data.GrantData;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GrantManager {

    private final Map<UUID, GrantData> playerGrants = new ConcurrentHashMap<>();
    private LuckPerms luckPerms;
    private Map<String, Integer> groupWeightCache = new HashMap<>();
    private Map<String, String> groupPrefixCache = new HashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_LIFETIME = 60000;

    public GrantManager() {
        try {
            this.luckPerms = LuckPermsProvider.get();
            updateGroupCache();
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to get LuckPerms API: " + e.getMessage());
        }
    }

    private void updateGroupCache() {
        if (luckPerms == null) return;

        groupWeightCache.clear();
        groupPrefixCache.clear();

        for (Group group : luckPerms.getGroupManager().getLoadedGroups()) {
            String groupName = group.getName().toLowerCase();
            int weight = group.getWeight().orElse(0);
            groupWeightCache.put(groupName, weight);

            String prefix = group.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
            groupPrefixCache.put(groupName, prefix != null ? prefix : "§7[" + group.getName() + "]");
        }

        lastCacheUpdate = System.currentTimeMillis();
    }

    private void checkAndUpdateCache() {
        if (System.currentTimeMillis() - lastCacheUpdate > CACHE_LIFETIME) {
            updateGroupCache();
        }
    }

    public GrantData getPlayerGrantData(UUID playerUuid) {
        return playerGrants.get(playerUuid);
    }

    public void loadPlayerData(Player player) {
        CompletableFuture.supplyAsync(() -> {
            String group = getPlayerPrimaryGroup(player);
            GrantData data = new GrantData(player.getUniqueId(), player.getName(), group);
            setGrantsFromLuckPerms(data, group);
            playerGrants.put(player.getUniqueId(), data);
            return data;
        });
    }

    private void setGrantsFromLuckPerms(GrantData data, String playerGroup) {
        if (luckPerms == null) {
            data.setGrantsByGroup(playerGroup);
            return;
        }

        checkAndUpdateCache();

        int playerGroupWeight = groupWeightCache.getOrDefault(playerGroup.toLowerCase(), 0);

        for (Map.Entry<String, Integer> entry : groupWeightCache.entrySet()) {
            String groupName = entry.getKey();
            int groupWeight = entry.getValue();

            if (groupWeight > playerGroupWeight) {
                data.getRemainingGrants().put(groupName, 1);
            }
        }
    }

    public String getPlayerPrimaryGroup(Player player) {
        if (luckPerms == null) return "default";

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "default";

        return user.getPrimaryGroup();
    }

    public String getGroupPrefix(String groupName) {
        checkAndUpdateCache();
        return groupPrefixCache.getOrDefault(groupName.toLowerCase(), "§7[" + groupName + "]");
    }

    public String getPlayerPrefix(Player player) {
        if (luckPerms == null || player == null) return "§7" + player.getName();

        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "§7" + player.getName();

        String prefix = user.getCachedData().getMetaData(QueryOptions.defaultContextualOptions()).getPrefix();
        return prefix != null ? prefix : "§7" + player.getName();
    }

    public int getGroupWeight(String groupName) {
        checkAndUpdateCache();
        return groupWeightCache.getOrDefault(groupName.toLowerCase(), 0);
    }

    public boolean canGrant(Player granter, Player target, String grantName) {
        GrantData granterData = playerGrants.get(granter.getUniqueId());
        if (granterData == null || !granterData.hasGrant(grantName)) {
            return false;
        }

        int grantWeight = getGroupWeight(grantName);
        String targetGroup = getPlayerPrimaryGroup(target);
        int targetWeight = getGroupWeight(targetGroup);

        return grantWeight > targetWeight;
    }

    public CompletableFuture<Boolean> giveGrant(Player granter, String targetPlayerName, String grantName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Player target = Bukkit.getPlayer(targetPlayerName);
                if (target == null) return false;

                if (!canGrant(granter, target, grantName)) {
                    return false;
                }

                if (luckPerms != null) {
                    User targetUser = luckPerms.getUserManager().getUser(target.getUniqueId());
                    if (targetUser == null) return false;

                    Group grantGroup = luckPerms.getGroupManager().getGroup(grantName);
                    if (grantGroup == null) return false;

                    InheritanceNode node = InheritanceNode.builder(grantGroup).build();
                    targetUser.data().add(node);
                    luckPerms.getUserManager().saveUser(targetUser);
                    luckPerms.getMessagingService().ifPresent(service -> service.pushUserUpdate(targetUser));
                }

                GrantData granterData = playerGrants.get(granter.getUniqueId());
                if (granterData != null) {
                    granterData.useGrant(grantName);
                    granterData.addToHistory(grantName, targetPlayerName, System.currentTimeMillis());
                }

                return true;
            } catch (Exception e) {
                Bukkit.getLogger().severe("Error giving grant: " + e.getMessage());
                return false;
            }
        });
    }

    public List<String> getAvailableGrants(Player player) {
        GrantData data = playerGrants.get(player.getUniqueId());
        if (data == null) return Collections.emptyList();

        List<String> available = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : data.getRemainingGrants().entrySet()) {
            if (entry.getValue() > 0) {
                available.add(entry.getKey());
            }
        }

        available.sort((g1, g2) -> {
            int w1 = getGroupWeight(g1);
            int w2 = getGroupWeight(g2);
            return Integer.compare(w1, w2);
        });

        return available;
    }

    public List<String> getAllGroups() {
        checkAndUpdateCache();
        return new ArrayList<>(groupWeightCache.keySet());
    }
}