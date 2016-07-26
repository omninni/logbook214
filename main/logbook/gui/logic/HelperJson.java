/**
 * 
 */
package logbook.gui.logic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.dto.DockDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.ShipDto;

/**
 * @author Nekorabbit
 * ねこヘルパー用のJSON生成ロジック
 */
public class HelperJson {

    private static DateFormat dateFormat = new SimpleDateFormat(AppConstants.DATE_FORMAT);

    private static class ItemAndSlot implements Comparable<ItemAndSlot> {
        public ItemInfoDto item;
        public int onslot;

        ItemAndSlot(ItemInfoDto item, int onslot) {
            this.item = item;
            this.onslot = onslot;
        }

        @Override
        public int compareTo(ItemAndSlot arg0) {
            return -Integer.compare(this.onslot, arg0.onslot);
        }
    }

    /**
     * 艦の搭載数を考慮して装備を生成
     * @param sb
     * @param ship
     * @param items
     */
    public static void genSlotitems(StringBuilder sb, ShipDto ship, List<ItemInfoDto> items) {
        // まずは空きを削る
        List<ItemAndSlot> sort = new ArrayList<>();
        int[] maxeq = ship.getMaxeq();
        for (int i = 0; i < items.size(); ++i) {
            ItemInfoDto item = items.get(i);
            if (item != null) {
                sort.add(new ItemAndSlot(item, maxeq[i]));
            }
        }
        // 搭載数で並べ替える
        Collections.sort(sort);
        // 生成
        sb.append("[");
        for (int c = 0; c < ship.getSlotNum(); ++c) {
            String name = (sort.size() > c) ? sort.get(c).item.getName() : "空き";
            if (c > 0) {
                sb.append(", ");
            }
            sb.append("\"").append(name).append("\"");
        }
        sb.append("]");
    }

    /**
     * 艦隊設定を生成
     * @param sb
     * @param dock
     */
    public static void genFleet(StringBuilder sb, DockDto dock) {
        List<ShipDto> ships = dock.getShips();
        sb.append("[ // ").append(dateFormat.format(new Date())).append(" 生成\n\t");
        for (int i = 0; i < ships.size(); ++i) {
            ShipDto dto = ships.get(i);
            sb.append("\t{ \"index\": " + (i + 1) + ", \"ships\": [" + dto.getId() + ", {\"slotitem\": ");
            genSlotitems(sb, dto, dto.getItem());
            sb.append("}] }, // ").append(dto.getName()).append("\n\t");
        }
        sb.append("]");
    }
}
