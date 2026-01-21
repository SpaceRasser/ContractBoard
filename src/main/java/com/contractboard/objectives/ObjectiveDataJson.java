package com.contractboard.objectives;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ObjectiveDataJson {
    private static final Gson GSON = new GsonBuilder().create();

    public static String toJson(ObjectiveType type, ObjectiveData data) {
        return GSON.toJson(data);
    }

    public static ObjectiveData fromJson(ObjectiveType type, String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        return switch (type) {
            case DELIVER_ITEMS -> GSON.fromJson(json, DeliverItemsData.class);
            case KILL_MOBS -> GSON.fromJson(json, KillMobsData.class);
            case MINE_BLOCKS -> GSON.fromJson(json, MineBlocksData.class);
            case CATCH_FISH -> GSON.fromJson(json, CatchFishData.class);
        };
    }

    public static ObjectiveData fromTemplate(ObjectiveType type, int target, java.util.List<String> types) {
        return switch (type) {
            case DELIVER_ITEMS -> new DeliverItemsData(types.getFirst(), target);
            case KILL_MOBS -> new KillMobsData(types, target);
            case MINE_BLOCKS -> new MineBlocksData(types, target);
            case CATCH_FISH -> new CatchFishData(types, target);
        };
    }
}
