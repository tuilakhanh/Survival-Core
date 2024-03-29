package alemiz.bettersurvival.addons;

import alemiz.bettersurvival.commands.*;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.fakeChest.FakeInventory;
import alemiz.bettersurvival.utils.fakeChest.FakeInventoryManager;
import alemiz.bettersurvival.utils.fakeChest.FakeSlotChangeEvent;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.inventory.transaction.action.SlotChangeAction;
import cn.nukkit.item.Item;
import cn.nukkit.level.GlobalBlockPalette;
import cn.nukkit.level.Level;
import cn.nukkit.level.Sound;
import cn.nukkit.level.particle.HugeExplodeParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.UpdateBlockPacket;
import cn.nukkit.scheduler.Task;
import cn.nukkit.utils.DummyBossBar;

import java.util.*;

public class Troller extends Addon {

    public final static int BLOCK_UPDATE_RANDOM_FLAT = 0;
    public final static int BLOCK_UPDATE_HOLE_WITH_LAVA = 1;

    protected List<String> vanishPlayers = new ArrayList<>();
    protected Map<String, List<Block>> blocksBefore = new HashMap<>();

    public Troller(String path){
        super("troller", path);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);

            configFile.set("playerNotFound", "§6»§7Không tiềm thấy người chơi!");
            configFile.set("blockNotFound", "§6»§7ID {id} không đúng!");

            configFile.set("permission-vanish", "bettersurvival.troller.vanish");
            configFile.set("vanishMessage", "§6»§7Woosh! Chế độ tàn hình đã được bật §6{state}!");

            configFile.set("permission-block", "bettersurvival.troller.block");
            configFile.set("blockMessage", "§6»§7Block đã được spawn quanh §6{victim}§7!");
            configFile.set("unblockMessage", "§6»{victim}§7 đã được thả!");

            configFile.set("permission-troll", "bettersurvival.troller.basic");
            configFile.set("permission-troll-advanced", "bettersurvival.troller.advanced");

            configFile.set("permission-invsee", "bettersurvival.troller.invsee");

            /*Basic Troll commands*/
            configFile.set("anvilMessage", "§6»§7Anvil đã rơi vào người §6{victim}§7!");
            configFile.set("chatMessage", "§6»§7It looks like §6{victim}§7 is unsure what to say§7!");
            configFile.set("rainbowFloorMessage", "§6»§7Người chơi §6{victim}§7 bị spam với một đống len§7!");
            configFile.set("fakeLavaMessage", "§6»§7Người chơi §6{victim}§7 muốn bơi trong lava§7!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        registerCommand("vanish", new VanishCommand("vanish", this));
        registerCommand("block", new BlockCommand("block", this));
        registerCommand("unblock", new UnblockCommand("unblock", this));
        registerCommand("troll", new TrollCommand("troll", this));
        registerCommand("invsee", new InvseeCommand("invsee", this));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();

        for (String name : this.vanishPlayers){
            Player pplayer = plugin.getServer().getPlayer(name);
            if (player == null || !player.isConnected()) continue;

            player.hidePlayer(pplayer);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();

        this.vanishPlayers.remove(player.getName());
        for (Player pplayer : plugin.getServer().getOnlinePlayers().values()){
            pplayer.showPlayer(player);
        }
    }

    /*@EventHandler //TODO: allow transferring items of invsee
    public void onInventoryTranslation(FakeSlotChangeEvent event){
        Player player = event.getPlayer();
        SlotChangeAction action = event.getAction();

    }*/

    public void showVanishPlayers(Player player){
        for (String pname : this.vanishPlayers){
            Player pplayer = plugin.getServer().getPlayer(pname);

            if (pplayer == null || !pplayer.isConnected()){
                this.vanishPlayers.remove(pname);
                continue;
            }

            player.showPlayer(pplayer);
        }
    }

    public void hideVanishPlayers(Player player){
        for (String pname : this.vanishPlayers){
            Player pplayer = plugin.getServer().getPlayer(pname);

            if (pplayer == null || !pplayer.isConnected()){
                this.vanishPlayers.remove(pname);
                continue;
            }

            player.hidePlayer(pplayer);
        }
    }

    public void vanish(Player player){
        if (!player.hasPermission(configFile.getString("permission-vanish"))){
            player.sendMessage("§cYou dont have permission to vanish!");
            return;
        }

        DummyBossBar bossBar = null;
        long bossBarId = 0;
        if (Addon.getAddon("betterlobby") != null && Addon.getAddon("betterlobby").isEnabled()){
            bossBarId = ((BetterLobby) Addon.getAddon("betterlobby")).getBossBars().get(player.getName());
            bossBar = ((BetterLobby) Addon.getAddon("betterlobby")).buildBossBar(player);
        }

        boolean hidden = this.vanishPlayers.contains(player.getName());
        if (hidden){
            this.vanishPlayers.remove(player.getName());
            hideVanishPlayers(player);
        }else {
            this.vanishPlayers.add(player.getName());
            showVanishPlayers(player);
            if (bossBar != null) bossBar.setText(bossBar.getText()+" §7- §3Vanished");
        }

        for (Player pplayer : plugin.getServer().getOnlinePlayers().values()){
            if (hidden){
                if (this.vanishPlayers.contains(pplayer.getName())){
                    player.hidePlayer(pplayer);
                    continue;
                }
                pplayer.showPlayer(player);
            }else{
                if (this.vanishPlayers.contains(pplayer.getName())){
                    player.showPlayer(pplayer);
                    continue;
                }
                pplayer.hidePlayer(player);
            }
        }

        if (bossBar != null){
            player.removeBossBar(bossBarId);
            player.createBossBar(bossBar);
        }

        String message = configFile.getString("vanishMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{state}", ((hidden)? "off" : "on"));
        player.sendMessage(message);
    }

    public List<Block> setBlocksArround(Player player, Block block){
        Vector3 base = player.clone();
        List<Vector3> positions = new ArrayList<Vector3>(){{
            add(base.add(0, -1, 0));
            add(base.add(0, 2, 0));

            add(base.add(1, 0, 0));
            add(base.add(1, 1, 0));
            add(base.add(-1, 0, 0));
            add(base.add(-1, 1, 0));

            add(base.add(0, 0, 1));
            add(base.add(0, 1, 1));
            add(base.add(0, 0, -1));
            add(base.add(0, 1, -1));
        }};

        List<Block> blocksBefore = new ArrayList<>();
        for (Vector3 position : positions){
            blocksBefore.add(player.level.getBlock(position));
            player.getLevel().setBlock(position, block, true, true);
        }

        return blocksBefore;
    }

    public void replaceBlocks(List<Block> replaceWith){
        for (Block block : replaceWith){
            Level level = block.getLevel();
            if (level == null) continue;

            level.setBlock(new Vector3(block.x, block.y, block.z), block, true, true);
        }
    }

    public List<UpdateBlockPacket> generateBlockUpdate(Player player, int type){
        Level level = player.getLevel();
        List<UpdateBlockPacket> packets = new ArrayList<>();

        switch (type){
            case BLOCK_UPDATE_RANDOM_FLAT:
                for (double x = player.getX()-14; x < player.getX()+14; x++){
                    for (double y = player.getY()-5; y < player.getY()+5; y++){
                        for (double z = player.getZ()-14; z < player.getZ()+14; z++){
                            int blockId = level.getBlock((int) x, (int) y, (int) z).getId();
                            switch (blockId){
                                case Block.AIR:
                                    continue;
                                case Block.LEAVES:
                                case Block.LEAVES2:
                                case Block.WOOD:
                                    if (new Random().nextInt(3) == 2) continue;
                            }

                            Block block = Block.get(Block.WOOL, new Random().nextInt(14));
                            block.x = x;
                            block.y = y;
                            block.z = z;

                            packets.add(createUpdatePacket(block));
                        }
                    }
                }
                break;
            case BLOCK_UPDATE_HOLE_WITH_LAVA:
                for (double y = player.getY()-3; y <= player.getY(); y++){
                    Block block = Block.get(Block.AIR);
                    if (y == (player.getY()-3)) block = Block.get(Block.LAVA);

                    for (double x = player.getX()-2; x < (player.getX()+1); x++){
                        for (double z = player.getZ()-2; z < (player.getZ()+1); z++){
                            block.x = x;
                            block.y = y;
                            block.z = z;
                            packets.add(createUpdatePacket(block));
                        }
                    }
                }

                break;
        }

        return packets;
    }

    public UpdateBlockPacket createUpdatePacket(Block block){
        UpdateBlockPacket updateBlockPacket = new UpdateBlockPacket();
        updateBlockPacket.x = (int) block.x;
        updateBlockPacket.y = (int) block.y;
        updateBlockPacket.z = (int) block.z;
        updateBlockPacket.flags = 0;
        try {
            updateBlockPacket.blockRuntimeId = GlobalBlockPalette.getOrCreateRuntimeId(block.getFullId());
        } catch (NoSuchElementException var18) {
            throw new IllegalStateException("Unable to create BlockUpdatePacket at (" + block.x + ", " + block.y + ", " + block.z + ") in " + block.getName(), var18);
        }
        return updateBlockPacket;
    }

    public void sendRealChunks(Vector3 pos, int interval, int maxChunks,  Player[] players){
        plugin.getServer().getScheduler().scheduleDelayedTask(new Task() {
            @Override
            public void onRun(int i) {
                if (maxChunks == 0){ //just chunk of pos
                    for (Player player : players){
                        player.getLevel().requestChunk((int) pos.getX() >> 4,(int) pos.getZ() >> 4, player);
                    }
                    return;
                }

                for (double x = pos.getX()-(16*maxChunks); x < pos.getX()+(16*maxChunks); x = x+16){
                    for (double z = pos.getZ()-(16*maxChunks); z < pos.getZ()+(16*maxChunks); z = z+16){
                        for (Player player : players){
                            player.getLevel().requestChunk((int) x >> 4,(int) z >> 4, player);
                        }
                    }
                }
            }
        }, interval);
    }

    public void block(Player player, String victim, String blockString){
        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player, configFile.getString("permission-block"),
                "§cYou dont have permission to block player!")) return;

        Block block = null;
        Item item = Item.fromString(blockString);
        if (item != null) block = item.getBlock();

        if (block == null){
            String message = configFile.getString("blockNotFound");
            message = message.replace("{id}", blockString);
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        this.blocksBefore.put(pvictim.getName(), setBlocksArround(pvictim, block));

        String message = configFile.getString("blockMessage");
        message = message.replace("{victim}", pvictim.getName());
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void unblock(Player player, String victim){
        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player, configFile.getString("permission-block"),
                "§cYou dont have permission to block player!")) return;

        List<Block> blocksBefore = this.blocksBefore.get(pvictim.getName());
        if (blocksBefore != null){
            replaceBlocks(blocksBefore);
        }else {
            Block block = Block.get(Block.AIR);
            setBlocksArround(pvictim, block);
        }

        String message = configFile.getString("unblockMessage");
        message = message.replace("{victim}", pvictim.getName());
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void anvil(Player player, String victim){
        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player, configFile.getString("permission-troll"),
                "§cYou dont have basic troll permission!")) return;

        pvictim.getLevel().setBlock(pvictim.add(0, 4), Block.get(Block.ANVIL), true, true);

        String message = configFile.getString("anvilMessage");
        message = message.replace("{victim}", pvictim.getName());
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void chat(Player player, String victim, String message){
        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player, configFile.getString("permission-troll-advanced"),
                "§cYou dont have basic troll permission!")) return;

        if (message.startsWith("/")){
            plugin.getServer().dispatchCommand(pvictim, message.substring(1));
        }else {
            pvictim.chat(message);
        }
        String pmessage = configFile.getString("chatMessage");
        pmessage = pmessage.replace("{victim}", pvictim.getName());
        pmessage = pmessage.replace("{player}", player.getName());
        player.sendMessage(pmessage);
    }

    public void rainbowFloor(Player player, String victim){
        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player, configFile.getString("permission-troll-advanced"),
                "§cYou dont have basic troll permission!")) return;

        List<UpdateBlockPacket> packets = generateBlockUpdate(pvictim, BLOCK_UPDATE_RANDOM_FLAT);
        if (packets == null) return;

        /* Send fake blocks*/
        plugin.getServer().batchPackets(new Player[]{pvictim, player}, packets.toArray(new DataPacket[0]));

        /* Schedule reload to real blocks*/
        sendRealChunks(pvictim, 20*60, 1, new Player[]{pvictim, player});

        String message = configFile.getString("rainbowFloorMessage");
        message = message.replace("{victim}", pvictim.getName());
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void fakeLavaHole(Player player, String victim){
        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player, configFile.getString("permission-troll"),
                "§cYou dont have basic troll permission!")) return;

        List<UpdateBlockPacket> packets = generateBlockUpdate(pvictim, BLOCK_UPDATE_HOLE_WITH_LAVA);
        if (packets == null) return;

        player.getLevel().addParticle(new HugeExplodeParticle(pvictim.clone()));
        player.getLevel().addSound(pvictim.clone(), Sound.RANDOM_EXPLODE);
        plugin.getServer().batchPackets(new Player[]{pvictim, player}, packets.toArray(new DataPacket[0]));

        /* Schedule reload to real blocks*/
        sendRealChunks(pvictim, 20*45, 1, new Player[]{pvictim, player});

        String message = configFile.getString("fakeLavaMessage");
        message = message.replace("{victim}", pvictim.getName());
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public void shopPlayerInv(Player player, String victim){
        Player pvictim = plugin.getServer().getPlayer(victim);
        if (!checkForPlayer(pvictim, player, configFile.getString("permission-invsee"),
                "§cYou dont have inv-see permission!")) return;

        FakeInventory inv = FakeInventoryManager.createInventory(player, pvictim.getName()+"'s Inventory", pvictim.getInventory().getContents(), FakeInventoryManager.INV_DOUBLE);
        inv.setInventoryFlag(FakeInventory.Flags.IS_LOCKED, true);
        inv.setInventoryFlag(FakeInventory.Flags.IS_INV_SEE, true);
        inv.showInventory(player);
    }

    public boolean checkForPlayer(Player player, Player executor, String permission, String permissionMessage){
        if (executor != null && !executor.hasPermission(permission)){
            executor.sendMessage(permissionMessage);
            return false;
        }

        if (player == null){
            if (executor != null){
                String message = configFile.getString("playerNotFound");
                executor.sendMessage(message);
            }
            return false;
        }
        return true;
    }
}
