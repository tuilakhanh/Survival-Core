package alemiz.bettersurvival.addons.myhomes;

import alemiz.bettersurvival.utils.form.Form;
import alemiz.bettersurvival.utils.form.SimpleForm;
import cn.nukkit.Player;
import cn.nukkit.form.element.ElementButton;

import java.util.ArrayList;
import java.util.List;

public class WarpMenu extends SimpleForm {

    private final transient MyHomes loader;
    private transient List<WarpCategory> categories;

    public WarpMenu(Player player, MyHomes loader){
        super("Warp Menu", "§7Vui lòng chọn danh mục để lấy danh sách warp. Bạn củng có thể tạo warp của riêng mình!");
        this.player = player;
        this.loader = loader;
    }

    @Override
    public Form buildForm() {
        this.addButton(new ElementButton("§dThêm Warp"));

        this.categories = new ArrayList<>(this.loader.getWarpCategories().values());
        for (WarpCategory category : this.categories){
            int count = category.getWarps().size();
            this.addButton(new ElementButton("§5"+category.getFormattedName()+"\n§7Warp hiện có: §8§l"+count));
        }
        return this;
    }

    @Override
    public void handle(Player player) {
        if (player == null || this.categories.isEmpty() || this.getResponse() == null) return;

        if (this.getResponse().getClickedButton().getText().equals("§dThêm Warp")){
            new AddWarpForm(player, this.loader).buildForm().sendForm();
            return;
        }

        int index = this.getResponse().getClickedButtonId() - 1;
        WarpCategory category = this.categories.get(index);
        new WarpCategoryForm(player, category, this.loader).buildForm().sendForm();
    }
}
