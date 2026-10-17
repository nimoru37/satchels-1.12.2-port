package makamys.satchels.gui;

import java.util.List;

import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public interface TooltippedItem {

    void getTooltips(List<String> linesNormal, List<String> linesDetails, ItemTooltipEvent event);

}
