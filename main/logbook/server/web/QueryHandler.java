/**
 * 
 */
package logbook.server.web;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logbook.data.context.Counter;
import logbook.data.context.GlobalContext;
import logbook.dto.BasicInfoDto;
import logbook.dto.BattleExDto;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.ItemDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.KdockDto;
import logbook.dto.MapCellDto;
import logbook.dto.MaterialDto;
import logbook.dto.NdockDto;
import logbook.dto.PracticeUserDetailDto;
import logbook.dto.PracticeUserDto;
import logbook.dto.QuestDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.dto.ShipParameters;
import logbook.gui.logic.ShipOrder;
import logbook.internal.CondTiming;
import logbook.internal.Item;
import logbook.internal.MasterData;
import logbook.internal.MasterData.MapAreaDto;
import logbook.internal.MasterData.MapInfoDto;
import logbook.internal.MasterData.MapState;
import logbook.internal.MasterData.MissionDto;
import logbook.internal.MasterData.ShipTypeDto;
import logbook.internal.RemodelItemData;
import logbook.internal.RemodelItemData.RemodelItem;
import logbook.internal.RemodelItemData.RemodelItemList;
import logbook.internal.RemodelItemData.RemodelRequirement;

import org.eclipse.swt.widgets.Display;

/**
 * @author Nekopanda
 *
 */
public class QueryHandler extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -2833563128459893536L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //
        //String qid_str = req.getParameter("qid");
        //System.out.println("qid=" + qid_str);

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        String requestURI = req.getRequestURI();
        if (requestURI.endsWith("query")) {
            writeResponse(resp, createQueryRespons());
        }
        else if (requestURI.endsWith("battle")) { // battle
            writeResponse(resp, createBattleRespons());
        }
        else if (requestURI.endsWith("remodeldb")) { // remodeldb
            writeResponse(resp, createRemodelDBResponse());
        }
        else if (requestURI.endsWith("master")) { // master
            writeResponse(resp, createMasterResponse());
        }
        else { // counter
            writeResponse(resp, getCounterResponse());
        }
    }

    private static void writeResponse(HttpServletResponse resp, String data) throws IOException {
        resp.getOutputStream().write(data.getBytes(Charset.forName("UTF-8")));
    }

    private static void shipInfoToJson(JsonBuilder jb, ShipInfoDto ship) {
        jb.beginArray("powup");
        int[] powup = ship.getPowup();
        for (int i = 0; i < 4; ++i) {
            jb.addValue(powup != null ? powup[i] : 0);
        }
        jb.endArray();
        jb.beginArray("maxeq");
        int[] maxeq = ship.getMaxeq();
        for (int i = 0; i < 5; ++i) {
            jb.addValue(maxeq != null ? maxeq[i] : 0);
        }
        jb.endArray();
        jb
                .add("ship_id", ship.getShipId())
                .add("ship_type", ship.getStype())
                .add("name", ship.getName())
                .add("afterlv", ship.getAfterlv())
                .add("aftershipid", ship.getAftershipid());
    }

    private static void itemInfoToJson(JsonBuilder jb, ItemInfoDto item) {
        jb
                .add("id", item.getId())
                .add("name", item.getName())
                .add("type", item.getType2());
    }

    private static void mapAreaToJson(JsonBuilder jb, MapAreaDto item) {
        jb
                .add("id", item.getId())
                .add("name", item.getName());
    }

    private static void mapInfoToJson(JsonBuilder jb, MapInfoDto item, MapState state) {
        jb
                .add("id", item.getId())
                .add("maparea_id", item.getMaparea_id())
                .add("name", item.getName())
                .add("required_defeat_count", item.getRequiredDefeatCount())
                .add("state", (state == null) ? -1 : state.getCleared())
                .add("defeat_count", (state == null) ? 0 : state.getDefeatCount())
                .add("max_hp", (state == null) ? 0 : state.getMaxhp())
                .add("now_hp", (state == null) ? 0 : state.getNowhp())
                .add("no", item.getNo());
    }

    private static void missionToJson(JsonBuilder jb, MissionDto item, Integer state) {
        jb
                .add("id", item.getId())
                .add("name", item.getName())
                .add("maparea_id", item.getMapareaId())
                .add("state", (state == null) ? -1 : state)
                .add("time", item.getTime());
    }

    private static void shipTypeToJson(JsonBuilder jb, ShipTypeDto item) {
        jb.beginArray("equip_type");
        for (Boolean item_number : item.getEquipType()) {
            jb.addValue(item_number);
        }
        jb.endArray();
        jb
                .add("id", item.getId())
                .add("name", item.getName());
    }

    private static String createMasterResponse() {
        final JsonBuilder jb = new JsonBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                jb.beginJson();
                { // 艦
                    jb.beginArray("master_ships");
                    for (ShipInfoDto ship : MasterData.getMaster().getShips().values()) {
                        jb.beginObject();
                        shipInfoToJson(jb, ship);
                        jb.endObject();
                    }
                    jb.endArray();
                }

                { // 装備
                    jb.beginArray("master_items");
                    for (int itemid : Item.keySet()) {
                        jb.beginObject();
                        itemInfoToJson(jb, Item.get(itemid));
                        jb.endObject();
                    }
                    jb.endArray();
                }

                MasterData data = MasterData.get();

                { // マップ
                    jb.beginArray("master_maparea");
                    for (MapAreaDto dto : data.getStart2().getMaparea()) {
                        jb.beginObject();
                        mapAreaToJson(jb, dto);
                        jb.endObject();
                    }
                    jb.endArray();
                }

                { // マップ
                    jb.beginArray("master_mapinfo");
                    Map<Integer, MasterData.MapState> mapState = data.getMapState2();
                    for (MapInfoDto dto : data.getStart2().getMapinfo().values()) {
                        jb.beginObject();
                        mapInfoToJson(jb, dto, mapState.get(dto.getId()));
                        jb.endObject();
                    }
                    jb.endArray();
                }

                { // 遠征
                    jb.beginArray("master_mission");
                    Map<Integer, Integer> missionState = data.getMissionState();
                    for (MissionDto dto : data.getStart2().getMission().values()) {
                        jb.beginObject();
                        missionToJson(jb, dto, missionState.get(dto.getId()));
                        jb.endObject();
                    }
                    jb.endArray();
                }

                { // 艦種
                    jb.beginArray("master_stype");
                    for (ShipTypeDto dto : data.getStart2().getStype()) {
                        jb.beginObject();
                        shipTypeToJson(jb, dto);
                        jb.endObject();
                    }
                    jb.endArray();
                }

                jb.add("last_update_time", data.getLastUpdateTime().getTime());
                jb.endJson();
            }
        });
        return jb.build();
    }

    private static void shipToJson(JsonBuilder jb, ShipDto ship, CondTiming condTiming, Date ndockCompleteTime) {
        jb.beginArray("slot_item");
        for (int item_number : ship.getItemId()) {
            jb.addValue(item_number);
        }
        jb.endArray();
        jb.beginArray("on_slot");
        for (int item_number : ship.getOnSlot()) {
            jb.addValue(item_number);
        }
        jb.endArray();

        // 成長の余地 = (装備なしのMAX) + (装備による上昇分) - (装備込の現在値)
        ShipParameters space = new ShipParameters();
        space.add(ship.getMax());
        space.add(ship.getSlotParam());
        space.subtract(ship.getParam());

        Date condClearTime = ship.getCondClearTime(condTiming, ndockCompleteTime);

        jb.beginArray("status")
                .addValue(ship.getKaryoku()) // 火力
                .addValue(ship.getRaisou()) // 雷装
                .addValue(ship.getTaiku()) // 対空
                .addValue(ship.getSoukou()) // 装甲
                .addValue(ship.getLucky()) // 運;
                .endArray();
        jb.beginArray("status_space")
                .addValue(space.getKaryoku()) // 火力
                .addValue(space.getRaisou()) // 雷装
                .addValue(space.getTaiku()) // 対空
                .addValue(space.getSoukou()) // 装甲
                .addValue(space.getLucky()) // 運
                .endArray();

        jb
                .add("id", ship.getId())
                .add("ship_id", ship.getShipId())
                .add("char_id", ship.getCharId())
                .add("ship_type", ship.getShipInfo().getStype())
                .add("level", ship.getLv())
                .add("cond", ship.getEstimatedCond(condTiming))
                .add("cond_clear_time", (condClearTime == null) ? -1 : condClearTime.getTime())
                .add("bull", ship.getBull())
                .add("bull_max", ship.getBullMax())
                .add("fuel", ship.getFuel())
                .add("fuel_max", ship.getFuelMax())
                .add("now_hp", ship.getNowhp())
                .add("max_hp", ship.getMaxhp())
                .add("locked", ship.getLocked())
                .add("dock_time", ship.getDocktime())
                .add("slot_num", ship.getSlotNum())
                .add("name", ship.getName())
                .add("slotex", ship.getSlotEx());
    }

    private static void itemToJson(JsonBuilder jb, int id, ItemDto item) {
        jb
                .add("id", id)
                .add("locked", item.isLocked())
                .add("level", item.getLevel())
                .add("alv", item.getAlv())
                .add("item_id", item.getSlotitemId());
    }

    private static void questToJson(JsonBuilder jb, QuestDto item) {
        jb
                .add("no", item.getNo())
                .add("page", item.getPage())
                .add("pos", item.getPos())
                .add("title", item.getTitle())
                .add("state", item.getState());
    }

    private static void practiceEnemyShipToJson(JsonBuilder jb, ShipInfoDto info, int level) {
        jb
                .add("ship_id", info.getShipId())
                .add("level", level);
    }

    private static String createQueryRespons() {
        final JsonBuilder jb = new JsonBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                jb.beginJson();
                { // 資源量を配列で追加
                    MaterialDto dto = GlobalContext.getMaterial();
                    if (dto != null) {
                        jb.beginArray("materials")
                                .addValue(dto.getFuel()) // 燃料
                                .addValue(dto.getAmmo()) // 弾薬
                                .addValue(dto.getMetal()) // 鋼材
                                .addValue(dto.getBauxite()) // ボーキ
                                .addValue(dto.getBurner()) // 高速建造材
                                .addValue(dto.getBucket()) // 高速修理材
                                .addValue(dto.getResearch()) // 開発資源
                                .addValue(dto.getScrew()) // 改修資材
                                .endArray();
                    }
                }

                { // 艦娘リストを配列で追加
                    jb.beginArray("ships");
                    CondTiming condTiming = GlobalContext.getCondTiming();
                    Map<Integer, Date> ndockMap = GlobalContext.getNDockCompleteTimeMap();
                    for (ShipOrder ship : ShipOrder.getOrderedShipList()) {
                        jb.beginObject();
                        shipToJson(jb, ship.ship, condTiming, ndockMap.get(ship.ship.getId()));
                        jb.beginArray("sort_number");
                        for (int sort_number : ship.sortNumber) {
                            jb.addValue(sort_number);
                        }
                        jb.endArray();
                        jb.endObject();
                    }
                    jb.endArray();
                }

                {// 艦隊情報を配列で追加
                    jb.beginArray("dock");
                    for (int i = 0; i < 4; i++) {
                        DockDto dock = GlobalContext.getDock(Integer.toString(i + 1));
                        if (dock != null) {
                            jb.beginObject();
                            jb.beginArray("ships");
                            for (ShipDto ship : dock.getShips()) {
                                jb.addValue(ship.getId());
                            }
                            jb.endArray();
                            jb.add("name", dock.getName());
                            jb.endObject();
                        }
                    }
                    jb.endArray();
                }

                { // 入渠ドック情報
                    jb.beginArray("ndock");
                    for (NdockDto ndock : GlobalContext.getNdocks()) {
                        jb.beginObject();
                        if (ndock.getNdockid() != 0) {
                            jb.add("ship_id", ndock.getNdockid());
                            jb.add("comp_time", ndock.getNdocktime().getTime());
                        }
                        else {
                            jb.add("ship_id", -1);
                            jb.add("comp_time", 0);
                        }
                        jb.endObject();
                    }
                    jb.endArray();
                }

                { // 建造ドック情報
                    jb.beginArray("kdock");
                    for (KdockDto kdock : GlobalContext.getKdocks()) {
                        jb.beginObject()
                                .add("now_using", kdock.getNowUsing())
                                .add("comp_time", kdock.getKdocktime() != null ? kdock.getKdocktime().getTime() : 0)
                                .endObject();
                    }
                    jb.endArray();
                }

                { // 遠征情報
                    jb.beginArray("mission");
                    for (DeckMissionDto mission : GlobalContext.getDeckMissions()) {
                        jb.beginArray();
                        Date comp_time = mission.getTime();
                        if (comp_time != null) {
                            jb.addValue(mission.getMissionId());
                            jb.addValue(comp_time.getTime());
                        }
                        else {
                            jb.addValue(-1);
                            jb.addValue(0);
                        }
                        jb.endArray();
                    }
                    jb.endArray();
                }

                { // 遠征情報v2
                    jb.beginArray("missionv2");
                    for (DeckMissionDto mission : GlobalContext.getDeckMissions()) {
                        boolean support = false;
                        MissionDto master = MasterData.getMaster().getMission().get(mission.getMissionId());
                        if (master != null) {
                            if (master.getName().equals("艦隊決戦支援任務") ||
                                    master.getName().equals("前衛支援任務")) {
                                support = true;
                            }
                        }

                        jb.beginObject();
                        Date comp_time = mission.getTime();
                        if (comp_time != null) {
                            jb.add("id", mission.getMissionId());
                            jb.add("comp_time", comp_time.getTime());
                            jb.add("support", support);
                        }
                        else {
                            jb.add("id", -1);
                            jb.add("comp_time", 0);
                            jb.add("support", false);
                        }
                        jb.endObject();
                    }
                    jb.endArray();
                }

                { // 装備
                    jb.beginArray("items");
                    Map<Integer, ItemDto> itemMap = GlobalContext.getItemMap();
                    for (int id : itemMap.keySet()) {
                        jb.beginObject();
                        itemToJson(jb, id, itemMap.get(id));
                        jb.endObject();
                    }
                    jb.endArray();
                }

                { // 装備順
                    jb.beginArray("unsetslot");
                    for (Entry<Integer, List<Integer>> entry : GlobalContext.getUnsetSlots().entrySet()) {
                        jb.beginObject();
                        jb.add("type", entry.getKey());
                        jb.beginArray("id");
                        for (int id : entry.getValue()) {
                            jb.addValue(id);
                        }
                        jb.endArray();
                        jb.endObject();
                    }
                    jb.endArray();
                }

                { // クエスト
                    jb.beginArray("quest");
                    for (QuestDto quest : GlobalContext.getQuest()) {
                        if (quest == null)
                            continue;
                        jb.beginObject();
                        questToJson(jb, quest);
                        jb.endObject();
                    }
                    jb.endArray();
                    jb.add("num_quest", GlobalContext.getQuest().size());
                    Date updateTime = GlobalContext.getQuestLastUpdate();
                    jb.add("quest_last_update_time", (updateTime != null) ? updateTime.getTime() : 0);
                }

                { // 出撃
                    jb.beginArray("sortie");
                    for (boolean mission : GlobalContext.getIsSortie()) {
                        jb.addValue(mission);
                    }
                    jb.endArray();
                }

                { // 演習相手
                    jb.beginArray("practice");
                    for (PracticeUserDto dto : GlobalContext.getPracticeUser()) {
                        if (dto != null) {
                            jb.beginObject()
                                    .add("id", dto.getId())
                                    .add("name", dto.getName())
                                    .add("state", dto.getState());

                            if (dto instanceof PracticeUserDetailDto) {
                                PracticeUserDetailDto detailDto = (PracticeUserDetailDto) dto;
                                List<ShipInfoDto> shipList = detailDto.getShips();
                                int[] levelList = detailDto.getShipsLevel();
                                jb.add("last_update_time", detailDto.getLastUpdate().getTime());
                                jb.beginArray("ships");
                                for (int i = 0; i < shipList.size(); ++i) {
                                    jb.beginObject();
                                    practiceEnemyShipToJson(jb, shipList.get(i), levelList[i]);
                                    jb.endObject();
                                }
                                jb.endArray();
                            }

                            jb.endObject();
                        }
                    }
                    jb.endArray();

                    Date updateTime = GlobalContext.getPracticeUserLastUpdate();
                    jb.add("practice_last_update_time", (updateTime != null) ? updateTime.getTime() : 0);
                }

                { // 出撃数, 遠征数
                    BasicInfoDto basicDto = GlobalContext.getBasicInfo();
                    if (basicDto != null) {
                        jb.beginObject("basic");
                        jb
                                .add("nickname", basicDto.getNickname())
                                .add("level", GlobalContext.hqLevel())
                                .add("deck_count", basicDto.getDeckCount())
                                .add("kdock_count", basicDto.getKdockCount())
                                .add("ndock_count", basicDto.getNdockCount())
                                .add("ms_count", basicDto.getMissionCount())
                                .add("ms_success", basicDto.getMissionSuccess())
                                .add("pt_win", basicDto.getPracticeWin())
                                .add("pt_lose", basicDto.getPracticeLose())
                                .add("st_win", basicDto.getSortieWin())
                                .add("st_lose", basicDto.getSortieLose());
                        jb.endObject();
                    }
                }

                { // カウンタ
                    Counter counter = GlobalContext.getCounter();
                    jb.beginObject("counter");
                    jb
                            .add("kenzo", counter.getKenzo())
                            .add("kaihatsu", counter.getKaihatsu())
                            .add("kaitai", counter.getKaitai())
                            .add("haiki", counter.getHaiki())
                            .add("kyoka", counter.getKyoka())
                            .add("remodelitem", counter.getRemodelitem())
                            .add("nyukyo", counter.getNyukyo())
                            .add("hokyu", counter.getHokyu());
                    jb.endObject();
                }

                {
                    jb.add("max_ships", GlobalContext.maxChara());
                    jb.add("max_slotitems", GlobalContext.maxSlotitem());
                    jb.add("is_combined", GlobalContext.isCombined());
                    jb.add("combine_flag", GlobalContext.getCombineFlag());
                    jb.add("parallel_quest_count", GlobalContext.getParallelQuestCount());
                    jb.add("master_last_update_time", MasterData.get().getLastUpdateTime().getTime());
                    jb.add("port_last_update_time", GlobalContext.getLastPortUpdate().getTime());
                    jb.add("remodeldb_last_update_time", RemodelItemData.getInstance().getLastUpdateTime().getTime());
                    jb.add("now_time", new Date().getTime());
                    jb.add("server_time_diff", GlobalContext.getServerTimeDiff());
                }
                jb.endJson();
            }
        });

        return jb.build();
    }

    private static String createBattleRespons() {
        final JsonBuilder jb = new JsonBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                jb.beginJson();
                BattleExDto battleDto = GlobalContext.getLastBattleDto();
                MapCellDto map = GlobalContext.getSortieMap();

                if (map != null) {
                    {// 戦闘中のマップ
                        jb.beginArray("map");
                        jb.addValue(map.getMap()[0]);
                        jb.addValue(map.getMap()[1]);
                        jb.addValue(map.getMap()[2]);
                        jb.endArray();
                        jb.add("enemy_id", map.getEnemyId());
                        jb.add("is_boss", map.isBoss());

                        // ルート選択
                        int[] cells = map.getSelectRoute();
                        if (cells != null) {
                            jb.beginArray("select_route");
                            for (int cell : cells) {
                                jb.addValue(cell);
                            }
                            jb.endArray();
                        }
                    }
                }
                // 演習もあるので
                if (battleDto != null) {// HP
                    List<ShipDto> fships = battleDto.getDock().getShips();
                    boolean[] escaped = battleDto.getDock().getEscaped();
                    boolean[] dmgctrl = battleDto.getDmgctrl();
                    CondTiming condTiming = GlobalContext.getCondTiming();
                    Map<Integer, Date> ndockMap = GlobalContext.getNDockCompleteTimeMap();

                    jb.beginArray("friend");
                    for (int i = 0; i < fships.size(); ++i) {
                        ShipDto ship = fships.get(i);
                        jb.beginObject();
                        shipToJson(jb, ship, condTiming, ndockMap.get(ship.getId()));
                        jb.add("escaped", (escaped != null) ? escaped[i] : false);
                        jb.add("use_dmgctrl", dmgctrl[i]);
                        jb.endObject();
                    }
                    jb.endArray();

                    List<EnemyShipDto> eships = battleDto.getEnemy();
                    int[] enowhp = battleDto.getNowEnemyHp();
                    int[] emaxhp = battleDto.getMaxEnemyHp();
                    jb.beginArray("enemy");
                    for (int i = 0; i < eships.size(); ++i) {
                        EnemyShipDto ship = eships.get(i);
                        String flagship = ship.getShipInfo().getFlagship();
                        int level = (flagship.equals("flagship") ? 2
                                : flagship.equals("elite") ? 1
                                        : 0);
                        jb.beginObject();
                        jb
                                .add("ship_id", ship.getShipId())
                                .add("ship_type", ship.getStype())
                                .add("level", level)
                                .add("now_hp", enowhp[i])
                                .add("max_hp", emaxhp[i]);
                        jb.endObject();
                    }
                    jb.endArray();

                    jb.add("rank", battleDto.getLastPhase().getEstimatedRank().rank());

                    if (battleDto.isCombined()) {
                        jb.beginArray("friend_combined");
                        List<ShipDto> fshipsCombined = battleDto.getDockCombined().getShips();
                        boolean[] escapedCombined = battleDto.getDockCombined().getEscaped();
                        boolean[] dmgctrlCombined = battleDto.getDmgctrlCombined();

                        for (int i = 0; i < fshipsCombined.size(); ++i) {
                            ShipDto ship = fshipsCombined.get(i);
                            jb.beginObject();
                            shipToJson(jb, ship, condTiming, ndockMap.get(ship.getId()));
                            jb.add("escaped", (escapedCombined != null) ? escapedCombined[i] : false);
                            jb.add("use_dmgctrl", dmgctrlCombined[i]);
                            jb.endObject();
                        }
                        jb.endArray();

                        int[] escapeInfo = battleDto.getEscapeInfo();
                        if (escapeInfo != null) {
                            jb.beginArray("escape_info");
                            for (int id : escapeInfo) {
                                jb.addValue(id);
                            }
                            jb.endArray();
                        }
                    }

                    boolean hasNextNight = false;
                    switch (battleDto.getLastPhase().getKind()) {
                    case BATTLE:
                    case PRACTICE_BATTLE:
                    case COMBINED_BATTLE:
                    case COMBINED_BATTLE_WATER:
                        hasNextNight = true;
                        break;
                    default:
                        break;
                    }
                    jb.add("has_next_night", hasNextNight);
                }
                jb.endJson();
            }
        });

        return jb.build();
    }

    public static String getCounterResponse() {
        final JsonBuilder jb = new JsonBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                jb.beginJson();
                jb.add("update", GlobalContext.getUpdateCounter());
                jb.endJson();
            }
        });
        return jb.build();
    }

    public static String createRemodelDBResponse() {
        final JsonBuilder jb = new JsonBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                jb.beginJson();
                jb.beginArray("data");
                for (RemodelItemList itemList : RemodelItemData.getInstance().getData().values()) {
                    int dayOfWeek = itemList.getDayOfWeek();
                    int shipId = itemList.getShipId();
                    jb.beginObject();
                    jb.add("day_of_week", dayOfWeek);
                    jb.add("ship_id", shipId);
                    jb.beginArray("items");
                    for (int id : itemList.getItems()) {
                        jb.addValue(id);
                    }
                    jb.endArray();
                    jb.endObject();
                }
                jb.endArray();
                jb.beginArray("items");
                for (RemodelItem item : RemodelItemData.getInstance().getItems().values()) {
                    jb.beginObject();
                    jb.add("id", item.getId());
                    jb.add("slot_id", item.getSlotId());
                    jb.beginArray("materials");
                    for (int r : item.getMaterials()) {
                        jb.addValue(r);
                    }
                    jb.endArray();
                    jb.beginArray("reqs");
                    for (RemodelRequirement r : item.getReqs()) {
                        jb.beginObject();
                        if (r != null) {
                            jb.beginArray("kits");
                            for (int k : r.getKits()) {
                                jb.addValue(k);
                            }
                            jb.endArray();
                            jb.add("req_slot_id", r.getReqSlotId());
                            jb.add("req_slot_num", r.getReqSlotNum());
                        }
                        jb.endObject();
                    }
                    jb.endArray();
                    jb.endObject();
                }
                jb.endArray();
                jb.add("remodeldb_last_update_time", RemodelItemData.getInstance().getLastUpdateTime().getTime());
                jb.endJson();
            }
        });
        return jb.build();
    }
}
