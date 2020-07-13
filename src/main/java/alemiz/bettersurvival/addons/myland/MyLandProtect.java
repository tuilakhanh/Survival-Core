package alemiz.bettersurvival.addons.myland;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.ClanLand;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.commands.LandCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.SuperConfig;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.blockentity.BlockEntitySign;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockPlaceEvent;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.event.entity.EntityExplodeEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3f;
import cn.nukkit.utils.Config;

import java.util.*;


public class MyLandProtect extends Addon {

    private Map<String, List<Block>> selectors = new HashMap<>();
    private Map<String, LandRegion> lands = new HashMap<>();

    public static String WAND = "§6LandWand";
    public static String PERM_VIP = "bettersurvival.land.vip";
    public static String PERM_ACCESS = "bettersurvival.land.access";
    public static String PERM_ACCESS_CHEST = "bettersurvival.chest.access";

    public MyLandProtect(String path){
        super("mylandprotect", path);

        WAND = configFile.getString("wandName");
        PERM_VIP = configFile.getString("landsVipPermission");
        PERM_ACCESS = configFile.getString("landsAccessPermission");
        PERM_ACCESS_CHEST = configFile.getString("chestsAccessPermission");

        for (SuperConfig config : ConfigManager.getInstance().loadAllPlayers()){
            loadLand(config);
        }
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")){
            configFile.set("enable", true);
            configFile.set("enablePrivateChests", true);

            configFile.set("wandName", "§6Land§eWand");

            configFile.set("landsLimit", 2);
            configFile.set("landsLimitSize", 50);
            configFile.set("landsLimitSizeVip", 100);

            configFile.set("landsVipPermission", "bettersurvival.land.vip");
            configFile.set("landsAccessPermission", "bettersurvival.land.access");
            configFile.set("chestsAccessPermission", "bettersurvival.chest.access");

            configFile.set("landNotExists", "§6»§7Vùng đất §6{land}§7 khổng thể tìm thấy!");
            configFile.set("landWithNameExists", "§6»§7Vùng đất §6{land}§7 không tồn tại§7!");
            configFile.set("landWarn", "§6»§7Hey §6{player}§7, đây không phải khu vực của bạn! Hỏi §6{owner} §7để có quyền ở §6{land}§7!");
            configFile.set("landTooBig", "§6»§7Vùng đất bạn đã chọn lớn hơn giới hạn cho phép là §6{limit} blocks§7!");
            configFile.set("landPosSelected", "§6»§7Hoàn thành chọn vị trí {select} tại §6{pos}§7!");
            configFile.set("landLimitWarn", "§6»§7Đã đạt giới hạn vùng đất cho phép!");
            configFile.set("landHereNotFound", "§6»§7Vùng đất này được miễn phí§7!");

            configFile.set("landCreate", "§6»§7Bạn đã tạo vùng đất mới tên §6{land}§7! Và bạn có §6{limit}§7 vùng đất miễn phí!");
            configFile.set("landRemove", "§6»§7Bạn đã xoá vùng đất của bạn §6{land}§7!");
            configFile.set("landSetPos", "§6»§7Touch 2 blocks with wand to select border positions§7!");
            configFile.set("landWhitelist", "§6»§7Danh sách trắng cho §6{land}§7 đã được lưu§7!");
            configFile.set("landWhitelistList", "§6»{land}§7 truy cập: {players}");
            configFile.set("landHere", "§6»§7Vùng đất §6{land}§7 thuộc sở hữu của §6{owner}§7!");
            configFile.set("landList", "§6»§7Vùng đất của bạn: {lands}");

            configFile.set("landWhitelistAdd", "§6»§7Bạn đã cho §6{player}§7 vào vùng đất §6{land}§7!");
            configFile.set("landWhitelistRemove", "§6»§7Đã xoá §6{player}§7 khỏi vùng đất của bạn §6{land}§7!");

            configFile.set("landClanExists", "§6»§7Clan §6{clan}§7 đã có vùng đất riêng!");

            configFile.set("privateChestCreate", "§6»§r§7Bạn đã tạo rương riêng của mình!");
            configFile.set("privateChestAccessDenied", "§c»§r§7Rường thuộc quyền sở hữu bởi §6{owner}§7! Bạn không có quyền sử dụng nó.");
            configFile.set("privateChestDestroy", "§c»§r§7Bạn đã phá huỷ rương riêng!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        if (configFile.getBoolean("enable", true)){
            registerCommand("land", new LandCommand("land", this));
        }
    }

    @EventHandler
    public void onBlockTouch(PlayerInteractEvent event){
        Player player = event.getPlayer();
        Block block = event.getBlock();

        String item = player.getInventory().getItemInHand().getName();

        if (event.getAction() == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK && item.equals(WAND)){
            List<Block> blocks = new ArrayList<>();

            if (selectors.containsKey(player.getName().toLowerCase())){
                blocks = selectors.get(player.getName().toLowerCase());
            }

            if (blocks.size() >= 2) blocks.clear();

            blocks.add(event.getBlock());
            selectors.put(player.getName().toLowerCase(), blocks);

            String message = configFile.getString("landPosSelected");
            message = message.replace("{pos}", event.getBlock().x +", "+ event.getBlock().y +", "+ event.getBlock().z);
            message = message.replace("{player}", player.getName());
            message = message.replace("{select}", (blocks.size() == 1)? "thứ nhất" : "thứ hai");
            player.sendMessage(message);
            event.setCancelled();
            return;
        }

        LandRegion region = getLandByPos(event.getBlock());
        if (!interact(player, region)) {
            event.setCancelled(true);
            return;
        }

        if (!configFile.getBoolean("enablePrivateChests", false)) return;

        if (block.getId() == Block.CHEST && (block.getLevel().getBlockEntity(block) instanceof BlockEntityChest)){
            BlockEntityChest chest = (BlockEntityChest) block.getLevel().getBlockEntity(block);

            if (!this.interactChest(player, chest)){
                event.setCancelled();
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        LandRegion region = this.getLandByPos(block);
        if (!this.interact(player, region)){
            event.setCancelled(true);
            return;
        }

        if (!configFile.getBoolean("enablePrivateChests", false)) return;

        if ((block.getId() == Block.WALL_SIGN || block.getId() == Block.SIGN_POST) && (player.getLevel().getBlockEntity(block) instanceof BlockEntitySign)){
            BlockEntitySign sign =  (BlockEntitySign) player.getLevel().getBlockEntity(block);
            if (this.signInteract(event, sign)) return;
        }

        if (block.getId() == Block.CHEST && (block.getLevel().getBlockEntity(block) instanceof BlockEntityChest)){
            BlockEntityChest chest = (BlockEntityChest) block.getLevel().getBlockEntity(block);

            if (!this.interactChest(player, chest)){
                event.setCancelled();
            }
        }
    }

    private boolean signInteract(BlockBreakEvent event, BlockEntitySign sign){
        Player player = event.getPlayer();
        String[] lines = sign.getText();

        if (!lines[0].equals("[private]") && !lines[0].equals("§r§f[§clocked§f]")) return false;
        BlockEntityChest chest = this.getChestBySign(sign);

        if (chest == null) return false;

        switch (lines[0]){
            case "§r§f[§clocked§f]":
                String owner = this.getPrivateChestOwner(chest);
                if (owner == null || this.removePrivateChest(player, chest)) break;
                event.setCancelled();
                break;
            case "[private]":
                if (this.createPrivateChest(player, chest)){
                    event.setCancelled();
                    sign.setText("§r§f[§clocked§f]", "§a"+player.getName());
                }
                break;
        }

        return true;
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();

        LandRegion region = getLandByPos(block);
        if (!interact(player, region)){
            event.setCancelled();
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event){
        for (Position block : event.getBlockList()){
            if (getLandByPos(block) == null) continue;
            event.setBlockList(new ArrayList<>());
            return;
        }
    }

    @EventHandler
    public void onItemFrame(ItemFrameDropItemEvent event){
        Position pos = event.getItemFrame();
        Player player = event.getPlayer();

        LandRegion region = getLandByPos(pos);
        if (!interact(player, region)){
            event.setCancelled();
        }
    }

    public boolean interact(Player player, LandRegion region){
        if (region == null) return true;
        if (region.owner.equals(player.getName().toLowerCase()) || region.whitelist.contains(player.getName().toLowerCase())) return true;
        if (player.isOp() || player.hasPermission(PERM_ACCESS)) return true;

        boolean clanLand = region instanceof ClanLand;
        if (clanLand){
            Clan clan = ((ClanLand) region).getClan();
            if (clan != null && clan.isMember(player)) return true;
        }

        String message = configFile.getString("landWarn");
        message = message.replace("{land}", clanLand? "" : region.land);
        message = message.replace("{player}", player.getName());
        message = message.replace("{owner}", region.owner + (clanLand? " Clan" : ""));
        player.sendMessage(message);
        return false;
    }

    public LandRegion getLandByPos(Position position){
        for (LandRegion region : this.lands.values()){
            if (position.getLevel() != null && !region.level.getFolderName().equals(position.getLevel().getFolderName())) continue;

            if (position.x >= Math.min(region.pos1.x, region.pos2.x) && position.x <= Math.max(region.pos1.x, region.pos2.x)
                    && position.y >= Math.min(region.pos1.y, region.pos2.y) && position.y <= Math.max(region.pos1.y, region.pos2.y)
                    && position.z >= Math.min(region.pos1.z, region.pos2.z) && position.z <= Math.max(region.pos1.z, region.pos2.z))
                return region;
        }

        return null;
    }

    public boolean interactChest(Player player, BlockEntityChest chest){
        if (player == null || chest == null) return true;
        if (player.isOp() || player.hasPermission(PERM_ACCESS_CHEST)) return true;

        String owner = this.getPrivateChestOwner(chest);
        if (owner == null || owner.equalsIgnoreCase(player.getName())) return true;

        String message = configFile.getString("privateChestAccessDenied");
        message = message.replace("{player}", player.getName());
        message = message.replace("{owner}", owner);
        player.sendMessage(message);

        return false;
    }

    public BlockEntityChest getChestBySign(BlockEntity block){
        if (block.getLevel().getBlockEntity(block.north()) instanceof BlockEntityChest){
            return (BlockEntityChest) block.getLevel().getBlockEntity(block.north());
        }else if (block.getLevel().getBlockEntity(block.south()) instanceof BlockEntityChest){
            return (BlockEntityChest) block.getLevel().getBlockEntity(block.south());
        }else if (block.getLevel().getBlockEntity(block.east()) instanceof BlockEntityChest){
            return (BlockEntityChest) block.getLevel().getBlockEntity(block.east());
        }else if (block.getLevel().getBlockEntity(block.west()) instanceof BlockEntityChest){
            return (BlockEntityChest) block.getLevel().getBlockEntity(block.west());
        }
        return null;
    }

    public Set<String> getLands(Player player){
        return getLands(player.getName());
    }

    public Set<String> getLands(String player){
        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return null;

        return config.getSection("land").getKeys(false);
    }

    public boolean validateLand(List<Block> blocks){
        return validateLand(blocks, null, false);
    }

    public boolean validateLand(List<Block> blocks, Player player, boolean clanMode){
        if (blocks == null || blocks.isEmpty()) return false;

        if (blocks.size() < 2){
            if (player != null){
                player.sendMessage(configFile.getString("landSetPos"));
            }
            return false;
        }

        //1. Check if lanf is in spawn
        for (Block block : blocks){
            if (!block.getLevel().isInSpawnRadius(block)) continue;
            player.sendMessage("§c»§7Bạn không thể tạo vùng đất ở khu spawn!");
            return false;
        }

        //2. Check if land is in other land
        LandRegion region = null;
        for (Block block : blocks){
            if ((region = this.getLandByPos(block)) != null){
                break;
            }
        }

        if (region != null && !region.owner.equalsIgnoreCase(player.getName()) && !player.hasPermission(PERM_ACCESS)){
            boolean cancel = true;
            if (Addon.getAddon("playerclans") != null && (region instanceof ClanLand)){
                PlayerClans playerClans = (PlayerClans) Addon.getAddon("playerclans");
                Clan clan = playerClans.getClan(player);
                if (clan != null && clan.getName().equals(((ClanLand) region).getClan().getName())){
                    cancel = false;
                }
            }

            if (cancel){
                String message = configFile.getString("landWarn");
                message = message.replace("{land}", (region instanceof ClanLand)? "" : region.land);
                message = message.replace("{player}", player.getName());
                message = message.replace("{owner}", region.owner + ((region instanceof ClanLand)? " Clan" : ""));
                player.sendMessage(message);
                return false;
            }
        }

        //3. Check land sizes
        int landSize;
        if (clanMode){
            landSize = 5*75; //TODO: calculate by clan player count & allow resizing
        }else {
            landSize = configFile.getInt("landsLimitSize");
            if (player != null && player.hasPermission(PERM_VIP)){
                landSize = configFile.getInt("landsLimitSizeVip");
            }
        }

        if (player != null && player.isOp()) return true;

        if ((Math.max(blocks.get(0).x, blocks.get(1).x) - Math.min(blocks.get(0).x, blocks.get(1).x)) > landSize ||
                (Math.max(blocks.get(0).y, blocks.get(1).y) - Math.min(blocks.get(0).y, blocks.get(1).y)) > landSize ||
                (Math.max(blocks.get(0).z, blocks.get(1).z) - Math.min(blocks.get(0).z, blocks.get(1).z)) > landSize){

            if (player != null){
                String message = configFile.getString("landTooBig");
                message = message.replace("{player}", player.getName());
                message = message.replace("{limit}", String.valueOf(landSize));
                player.sendMessage(message);
            }
            return false;
        }
        return true;
    }

    public void loadLand(SuperConfig config){
       String owner = config.getName().substring(0, config.getName().lastIndexOf("."));
       this.plugin.getLogger().info("§eLoading lands for player §3"+owner+"§e!");

       for (String land : config.getSection("land").getKeys(false)){
           Level level = this.plugin.getServer().getLevelByName(config.getString("land."+land+"level"));
           LandRegion region = new LandRegion(owner, land, level);

           List<Integer> data = config.getIntegerList("land."+land+".pos0");
           region.pos1 = new Vector3f(data.get(0), data.get(1), data.get(2));

           data = config.getIntegerList("land."+land+".pos1");
           region.pos2 = new Vector3f(data.get(0), data.get(1), data.get(2));

           region.whitelist = config.getStringList("land."+land+".whitelist");
           this.lands.put(owner.toLowerCase()+"-"+land, region);

           region.save();
       }
    }

    public void loadClanLand(Clan clan){
        Config config = clan.getConfig();
        this.plugin.getLogger().info("§eLoading land for clan §3"+clan.getName()+"§e!");

        Level level = this.plugin.getServer().getLevelByName(config.getString("land.level"));
        ClanLand region = new ClanLand(clan, level);

        List<Integer> data = config.getIntegerList("land.pos0");
        region.pos1 = new Vector3f(data.get(0), data.get(1), data.get(2));

        data = config.getIntegerList("land.pos1");
        region.pos2 = new Vector3f(data.get(0), data.get(1), data.get(2));

        this.lands.put(clan.getRawName(), region);
        region.save();
    }

    public void createLand(Player player, String land){
        this.createLand(player, land, false);
    }

    public void createLand(Player player, String land, boolean clanMode){
        Runnable task = () -> {
            if (player == null || !player.isConnected()) return;
            Config config;
            Clan clan = null;
            int freeLands = 0;

            if (clanMode){
                clan = ((PlayerClans) Addon.getAddon("playerclans")).getClan(player);
                if (clan == null) {
                    player.sendMessage("§c»§7Bạn không có ở clan nào!");
                    return;
                }

                config = clan.getConfig();
                if (config.exists("land")){
                    String message = configFile.getString("landClanExists");
                    message = message.replace("{clan}", clan.getName());
                    player.sendMessage(message);
                    return;
                }
            }else {
                config = ConfigManager.getInstance().loadPlayer(player);
                if (config == null) return;
                int limit = configFile.getInt("landsLimit");
                Set<String> lands = config.getSection("land").getKeys(false);
                freeLands = limit - lands.size();

                if (freeLands < 1 && !player.isOp()){
                    String message = configFile.getString("landLimitWarn");
                    message = message.replace("{land}", land);
                    message = message.replace("{player}", player.getName());
                    player.sendMessage(message);
                    return;
                }

                if (lands != null && lands.contains(land.toLowerCase())){
                    String message = configFile.getString("landWithNameExists");
                    message = message.replace("{land}", land);
                    message = message.replace("{player}", player.getName());
                    player.sendMessage(message);
                    return;
                }
            }

            if (!this.selectors.containsKey(player.getName().toLowerCase())){
                player.sendMessage(configFile.getString("landSetPos"));
                return;
            }

            player.sendMessage("§6»§r§7Đang xác thực vùng đất. Vui lòng đợi ...");

            List<Block> blocks = this.selectors.get(player.getName().toLowerCase());
            if (!validateLand(blocks, player, clanMode)){
                selectors.remove(player.getName().toLowerCase());
                return;
            }

            for (int i = 0; i < blocks.size(); i++){
                Block block = blocks.get(i);
                Double[] pos = {block.getX(), block.getY(), block.getZ()};

                String index = clanMode? "land.pos"+i : "land."+land.toLowerCase()+".pos"+i;
                config.set(index, pos);
            }

            if (!clanMode) config.set("land."+land.toLowerCase()+".whitelist", new String[0]);

            String message = configFile.getString("landCreate");
            message = message.replace("{land}", (clanMode? clan.getName()+" land" : land));
            message = message.replace("{player}", player.getName());
            message = message.replace("{limit}", player.isOp()? "không giới hạn" : String.valueOf(freeLands));
            player.sendMessage(message);

            this.selectors.remove(player.getName().toLowerCase());
            config.save();

            LandRegion region = clanMode? new ClanLand(clan, player.getLevel()) : new LandRegion(player.getName().toLowerCase(), land.toLowerCase(), player.getLevel());
            region.pos1 = blocks.get(0).asVector3f();
            region.pos2 = blocks.get(1).asVector3f();
            this.lands.put(clanMode? clan.getRawName() : player.getName().toLowerCase()+"-"+land.toLowerCase(), region);
        };
        this.plugin.getServer().getScheduler().scheduleTask(this.plugin, task, true);
    }

    public void removeLand(Player player, String land){
        if (player == null || !player.isConnected()) return;

        Config config = ConfigManager.getInstance().loadPlayer(player);
        if (config == null) return;

        if (!config.exists("land."+land.toLowerCase())){
            String message = configFile.getString("landNotExists");
            message = message.replace("{land}", land);
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        ((Map) config.get("land")).remove(land.toLowerCase());
        config.save();

        this.lands.remove(player.getName().toLowerCase()+"-"+land.toLowerCase());

        String message = configFile.getString("landRemove");
        message = message.replace("{player}", player.getName());
        message = message.replace("{land}", land);
        player.sendMessage(message);
    }

    public void removeClanLand(Player player){
        if (player == null) return;

        Clan clan = ((PlayerClans) Addon.getAddon("playerclans")).getClan(player);
        if (clan == null) {
            player.sendMessage("§c»§7Bạn không có ơ clan!");
            return;
        }

        Config config = clan.getConfig();
        config.remove("land");
        config.save();

        this.lands.remove(clan.getRawName());

        String message = configFile.getString("landRemove");
        message = message.replace("{player}", player.getName());
        message = message.replace("{land}", clan.getName());
        player.sendMessage(message);
    }

    public void findLand(Player player){
        Block block = player.getLevel().getBlock(player.clone());
        LandRegion land = null;

        if (block == null || (land = getLandByPos(block)) == null){
            String message = configFile.getString("landHereNotFound");
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        if (land instanceof ClanLand){
            player.sendMessage("§6»§7Vùng đất thuộc quyền sở hưu của Clan §6"+land.owner+"!");
            return;
        }

        String message = configFile.getString("landHere");
        message = message.replace("{owner}", land.owner);
        message = message.replace("{land}", land.land);
        player.sendMessage(message);

    }

    public void whitelist(Player owner, String player, String land, String action){
        LandRegion region = this.lands.get(owner.getName().toLowerCase()+"-"+land.toLowerCase());

        if (region == null){
            String message = configFile.getString("landNotExists");
            message = message.replace("{land}", land);
            message = message.replace("{player}", owner.getName());
            owner.sendMessage(message);
            return;
        }

        if (!region.owner.equals(owner.getName().toLowerCase())){
            String message = configFile.getString("landWarn");
            message = message.replace("{land}", region.land);
            message = message.replace("{player}", owner.getName());
            message = message.replace("{owner}", region.owner);
            owner.sendMessage(message);
            return;
        }

        switch (action){
            case LandRegion.WHITELIST_ADD:
                region.addWhitelist(player);
                break;
            case LandRegion.WHITELIST_REMOVE:
                region.whitelistRemove(player);
                break;
            case LandRegion.WHITELIST_LIST:
                String players = String.join(", ", region.whitelist);

                String message = configFile.getString("landWhitelistList");
                message = message.replace("{land}", region.land);
                message = message.replace("{player}", owner.getName());
                message = message.replace("{players}", players);
                owner.sendMessage(message);
                return; //exit
        }

        String message = configFile.getString("landWhitelist");
        message = message.replace("{land}", region.land);
        message = message.replace("{player}", owner.getName());
        owner.sendMessage(message);
    }

    public boolean createPrivateChest(Player player, BlockEntityChest chest){
        if (player == null || chest == null) return false;

        String owner = this.getPrivateChestOwner(chest);
        if (owner != null){
            player.sendMessage("§c»§r§7Rưởng này đã thược quyền sở hữu của §6"+owner+"§7!");
            return false;
        }

        chest.namedTag.putString("private_owner", player.getName());

        String message = configFile.getString("privateChestCreate");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
        return true;
    }

    public boolean removePrivateChest(Player player, BlockEntityChest chest){
        if (player == null || chest == null) return false;

        BlockEntityChest privateChest = this.getPrivateChest(chest);
        if (privateChest == null) return false;

        String owner = privateChest.namedTag.getString("private_owner");

        if (!owner.equalsIgnoreCase(player.getName()) && !player.isOp() && !player.hasPermission(PERM_ACCESS_CHEST)){
            String message = configFile.getString("privateChestAccessDenied");
            message = message.replace("{player}", player.getName());
            message = message.replace("{owner}", owner);
            player.sendMessage(message);
            return false;
        }

        privateChest.namedTag.remove("private_owner");
        String message = configFile.getString("privateChestDestroy");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
        return true;
    }

    public boolean isPrivateChest(Position pos){
        BlockEntity entity = pos.getLevel().getBlockEntity(pos);
        if (!(entity instanceof BlockEntityChest)) return false;

        return this.isPrivateChest((BlockEntityChest) entity);
    }

    public boolean isPrivateChest(BlockEntityChest chest){
        return this.getPrivateChestOwner(chest) != null;
    }

    public String getPrivateChestOwner(Position pos){
        BlockEntity entity = pos.getLevel().getBlockEntity(pos);
        if (!(entity instanceof BlockEntityChest)) return null;

        return this.getPrivateChestOwner((BlockEntityChest) entity);
    }

    public String getPrivateChestOwner(BlockEntityChest chest){
        if (chest == null) return null;

        String owner = null;
        if (chest.namedTag.contains("private_owner")){
            owner = chest.namedTag.getString("private_owner");
        }else if (chest.isPaired() && chest.getPair().namedTag.contains("private_owner")){
            owner = chest.getPair().namedTag.getString("private_owner");
        }

        return owner;
    }

    public BlockEntityChest getPrivateChest(Position pos){
        if (pos == null) return null;
        BlockEntity entity = pos.getLevel().getBlockEntity(pos);

        if (!(entity instanceof BlockEntityChest)) return null;
        BlockEntityChest chest = (BlockEntityChest) entity;

        if (!chest.namedTag.getString("private_owner").equals("")){
            return chest;
        }else if (chest.isPaired() && !chest.getPair().namedTag.getString("private_owner").equals("")){
            return chest.getPair();
        }
        return null;
    }

    public void listLands(Player player){
        Set<String> lands = this.getLands(player);

        String message = configFile.getString("landList");
        message = message.replace("{lands}", String.join(", ", lands));
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }
}
