package alemiz.bettersurvival.commands;

import alemiz.bettersurvival.addons.economy.BetterEconomy;
import alemiz.bettersurvival.utils.Command;
import cn.nukkit.Player;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;

public class BankCommand extends Command {

    public BetterEconomy loader;

    public BankCommand(String name, BetterEconomy loader) {
        super(name, "Quản lý ngân hàng", "");

        this.usage = "§7/bank note <price> : Tạo tiền giấy từ coins của bạn\n" +
                "§7/bank apply : Chuyển tiền giấy thành coins";
        this.setUsage(getUsageMessage());

        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("action", CommandParamType.STRING, true)
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

        if (args.length < 1){
            player.sendMessage(this.getUsageMessage());
            return true;
        }

        switch (args[0]){
            case "note":
                if (args.length < 2){
                    player.sendMessage(this.getUsageMessage());
                    break;
                }

                try {
                    int value = Integer.parseInt(args[1]);
                    this.loader.createNote(player, value);
                }catch (NumberFormatException e){
                    player.sendMessage("§c»§r§7Vui lòng nhập gia trị là số!");
                    break;
                }
                break;
            case "apply":
                Item item = player.getInventory().getItemInHand();
                if (item.getId() == Item.AIR){
                    player.sendMessage("§c»§r§7Bạn phải giữ thẻ ngân hàng của bạn trên tay");
                    break;
                }

                this.loader.applyNote(player, item);
                break;
            default:
                player.sendMessage(this.getUsageMessage());
                break;
        }
        return true;
    }
}
