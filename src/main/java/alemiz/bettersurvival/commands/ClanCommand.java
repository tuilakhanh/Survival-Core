package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.clans.Clan;
import alemiz.bettersurvival.addons.clans.ClanLand;
import alemiz.bettersurvival.addons.clans.PlayerClans;
import alemiz.bettersurvival.addons.myland.MyLandProtect;
import alemiz.bettersurvival.utils.Addon;
import alemiz.bettersurvival.utils.Command;
import alemiz.bettersurvival.utils.ConfigManager;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.utils.Config;
import me.onebone.economyapi.EconomyAPI;

import java.util.Arrays;
import java.util.List;

public class ClanCommand extends Command {

    public PlayerClans loader;

    public ClanCommand(String name, PlayerClans loader) {
        super(name, "Tất cả các lệnh của clan", "");

        this.usage = "§7/clan create <name> : Tạo clan mới\n" +
                "§7/clan invite <player> : Mời người chơi vào clan của ban\n" +
                "§7/clan kick <player> : Kick người chơi khỏi clan của bạn\n" +
                "§7/clan invitations : Danh sách lời mời vào clan của bạn\n" +
                "§7/clan accept <clan(tên gốc)> : Chấp nhận lời mời vào clan\n" +
                "§7/clan deny <clan(tân gốc)> : Từ chối lời mời vào clan\n" +
                "§7/clan leave : Rời khỏi clan hiện tại\n" +
                "§7/clan destroy : Xoá Clan của bạn. Tất cả mọi thứ của clan sẽ bị mất!\n" +
                "§7/clan info : Thông tin về clam của bạn\n" +
                "§7/clan admin add <player> : Thêm quyền admin cho người chơi chỉ định\n" +
                "§7/clan admin remove <player> : Xoá quyền admin của người chơi chỉ định\n" +
                "§7/clan bank note <value> : Tạo tiền giấy từ coins của ngân hàng clan\n" +
                "§7/clan bank apply : Chuyển coins từ tiền giấy vào ngân hàng của clan\n" +
                "§7/clan bank donate <value> : Chuyển tiền vào ngân hàng của clan\n" +
                "§7/clan bank status: Xem thông tin ngân hàng của clan\n" +
                "§7/clan land create: Tạo vùng đất\n" +
                "§7/clan land remove: Xoá vùng đất\n" +
                "§7/clan land access <on|off>: Cho member trong clan mở chest\n" +
                "§7/clan land whitelist <add|remove|list|on|off> <value - optional>: Cho phép 1 menber trong clan mở chest\n" +
                "§7/clan home <create|remove|home>: Lệnh clan home\n" +
                "§7/clan listhome : Danh sách các homes\n" +
                "§aBạn có thể chat với clan bằng cách chat bắt đầu bằng §6%§a!";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", CommandParamType.STRING, false),
                new CommandParameter("sub-action", CommandParamType.STRING, true)
        });
        this.loader = loader;
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (!(sender instanceof Player)){
            sender.sendMessage("§cLệnh này chỉ được sử dụng trong game!");
            return true;
        }

        Player player = (Player) sender;
        String clanName;
        String playerName;
        Clan clan;
        Config config;

        if (args.length < 1){
            player.sendMessage(this.getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "create":
                clanName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                this.loader.createClan(player, clanName);
                break;
            case "invite":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                playerName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                this.loader.invite(playerName, player);
                break;
            case "kick":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                playerName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                clan = this.loader.getClan(player);

                if (clan == null){
                    player.sendMessage("§c»§7Bạn không ở trong clan!");
                    break;
                }
                clan.kickPlayer(playerName, player);
                break;
            case "invitations":
                this.loader.sendInvitationsMessage(player);
                break;
            case "accept":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.loader.getClan(player);
                if (clan != null){
                    player.sendMessage("§c»§7Bạn đã ở trong clan! Nếu muốn chuyển clan vui lòng rời khỏi clan!");
                    break;
                }

                config = ConfigManager.getInstance().loadPlayer(player);
                List<String> pendingInvites = config.getStringList("clanInvites");

                if (!pendingInvites.remove(args[1])){
                    player.sendMessage("§c»§7Không có lời mời từ §6"+args[1]+"§7! Hãy đảm bảo rằng bạn đã nhập đúng tên clan.");
                    break;
                }

                clan = this.loader.getClans().get(args[1]);
                if (clan == null){
                    player.sendMessage("§c»§7Clan này không tồn tại!");
                    config.set("clanInvites", pendingInvites);
                    config.save();
                    break;
                }

                this.loader.clearInvitations(player);
                clan.addPlayer(player);
                break;
            case "deny":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                config = ConfigManager.getInstance().loadPlayer(player);
                List<String> invites = config.getStringList("clanInvites");

                if (!invites.remove(args[1])){
                    player.sendMessage("§c»§7Không có lời mời từ §6"+args[1]+"§7! Hãy đảm bảo rằng bạn đã nhập đúng tên clan.");
                    break;
                }

                config.set("clanInvites", invites);
                config.save();

                player.sendMessage("§6»§7Bạn đã chấp nhận lời mời từ clan §6"+args[1]+"§7!");
                break;
            case "info":
                clan = this.checkForClan(player);
                if (clan == null) break;

                player.sendMessage(clan.buildTextInfo());
                break;
            case "leave":
                clan = this.checkForClan(player);
                if (clan == null) break;

                clan.removePlayer(player);
                break;
            case "destroy":
                clan = this.checkForClan(player);
                if (clan == null) break;

                this.loader.destroyClan(player, clan);
                break;
            case "admin":
                if (args.length < 3){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.checkForClan(player);
                if (clan == null) break;

                switch (args[1]){
                    case "add":
                        clan.addAdmin(args[2], player);
                        break;
                    case "remove":
                        clan.removeAdmin(args[2], player);
                        break;
                    default:
                        player.sendMessage(this.getUsageMessage());
                        break;
                }
                break;
            case "bank":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.checkForClan(player);
                if (clan == null) break;

                switch (args[1]){
                    case "status":
                        player.sendMessage("§a"+clan.getName()+"§a Clan:\n§3»§7 Coins: §e"+clan.getMoney()+"§7/§6"+clan.getMaxMoney()+"$");
                    break;
                    case "note":
                        try {
                            clan.createBankNote(player, Integer.parseInt(args[2]));
                        }catch (Exception e){
                            player.sendMessage("§c»§7Vui lòng nhập giá trị là số!");
                        }
                        break;
                    case "apply":
                        clan.applyBankNote(player);
                        break;
                    case "donate":
                        try {
                            int value = Integer.parseInt(args[2]);
                            boolean success = (EconomyAPI.getInstance().myMoney(player) - value) >= 0;

                            if (!success){
                                player.sendMessage("§c»§7Bạn không có đủ tiền để chuyển!");
                                break;
                            }

                            if (!clan.addMoney(value)){
                                player.sendMessage("§c»§7Ngần hàng của clan đã đạt giời hạn!");
                                break;
                            }

                            EconomyAPI.getInstance().reduceMoney(player, value);
                            clan.onDonate(player, value);
                        }catch (Exception e){
                            player.sendMessage("§c»§7Vui lòng nhập giá trị là số!");
                        }
                        break;
                    default:
                        player.sendMessage(this.getUsageMessage());
                        break;
                }
                break;
            case "land":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.checkForClan(player);
                if (clan == null) break;

                switch (args[1]){
                    case "create":
                        clan.createLand(player);
                        break;
                    case "remove":
                        clan.removeLand(player);
                        break;
                    case "access":
                        if (args.length < 3){
                            player.sendMessage(this.getUsageMessage());
                            break;
                        }

                        ClanLand land = clan.getLand();
                        if (land == null){
                            player.sendMessage("§c»§7Your clan has not land!");
                            break;
                        }
                        if (!clan.getOwner().equalsIgnoreCase(player.getName())){
                            player.sendMessage("§c»§7Land settings can be configured by clan owner only!");
                            break;
                        }

                        boolean state = args[2].equalsIgnoreCase("on");
                        land.setRestriction(state);
                        land.save();
                        player.sendMessage("§a»§7Land restrictions has been turned §6"+(state? "on" : "off")+"§7!");
                        break;
                    case "whitelist":
                        if (args.length < 3){
                            player.sendMessage(this.getUsageMessage());
                            break;
                        }
                        clan.landWhitelist(player, args[2], Arrays.copyOfRange(args, 3, args.length));
                        break;
                    default:
                        player.sendMessage(this.getUsageMessage());
                        break;

                }
                break;
            case "home":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    return true;
                }

                clan = this.checkForClan(player);
                if (clan == null) break;

                switch (args[1]){
                    case "create":
                    case "add":
                        if (args.length < 3){
                            player.sendMessage(this.getUsageMessage());
                            return true;
                        }

                        clan.createHome(player, String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                        break;
                    case "remove":
                        if (args.length < 3){
                            player.sendMessage(this.getUsageMessage());
                            return true;
                        }

                        clan.removeHome(player, String.join(" ", Arrays.copyOfRange(args, 2, args.length)));
                        break;
                    default:
                        clan.teleportToHome(player, String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
                        break;
                }
                break;
            case "listhome":
                clan = this.checkForClan(player);
                if (clan == null) break;

                int homeLimit = clan.getConfig().getInt("homeLimit");
                player.sendMessage("§a"+clan.getName()+"§a Clan:\n" +
                        "§3»§7 Homes: §a"+clan.getHomes().size()+"§7/§2"+homeLimit+"\n" +
                        "§3»§7Danh sách home: §e"+String.join(", ", clan.getHomes().keySet()));
                break;
            default:
                player.sendMessage(this.getUsageMessage());
                break;
        }
        return true;
    }

    private Clan checkForClan(Player player){
        Clan clan = this.loader.getClan(player);
        if (clan == null){
            player.sendMessage("§c»§7You are not in any clan!");
            return null;
        }
        return clan;
    }
}
