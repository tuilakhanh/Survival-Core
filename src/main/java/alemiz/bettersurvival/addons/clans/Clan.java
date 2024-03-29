package alemiz.bettersurvival.addons.clans;

import alemiz.bettersurvival.BetterSurvival;
import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.addons.myland.LandRegion;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.ConfigManager;
import alemiz.bettersurvival.utils.TextUtils;
import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Clan {

    private final PlayerClans loader;

    private final String rawName;
    private final String name;

    private Config config;

    private final String owner;
    private final List<String> players = new ArrayList<>();
    private final List<String> admins = new ArrayList<>();

    private final Map<String, Position> homes = new HashMap<>();

    private int money;

    public Clan(String rawName, String name, Config config, PlayerClans loader){
        this.rawName = rawName;
        this.name = name;
        this.owner = config.getString("owner");
        this.players.addAll(config.getStringList("players"));
        this.admins.addAll(config.getStringList("admins"));
        this.money = config.getInt("money");

        this.loader = loader;
        this.config = config;

        this.loadHomes();
    }

    public void setMoney(int value){
        this.money = value;
        config.set("money", value);
        config.save();
    }

    public boolean addMoney(int value){
        int balance = this.money+value;

        if (balance > this.config.getInt("maxMoney")) return false;
        this.setMoney(balance);
        return true;
    }

    public boolean reduceMoney(int value){
        if ((this.money - value) < 0) return false;

        this.setMoney(this.money - value);
        return true;
    }

    private void savePlayerList(){
        config.set("players", this.players);
        config.set("admins", this.admins);
        config.save();
    }

    private void saveHomes(){
        for (String homeName : this.homes.keySet()){
            Position home = this.homes.get(homeName);
            String homeString = home.getX()+","+home.getY()+","+home.getZ()+","+home.getLevel().getFolderName();

            config.set("home."+homeName, homeString);
        }
        config.save();
    }

    private void loadHomes(){
        ConfigSection section = config.getSection("home");
        for (String home : section.getKeys(false)){
            String homeString = section.getString(home);
            String[] data = homeString.split(",");

            Level level = Server.getInstance().getLevelByName(data[3]);
            if (level == null) continue;

            try {
                this.homes.put(home.toLowerCase(), new Position(Double.parseDouble(data[0]), Double.parseDouble(data[1]), Double.parseDouble(data[2]), level));
            }catch (Exception e){
                BetterSurvival.getInstance().getLogger().warning("§cUnable to load home §4"+home+"§c for clan §4"+this.rawName);
            }
        }
    }

    public boolean isMember(Player player){
        return player != null && this.isMember(player.getName());
    }

    public boolean isMember(String player){
        return this.players.contains(player) || this.owner.equalsIgnoreCase(player);
    }

    public boolean isAdmin(Player player){
        return player != null && this.isAdmin(player.getName());
    }

    public boolean isAdmin(String player){
        return this.admins.contains(player.toLowerCase());
    }

    public void addAdmin(String player, Player executor){
        if (player == null) return;

        if (executor != null && !this.owner.equals(executor.getName())){
            executor.sendMessage("§c»§7Bạn không có quyền để thức hiện!");
            return;
        }

        if (this.isAdmin(player)){
            if (executor != null) executor.sendMessage("§c»§7Người chơi đã là admin clan của bạn!");
            return;
        }

        if (executor != null && executor.getName().equalsIgnoreCase(player)){
            executor.sendMessage("§c»§7Bạn khổng thể tự thêm bản thân làm admin!");
            return;
        }

        if (this.owner.equalsIgnoreCase(player)){
            if (executor != null) executor.sendMessage("§c»§7Bạn đã là chủ clan! Bạn khổng thê làm clan admin.!");
            return;
        }

        this.admins.add(player.toLowerCase());
        this.savePlayerList();
        this.sendMessage("§2Người chơi §6@"+player+"§2 đã được chọ làm admin của clan!");
    }

    public void removeAdmin(String player, Player executor){
        if (player == null) return;

        if (executor != null && !this.owner.equals(executor.getName())){
            executor.sendMessage("§c»§7Bạn không có quyền để thực hiện!");
            return;
        }

        if (!this.isAdmin(player.toLowerCase())){
            if (executor != null) executor.sendMessage("§c»§7Người chơi không phải là admin clan của bạn!");
            return;
        }

        this.admins.remove(player.toLowerCase());
        this.savePlayerList();
        this.sendMessage("§4Người chơi §6@"+player+"§4 đã bị giáng xuống làm member clan!");
    }

    public void invitePlayer(Player player, Player executor){
        if (player == null) return;

        if (executor != null && !this.owner.equals(executor.getName()) && !this.isAdmin(executor)){
            executor.sendMessage("§c»§7Bạn không có quyền mời người chơi khác vào clan!");
            return;
        }

        if (this.players.contains(player.getName())){
            if (executor != null) executor.sendMessage("§c»§7Người chơi đã ở trong clan!");
            return;
        }

        if (this.loader.getClan(player) != null){
            if (executor != null) executor.sendMessage("§c»§7Người chơi đã có clan!");
            return;
        }

        int limit = this.config.getInt("playerLimit");
        if (this.players.size() >= limit){
            if (executor != null) executor.sendMessage("§c»§7Giới hạn clan của bạn là §6"+limit+"§7 người! " +
                    "TIP: Nâng cấp clan để được thêm slot!");
            return;
        }

        Config config = ConfigManager.getInstance().loadPlayer(player);
        List<String> pendingInvites = config.getStringList("clanInvites");

        pendingInvites.add(this.rawName);
        config.set("clanInvites", pendingInvites);
        config.save();

        player.sendMessage("§6»§7Bạn có lời mời gia nhập từ Clan §6@"+this.name+"! Sử dụng §6/clan accept <tên> §7hoặc§6 /clan deny <tên> §7để quản lý lời mời!");
        if (executor != null) executor.sendMessage("§6»§7Bạn đã mời §6@"+player.getName()+"§7 vào Clan!");
    }

    public void addPlayer(Player player){
        if (player == null) return;

        this.sendMessage("Người chơi §6@"+player.getName()+" gia nhập vào Clan!");
        this.players.add(player.getName());
        this.savePlayerList();

        player.sendMessage("§6»§7Bạn đã gia nhập Clan §6@"+this.name+"§7!");
    }

    public void kickPlayer(String playerName, Player executor){
        if (playerName == null) return;

        boolean admin = executor != null && this.isAdmin(executor);
        if (executor != null && !this.owner.equals(executor.getName()) && !admin){
            executor.sendMessage("§c»§7You do not have permission to kick player from clan!");
            return;
        }

        if (this.isAdmin(playerName) && admin){
            executor.sendMessage("§c»§7You do not have permission to kick clan admin!");
            return;
        }

        if (executor != null && executor.getName().equalsIgnoreCase(playerName)){
            executor.sendMessage("§c»§7You can not kick yourself from clan!");
            return;
        }

        if (playerName.equalsIgnoreCase(this.owner)){
            if (executor != null) executor.sendMessage("§c»§7You can not kick owner of clan!");;
            return;
        }

        if (!this.players.contains(playerName)){
            if (executor != null) executor.sendMessage("§c»§7Người chơi §6@"+playerName+" không phải là thành viên clan!");
            return;
        }

        this.sendMessage("Người chơi §6@"+playerName+" đã bị kick khỏi clan!");
        this.players.remove(playerName);
        this.savePlayerList();

        Player player = Server.getInstance().getPlayer(playerName);
        if (player != null) player.sendMessage("§c»§7Bạn đã bị kick khỏi clan §6@"+this.name+"§7!");
    }

    public void removePlayer(Player player){
        if (player == null) return;

        if (this.owner.equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7Bạn là chủ clan bạn không thể thoát clan. Sử dụng §e/clan destroy§7 để xóa clan.");
            return;
        }

        this.players.remove(player.getName());
        this.sendMessage("Người chơi @6"+player.getName()+" rời khỏi Clan!");
        this.savePlayerList();

        player.sendMessage("§6»§7Bạn đã rời khỏi clan §6@"+this.name+"§7!");
    }

    public void createBankNote(Player player, int value){
        if (player == null || value == 0) return;

        if (!this.owner.equalsIgnoreCase(player.getName()) && !this.isAdmin(player)){
            player.sendMessage("§c»§7Bạn không có quyền tạo tiền giáy của clan!");
            return;
        }

        if (Addon.getAddon("bettereconomy") == null){
            player.sendMessage("§c»§7Economy addon is not enabled!");
            return;
        }

        BetterEconomy economy = (BetterEconomy) Addon.getAddon("bettereconomy");
        economy.createNote(player, value, true);
    }

    public void applyBankNote(Player player){
        if (Addon.getAddon("bettereconomy") == null){
            player.sendMessage("§c»§7Economy addon is not enabled!");
            return;
        }

        BetterEconomy economy = (BetterEconomy) Addon.getAddon("bettereconomy");
        Item item = player.getInventory().getItemInHand();

        if (item.getId() == Item.AIR){
            player.sendMessage("§c»§r§7Bạn phải cầm tiền giấy!");
            return;
        }
        economy.applyNote(player, item, true);
    }

    public void createLand(Player player){
        if (player == null) return;

        if (!this.owner.equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7Bạn không có quyền tạo clan!");
            return;
        }

        if (Addon.getAddon("mylandprotect") == null){
            player.sendMessage("§c»§7MyLandProtect addon is not enabled!");
        }

        MyLandProtect landProtect = (MyLandProtect) Addon.getAddon("mylandprotect");
        landProtect.createLand(player, "", true);
    }

    public void removeLand(Player player){
        if (player == null) return;

        if (!this.owner.equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7Bạn không có quyền xóa clan!");
            return;
        }

        if (Addon.getAddon("mylandprotect") == null){
            player.sendMessage("§c»§7MyLandProtect addon is not enabled!");
        }

        MyLandProtect landProtect = (MyLandProtect) Addon.getAddon("mylandprotect");
        landProtect.removeClanLand(player);
    }

    public void landWhitelist(Player player, String action, String[] args){
        ClanLand land = this.getLand();
        if (land == null){
            player.sendMessage("§c»§7Clan của bạn không có vùng đất!");
            return;
        }

        if (!this.owner.equalsIgnoreCase(player.getName())){
            player.sendMessage("§c»§7Những cài đặt của clan chỉ có thể thay đổi bởi chủ clan!");
            return;
        }

        if (Addon.getAddon("mylandprotect") == null){
            player.sendMessage("§c»§7MyLandProtect addon is not enabled!");
        }

        MyLandProtect landProtect = (MyLandProtect) Addon.getAddon("mylandprotect");

        if (!action.equals("on") && !action.equals("off")){
            if (args.length < 1 && !action.equals(LandRegion.WHITELIST_LIST)){
                player.sendMessage("§c»§7Command can not be proceed! Please provide additional value to complete this command!");
                return;
            }

            landProtect.whitelist(player, String.join(" ", args), land, action);
            return;
        }

        boolean state = action.equalsIgnoreCase("on");
        land.setWhitelistEnabled(state);
        land.save();
        player.sendMessage("§a»§7Land whitelist đang ở trạng thái §6"+(state? "on" : "off")+"§7!");
    }

    public ClanLand getLand(){
        if (Addon.getAddon("mylandprotect") == null){
            return null;
        }

        MyLandProtect landProtect = (MyLandProtect) Addon.getAddon("mylandprotect");
        LandRegion land = landProtect.getLands().get(this.rawName);
        return (land instanceof ClanLand)? (ClanLand) land : null;
    }

    public void createHome(Player player, String name){
        if (player == null) return;

        if (!this.owner.equalsIgnoreCase(player.getName()) && !this.isAdmin(player)){
            player.sendMessage("§c»§7Bạn không có quyền tạo home của clan!");
            return;
        }

        int homeLimit = this.config.getInt("homeLimit", 10);
        if (this.homes.size() >= homeLimit){
            player.sendMessage("§c»§7Clan của bạn đã đạt giới hạn home là §6"+homeLimit+"§7 homes!");
            return;
        }

        if (this.homes.containsKey(name.toLowerCase())){
            player.sendMessage("§c»§7Chùng tên rùi :3!");
            return;
        }

        this.homes.put(name.toLowerCase(), player.clone());
        this.saveHomes();
        player.sendMessage("§6»§7Bạn đã tạo clan home!");
    }

    public void removeHome(Player player, String home){
        if (player == null) return;

        if (!this.owner.equals(player.getName()) && !this.isAdmin(player)){
            player.sendMessage("§c»§7Bạn không có quyền để xóa clan home!");
            return;
        }

        if (!this.homes.containsKey(home.toLowerCase())){
            player.sendMessage("§c»§7Clan home với tên §6"+home+"§7 không tồn tại!");
            return;
        }

        this.homes.remove(home.toLowerCase());
        this.saveHomes();
        player.sendMessage("§6»§7Bạn đã xóa clan home!");
    }

    public void teleportToHome(Player player, String home){
        if (player == null) return;

        if (!this.homes.containsKey(home.toLowerCase())){
            player.sendMessage("§c»§7Clan home với tên §6\"+home+\"§7 không tồn tại!");
            return;
        }

        player.teleport(this.homes.get(home.toLowerCase()));
        player.sendMessage("§6»§7Woosh! Chào mừng tới clan home §6"+home+" @"+player.getName()+"§7!");
    }

    //May be useful in feature
    public void onApplyNote(Player player, int value){
        this.onDonate(player, value);
    }

    public void onDonate(Player player, int value){
        this.sendMessage("Người chơi §6@"+player.getName()+"§fđã chuyển §e"+TextUtils.formatBigNumber(value)+"$§f vào ngân hàng của clan!");
    }

    public void chat(String message, Player player){
        if (message == null || message.isEmpty() || player == null) return;
        this.sendMessage(message, player.getName());
    }

    public void sendMessage(String message){
        this.sendMessage(message, null);
    }

    public void sendMessage(String message, String author){
        String formattedMessage = "§f[§a"+this.name+"§f] §7"+(author == null? "" : author)+": §f"+message;

        for (String playerName : this.players){
            Player member = Server.getInstance().getPlayer(playerName);

            if (member == null) continue;
            member.sendMessage(formattedMessage);
        }
    }

    public String buildTextInfo(){
        int moneyLimit = this.config.getInt("maxMoney");
        int playerLimit = this.config.getInt("playerLimit");
        int homeLimit = this.config.getInt("homeLimit", 10);

        return "§a"+this.name+"§a Clan:\n" +
                "§3»§7 CHủ: "+this.owner+"\n" +
                "§3»§7 Coins: §e"+this.money+"§7/§6"+moneyLimit+"$\n" +
                "§3»§7 Vùng đất: §e"+(this.hasLand()? "Có" : "Không")+"\n" +
                "§3»§7 Danh sách admin: §e"+(this.admins.size() == 0? "Không có ai" : String.join(", ", this.admins))+"\n" +
                "§3»§7 Thành viên: §c"+this.players.size()+"§7/§4"+playerLimit+"\n" +
                "§3»§7 Danh sách thành viên: §e"+String.join(", ", this.players)+"\n" +
                "§3»§7 Home: §a"+this.homes.size()+"§7/§2"+homeLimit+"\n" +
                "§3»§7 Danh sách home: §e"+(this.homes.size() == 0? "None" : String.join(", ", this.homes.keySet()));
    }

    public String getRawName() {
        return this.rawName;
    }

    public String getName() {
        return this.name;
    }

    public String getOwner() {
        return this.owner;
    }

    public List<String> getPlayers() {
        return this.players;
    }

    public int getMoney() {
        return this.money;
    }

    public int getMaxMoney(){
        return this.config.getInt("maxMoney");
    }

    public Config getConfig(){
        return config;
    }

    public boolean hasLand(){
        return this.config.exists("land");
    }

    public Map<String, Position> getHomes() {
        return this.homes;
    }
}
