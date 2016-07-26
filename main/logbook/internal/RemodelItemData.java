/**
 * 
 */
package logbook.internal;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import logbook.constants.AppConstants;
import logbook.util.BeanUtils;

/**
 * @author Nekopanda
 *
 */
public class RemodelItemData {

    private static class Holder {
        public static RemodelItemData instance = null;
    }

    public static class RemodelRequirement {
        // buildkit, remodelkit, buildkit(certain), remodelkit(certain)
        private int[] kits;
        private int reqSlotId;
        private int reqSlotNum;

        public RemodelRequirement() {
        }

        public RemodelRequirement(JsonObject jsonItem) {
            this.kits = new int[] {
                    jsonItem.getInt("api_req_buildkit"),
                    jsonItem.getInt("api_req_remodelkit"),
                    jsonItem.getInt("api_certain_buildkit"),
                    jsonItem.getInt("api_certain_remodelkit")
            };
            this.reqSlotId = jsonItem.getInt("api_req_slot_id");
            this.reqSlotNum = jsonItem.getInt("api_req_slot_num");
        }

        @Override
        public boolean equals(Object obj) {
            if ((obj != null) && (obj instanceof RemodelRequirement)) {
                RemodelRequirement o = (RemodelRequirement) obj;
                return Arrays.equals(this.kits, o.kits) &&
                        (this.reqSlotId == o.reqSlotId) &&
                        (this.reqSlotNum == o.reqSlotNum);
            }
            return false;
        }

        /**
         * @return kit
         */
        public int[] getKits() {
            return this.kits;
        }

        /**
         * @param kit セットする kit
         */
        public void setKits(int[] kits) {
            this.kits = kits;
        }

        /**
         * @return reqSlotId
         */
        public int getReqSlotId() {
            return this.reqSlotId;
        }

        /**
         * @param reqSlotId セットする reqSlotId
         */
        public void setReqSlotId(int reqSlotId) {
            this.reqSlotId = reqSlotId;
        }

        /**
         * @return reqSlotNum
         */
        public int getReqSlotNum() {
            return this.reqSlotNum;
        }

        /**
         * @param reqSlotNum セットする reqSlotNum
         */
        public void setReqSlotNum(int reqSlotNum) {
            this.reqSlotNum = reqSlotNum;
        }
    }

    public static class RemodelItem {
        private int id;
        private int slotId;
        // fuel, bull, steel, bauxite, buildkit, remodelkit
        private int[] materials;
        private RemodelRequirement[] reqs = new RemodelRequirement[3];

        public RemodelItem() {

        }

        public RemodelItem(JsonObject jsonItem) {
            this.id = jsonItem.getInt("api_id");
            this.slotId = jsonItem.getInt("api_slot_id");
            this.materials = new int[] {
                    jsonItem.getInt("api_req_fuel"),
                    jsonItem.getInt("api_req_bull"),
                    jsonItem.getInt("api_req_steel"),
                    jsonItem.getInt("api_req_bauxite")
            };
        }

        public static int levelToIndex(int level) {
            if (level < 6) {
                return 0;
            }
            else if (level < 10) {
                return 1;
            }
            return 2;
        }

        /**
         * 戻り値: 変更があったか
         * @param jsonDetail
         * @param level
         * @return
         */
        public boolean loadRequirement(JsonObject jsonDetail, int level) {
            RemodelRequirement req = new RemodelRequirement(jsonDetail);
            int index = levelToIndex(level);
            if ((this.reqs[index] != null) && this.reqs[index].equals(req)) {
                return false;
            }
            this.reqs[index] = req;
            return true;
        }

        @Override
        public boolean equals(Object obj) {
            if ((obj != null) && (obj instanceof RemodelItem)) {
                RemodelItem o = (RemodelItem) obj;
                return (this.id == o.id) &&
                        (this.slotId == o.slotId) &&
                        Arrays.equals(this.materials, o.materials) &&
                        Arrays.deepEquals(this.reqs, o.reqs);
            }
            return false;
        }

        /**
         * @return id
         */
        public int getId() {
            return this.id;
        }

        /**
         * @param id セットする id
         */
        public void setId(int id) {
            this.id = id;
        }

        /**
         * @return slotId
         */
        public int getSlotId() {
            return this.slotId;
        }

        /**
         * @param slotId セットする slotId
         */
        public void setSlotId(int slotId) {
            this.slotId = slotId;
        }

        /**
         * @return materials
         */
        public int[] getMaterials() {
            return this.materials;
        }

        /**
         * @param materials セットする materials
         */
        public void setMaterials(int[] materials) {
            this.materials = materials;
        }

        /**
         * @return reqs
         */
        public RemodelRequirement[] getReqs() {
            return this.reqs;
        }

        /**
         * @param reqs セットする reqs
         */
        public void setReqs(RemodelRequirement[] reqs) {
            this.reqs = reqs;
        }
    }

    public static class RemodelItemList {

        private int dayOfWeek;
        private int shipId;
        private int[] items;

        public RemodelItemList() {

        }

        public RemodelItem[] load(JsonArray apidata, int shipId) {
            this.dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            this.shipId = shipId;

            RemodelItem[] ret = new RemodelItem[apidata.size()];
            this.items = new int[apidata.size()];
            for (int i = 0; i < apidata.size(); ++i) {
                ret[i] = new RemodelItem(apidata.getJsonObject(i));
                this.items[i] = ret[i].id;
            }

            return ret;
        }

        @Override
        public boolean equals(Object obj) {
            if ((obj != null) && (obj instanceof RemodelItemList)) {
                RemodelItemList o = (RemodelItemList) obj;
                return (this.dayOfWeek == o.dayOfWeek) &&
                        (this.shipId == o.shipId) &&
                        Arrays.equals(this.items, o.items);
            }
            return false;
        }

        public int getKey() {
            return (this.dayOfWeek * 10000) + this.shipId;
        }

        /**
         * @return dayOfWeek
         */
        public int getDayOfWeek() {
            return this.dayOfWeek;
        }

        /**
         * @param dayOfWeek セットする dayOfWeek
         */
        public void setDayOfWeek(int dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        /**
         * @return shipId
         */
        public int getShipId() {
            return this.shipId;
        }

        /**
         * @param shipId セットする shipId
         */
        public void setShipId(int shipId) {
            this.shipId = shipId;
        }

        /**
         * @return items
         */
        public int[] getItems() {
            return this.items;
        }

        /**
         * @param items セットする items
         */
        public void setItems(int[] items) {
            this.items = items;
        }

    }

    /**
     * 
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        if (modified) {
            BeanUtils.writeObject(AppConstants.REMODEL_DATA_FILE, Holder.instance);
            modified = false;
        }
    }

    public static final boolean INIT_COMPLETE;
    static {
        load();
        INIT_COMPLETE = true;
    }

    private static void load() {
        RemodelItemData remodelData = BeanUtils.readObject(AppConstants.REMODEL_DATA_FILE, RemodelItemData.class);
        if (remodelData != null) {
            Holder.instance = remodelData;
        }
        else {
            Holder.instance = new RemodelItemData();
        }
    }

    public RemodelItemData() {
    }

    private Map<Integer, RemodelItemList> data = new HashMap<>();
    private Map<Integer, RemodelItem> items = new HashMap<>();

    /** 最終更新日時 */
    private Date lastUpdateTime = new Date(0);

    /** 変更があったか */
    private static boolean modified = false;

    public static RemodelItemData getInstance() {
        return Holder.instance;
    }

    public static void updateRemodelItemList(JsonArray apidata, int supportShipId) {
        Holder.instance.doRemodelItemList(apidata, supportShipId);
    }

    public static void updateRemodelItemDetail(JsonObject apidata, int id, int level) {
        Holder.instance.doRemodelItemDetail(apidata, id, level);
    }

    private void doRemodelItemList(JsonArray apidata, int supportShipId) {
        RemodelItemList itemList = new RemodelItemList();
        RemodelItem[] items = itemList.load(apidata, supportShipId);
        int key = itemList.getKey();
        boolean updated = false;
        if (!this.data.containsKey(key) || !this.data.get(key).equals(itemList)) {
            this.data.put(key, itemList);
            updated = true;
        }
        for (RemodelItem item : items) {
            if (!this.items.containsKey(item.id) || !this.items.get(item.id).equals(item)) {
                this.items.put(item.id, item);
                updated = true;
            }
        }
        if (updated) {
            this.lastUpdateTime = new Date();
            modified = true;
        }
    }

    private void doRemodelItemDetail(JsonObject apidata, int id, int level) {
        RemodelItem item = this.items.get(id);
        if (item != null) {
            if (item.loadRequirement(apidata, level)) {
                this.lastUpdateTime = new Date();
                modified = true;
            }
        }
    }

    /**
     * @return data
     */
    public Map<Integer, RemodelItemList> getData() {
        return this.data;
    }

    /**
     * @param data セットする data
     */
    public void setData(Map<Integer, RemodelItemList> data) {
        this.data = data;
    }

    /**
     * @return lastUpdateTime
     */
    public Date getLastUpdateTime() {
        return this.lastUpdateTime;
    }

    /**
     * @param lastUpdateTime セットする lastUpdateTime
     */
    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * @return items
     */
    public Map<Integer, RemodelItem> getItems() {
        return this.items;
    }

    /**
     * @param items セットする items
     */
    public void setItems(Map<Integer, RemodelItem> items) {
        this.items = items;
    }
}
