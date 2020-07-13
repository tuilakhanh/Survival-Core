package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SurvivalShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;

public class ShopCommand extends Command {

    public SurvivalShop loader;

    public ShopCommand(String name, SurvivalShop loader) {
        super(name, "Dịch chuyển đến shop", "");

        this.usage = "§7/shop : Dịch chuyển đến shop";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
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

        if (args.length <= 0){
            this.loader.teleportToSpawn(player);
            player.sendMessage("§6»§7Bạn đã được dịch chuyển đến shop!");
            return true;
        }

        if (!player.hasPermission(this.loader.configFile.getString("bettersurvival.shop.manage"))){
            player.sendMessage("§c»§7Bạn không có quyền tạo shop!");
            return true;
        }

        switch (args[0]){
            case "set":
                this.loader.setShopSpawn(player);
                player.sendMessage("§6»§7Đã lưu spawn của shop!");
                break;
            case "smith":
                if (this.loader.getSmithShop() == null){
                    player.sendMessage("§c»§7Smith addon is not enabled. Please enable it in SurvivalShop config!");
                    break;
                }

                this.loader.getSmithShop().createSmith(player);
                break;
            case "clear":
                if (this.loader.getNpcRemovers().contains(player.getName())){
                    this.loader.removeRemover(player);
                    break;
                }
                this.loader.addRemover(player);
                break;
            default:
                player.sendMessage("§6Shop Commands:\n§7/shop : Teleports to shop\n"+
                        "§7/shop set : Set shop spawn\n" +
                        "§7/shop smith : Creates new smith npc\n" +
                        "§7/shop clear : Hit any shop npc to remove");
                break;
        }
        return true;
    }
}
