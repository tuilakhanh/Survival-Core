package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.shop.SmithShop;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.item.Item;

public class EnchCommand extends Command {

    public SmithShop loader;

    public EnchCommand(String name, SmithShop loader) {
        super(name, "Enchant item của bạn", "");

        this.usage = "§7/ench : Enchant item";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();

        this.setPermission(loader.getLoader().configFile.getString("enchantPermission"));
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
        Item inHand = player.getInventory().getItemInHand();
        if (inHand.getId() == Item.AIR){
            player.sendMessage("§c»§r§7Bạn phải cầm item!");
            return true;
        }

        Item item = this.loader.enchantItem(player, inHand);
        if (item != null) player.getInventory().setItemInHand(item);
        return true;
    }
}
