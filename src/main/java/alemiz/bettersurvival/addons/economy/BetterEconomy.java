package alemiz.bettersurvival.addons.economy;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.commands.BankCommand;
import alemiz.bettersurvival.commands.TradeCommand;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.Items;
import alemiz.bettersurvival.utils.TextUtils;
import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.BlockItemFrame;
import cn.nukkit.blockentity.BlockEntityItemFrame;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.block.ItemFrameDropItemEvent;
import cn.nukkit.event.player.PlayerInteractEvent;
import cn.nukkit.event.player.PlayerQuitEvent;
import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import me.onebone.economyapi.EconomyAPI;
import cn.nukkit.utils.BlockIterator;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BetterEconomy extends Addon {

    private static final Item bankNote;

    static {
        Item item = Item.get(Item.PAPER, 0, 1);
        item.setCustomName("§r§eTiền giấy");
        item.getNamedTag().putByte("economy_note", 1);
        item.setLore("§r§5Sử dụng §d/bank apply§5 để nhận coins");
        bankNote = item;
    }

    private final Map<String, Integer> tradeCreators = new HashMap<>();
    private final List<String> traders = new ArrayList<>();

    public BetterEconomy(String path) {
        super("bettereconomy", path);
    }

    @Override
    public void postLoad() {
        this.plugin.getServer().getScheduler().scheduleRepeatingTask(plugin, () -> {
            for (Player player : this.plugin.getServer().getOnlinePlayers().values()){
                this.showItemInfo(player);
            }
        }, 20);
    }

    @Override
    public void loadConfig() {
        if (!configFile.exists("enable")) {
            configFile.set("enable", true);

            configFile.set("enableWithdraw", true);
            configFile.set("maxWithdrawAmount", 50000);

            configFile.set("noteCreateMessage", "§6»§7Bạn đã tạo tiền giấy với giá §e{money}$§7!");
            configFile.set("noteApplyMessage", "§6»§7Tiền giấy đã được chuyển vào ngân hàng của bạn. Số dư hiện tại: §e{money}$§7!");
            configFile.set("noteApplyMessageClan", "§6»§7Tiền giấy đã được chuyển vào ngân hàng của clan. Số dư hiện tại: §e{money}$§7!");
            configFile.set("failMessage", "§c»§7Bạn không có đủ tiền để tạo tiền giấy§7!");
            configFile.set("failMessageLimit", "§c»§7Giá trị cao nhất của tiền giấy là §e{limit}$§7!");

            configFile.set("tradeCreatorAdd", "§6»§7Đặt item vào ItemFrame để tạo trade shop.");
            configFile.set("requireTraderMode", "§c»§7Vui lòng bật chế độ trade trước: §6/trade <on|off>§7 Khi ở chế độ trade, bạn có thể mua item bằng cách lấy item ra khỏi ItemFrame!");
            configFile.set("tradeShopCreate", "§a»§7Khung trade item với giá §6{price}§7 đã được tạo!");
            configFile.set("tradeFailMessage", "§c»§7Bạn không có đủ tiền để mua item!");
            configFile.set("tradeBuyMessage", "§a»§7Bạn đã mua thành công §6{item}§7!");
            configFile.save();
        }
    }

    @Override
    public void registerCommands() {
        registerCommand("bank", new BankCommand("bank", this));
        registerCommand("trade", new TradeCommand("trade", this));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        Player player = event.getPlayer();
        this.tradeCreators.remove(player.getName());
        this.traders.remove(player.getName());
    }

    @EventHandler
    public void onItemFrameAdd(PlayerInteractEvent event){
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Item item = event.getItem();

        if (block.getId() != Block.ITEM_FRAME_BLOCK || !this.isTraderCreator(player)) return;
        Integer price = this.removeTraderCreator(player);

        if (price == null || price < 1){
            player.sendMessage("§c»§7Vui lòng nhập giá trade trước.");
            return;
        }

        BlockEntityItemFrame itemFrame = (BlockEntityItemFrame) player.getLevel().getBlockEntity(block);
        if (itemFrame.getItem() != null && itemFrame.getItem().getId() != Item.AIR) return;

        CompoundTag tag = itemFrame.namedTag;
        tag.putInt("trade_price", price);
        tag.putString("trade_owner", player.getName());
        tag.putInt("trade_count", item.getCount());
        if (item.hasCustomName()) tag.putString("trade_item_name", item.getCustomName());

        Item clonedItem = item.clone();
        clonedItem.setCustomName(item.getName());
        itemFrame.setItem(clonedItem);

        item.setCount(0);
        player.getInventory().setItemInHand(item);

        String message = configFile.getString("tradeShopCreate");
        message = message.replace("{player}", player.getName());
        message = message.replace("{price}", price.toString());
        player.sendMessage(message);
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemFrameDrop(ItemFrameDropItemEvent event){
        Player player = event.getPlayer();
        Item item = event.getItem();

        BlockEntityItemFrame itemFrame = event.getItemFrame();
        if (!itemFrame.namedTag.contains("trade_price") || !itemFrame.namedTag.contains("trade_owner")) return;
        event.setCancelled(true);

        CompoundTag tag = itemFrame.namedTag;
        String owner = tag.getString("trade_owner");
        int price = tag.getInt("trade_price");

        if (player.getName().equals(owner)){
            Vector3 vector = player.temporalVector.setComponents(itemFrame.x + 0.5, itemFrame.y, itemFrame.z + 0.5);
            this.dropItem(itemFrame, vector, item);
            itemFrame.setItem(new ItemBlock(Block.get(BlockID.AIR)));
            itemFrame.setItemRotation(0);
            return;
        }

        if (!this.isTrader(player)){
            String message = configFile.getString("requireTraderMode");
            message = message.replace("{player}", player.getName());
            player.sendMessage(message);
            return;
        }

        boolean success = EconomyAPI.getInstance().reduceMoney(player, price) >= 1;
        if (!success){
            String message = configFile.getString("tradeFailMessage");
            message = message.replace("{player}", player.getName());
            message = message.replace("{item}", item.getName());
            player.sendMessage(message);
            return;
        }

        Vector3 vector = player.temporalVector.setComponents(itemFrame.x + 0.5, itemFrame.y, itemFrame.z + 0.5);
        this.dropItem(itemFrame, vector, item);

        Item bankNote = this.buildNote(player.getName(), price);
        itemFrame.setItem(bankNote);
        itemFrame.setItemRotation(0);

        String message = configFile.getString("tradeBuyMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{item}", item.getName());
        player.sendMessage(message);
    }

    private void dropItem(BlockEntityItemFrame itemFrame, Vector3 source, Item item){
        CompoundTag tag = itemFrame.namedTag;

        if (tag.contains("trade_item_name")){
            item.setCustomName(tag.getString("trade_item_name"));
        }else{
            item.clearCustomName();
        }

        item.setCount(tag.getInt("trade_count"));

        tag.remove("trade_price");
        tag.remove("trade_owner");
        tag.remove("trade_count");
        tag.remove("trade_item_name");

        itemFrame.getLevel().dropItem(source, item);
    }

    public void createNote(Player player, int price){
        this.createNote(player, price, false);
    }

    public void createNote(Player player, int price, boolean clanMode){
        if (player == null) return;

        if (price < 1){
            player.sendMessage("§c»§Vui lòng nhập giá trị hợp lệ!");
            return;
        }

        int limit = configFile.getInt("maxWithdrawAmount", 50000);

        if (price > limit){
            String message = configFile.getString("failMessageLimit");
            message = message.replace("{player}", player.getName());
            message = message.replace("{limit}", TextUtils.formatBigNumber(limit));
            player.sendMessage(message);
            return;
        }

        boolean success;
        String owner;
        if (clanMode && Addon.getAddon("playerclans") != null){
            Clan clan = ((PlayerClans) Addon.getAddon("playerclans")).getClan(player);
            if (clan == null) {
                player.sendMessage("§c»§7Bạn không ở trong clan!");
                return;
            }

            owner = clan.getName();
            success = clan.reduceMoney(price);
        }else {
            success = EconomyAPI.getInstance().reduceMoney(player, price) >= 1;
            owner = player.getName();
        }

        if (success){
            Item item = this.buildNote(owner, price);
            if (!player.getInventory().isFull()){
                player.getInventory().addItem(item);
            }else {
                player.getLevel().dropItem(player, item);
            }
        }

        String message = configFile.getString(success? "noteCreateMessage" : "failMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{money}", TextUtils.formatBigNumber(price));
        player.sendMessage(message);
    }

    public void applyNote(Player player, Item item){
        this.applyNote(player, item, false);
    }

    public void applyNote(Player player, Item item, boolean clanMode){
        if (player == null || item == null) return;

        if (!item.hasCompoundTag() || item.getNamedTag().getByte("economy_note") != 1 || !item.getNamedTag().contains("economy_value")){
            player.sendMessage("§c»§7Vui lòng cầm tiền giấy trên tay!");
            return;
        }

        int value = item.getNamedTag().getInt("economy_value");
        Clan clan = null;

        if (clanMode && Addon.getAddon("playerclans") != null){
            clan = ((PlayerClans) Addon.getAddon("playerclans")).getClan(player);
            if (clan == null) {
                player.sendMessage("§c»§7Bạn không ở trong clan!");
                return;
            }

            clan.addMoney(value);
            clan.onApplyNote(player, value);
        }else {
            EconomyAPI.getInstance().addMoney(player, value);
        }

        PlayerInventory inv = player.getInventory();
        inv.clear(inv.getHeldItemIndex());

        String message = configFile.getString(clanMode? "noteApplyMessageClan" : "noteApplyMessage");
        message = message.replace("{player}", player.getName());
        message = message.replace("{money}", TextUtils.formatBigNumber((clanMode && clan != null)? clan.getMoney() : EconomyAPI.getInstance().myMoney(player)));
        player.sendMessage(message);
    }

    public Item buildNote(String owner, int price){
        Item item = getBankNote();
        item.setCustomName(item.getCustomName()+" §6"+TextUtils.formatBigNumber(price)+"$");
        item.setLore(ArrayUtils.addAll(new String[]{"§r§5Giá: "+price+"$", "§r§5Tạo bởi: §d"+owner}, item.getLore()));

        CompoundTag tag = item.getNamedTag();
        tag.putInt("economy_value", price);
        item.setNamedTag(tag);
        return item;
    }

    public void showItemInfo(Player player){
        if (player == null || !player.isConnected()) return;
        BlockFace direction = player.getDirection();
        Block itemFrameBlock = null;

        try {
            BlockIterator iterator = new BlockIterator(player.getLevel(), player, player.getDirectionVector(), 1.5, 4);
            while (iterator.hasNext()){
                Block block = iterator.next();
                if (block.getId() == Block.ITEM_FRAME_BLOCK){
                    BlockItemFrame frameBlock = (BlockItemFrame) block;

                    if (frameBlock.getFacing() == direction) {
                        itemFrameBlock = block;
                    }
                }
            }
        }catch (Exception e){
            //ignore
        }

        if (itemFrameBlock == null) return;
        BlockEntityItemFrame itemFrame = (BlockEntityItemFrame) player.getLevel().getBlockEntity(itemFrameBlock);

        Item item = itemFrame.getItem();
        if (item == null || item.getId() == Item.AIR || !itemFrame.namedTag.contains("trade_price")) return;

        CompoundTag tag = itemFrame.namedTag;
        String owner = tag.getString("trade_owner");
        int price = tag.getInt("trade_price");
        int count = tag.getInt("trade_count");

        player.sendTip("§aChủ sở hữu: §2"+owner+"\n§bSố lượng: §3"+count+"§b Giá: §e"+price+"$");
    }

    public void addTraderCreator(Player player, int value){
        if (player == null) return;

        if (value < 1){
            player.sendMessage("§c»§7Vui lòng nhập giá trước.");
            return;
        }
        this.tradeCreators.put(player.getName(), value);

        String message = configFile.getString("tradeCreatorAdd");
        message = message.replace("{player}", player.getName());
        player.sendMessage(message);
    }

    public boolean isTraderCreator(Player player){
        return player != null && this.tradeCreators.containsKey(player.getName());
    }

    public Integer removeTraderCreator(Player player){
        return player == null? null : this.tradeCreators.remove(player.getName());
    }

    public boolean isTrader(Player player){
        return player != null && this.traders.contains(player.getName());
    }

    public void addTrader(Player player){
        if (player == null) return;
        this.traders.add(player.getName());
    }

    public void removeTrader(Player player){
        if (player == null) return;
        this.traders.remove(player.getName());
    }

    public static Item getBankNote(){
        return Items.deepCopy(bankNote);
    }
}
