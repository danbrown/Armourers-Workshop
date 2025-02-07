package moe.plushie.armourers_workshop.init;

import moe.plushie.armourers_workshop.api.common.IEntityTypeProvider;
import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.api.data.IDataPackBuilder;
import moe.plushie.armourers_workshop.core.armature.ArmatureTransformerManager;
import moe.plushie.armourers_workshop.core.data.DataPackType;
import moe.plushie.armourers_workshop.core.data.slot.SkinSlotType;
import moe.plushie.armourers_workshop.core.entity.EntityProfile;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IODataObject;
import moe.plushie.armourers_workshop.init.platform.DataPackManager;
import moe.plushie.armourers_workshop.utils.SkinFileUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class ModEntityProfiles {

    private static final ArrayList<BiConsumer<IEntityTypeProvider<?>, EntityProfile>> INSERT_HANDLERS = new ArrayList<>();
    private static final ArrayList<BiConsumer<IEntityTypeProvider<?>, EntityProfile>> REMOVE_HANDLERS = new ArrayList<>();

    private static final HashMap<IResourceLocation, EntityProfile> PENDING_ENTITY_PROFILES = new HashMap<>();
    private static final HashMap<IEntityTypeProvider<?>, EntityProfile> PENDING_ENTITIES = new HashMap<>();

    private static final HashMap<IResourceLocation, EntityProfile> ALL_ENTITY_PROFILES = new HashMap<>();
    private static final HashMap<IEntityTypeProvider<?>, EntityProfile> ALL_ENTITIES = new HashMap<>();

    private static BiConsumer<IEntityTypeProvider<?>, EntityProfile> dispatch(ArrayList<BiConsumer<IEntityTypeProvider<?>, EntityProfile>> consumers) {
        return (entityType, entityProfile) -> consumers.forEach(consumer -> consumer.accept(entityType, entityProfile));
    }

    public static void init() {
        DataPackManager.register(DataPackType.BUNDLED_DATA, "skin/profiles", SimpleLoader::new, SimpleLoader::clean, SimpleLoader::freeze, 1);
    }

    public static void forEach(BiConsumer<IEntityTypeProvider<?>, EntityProfile> consumer) {
        ALL_ENTITIES.forEach(consumer);
    }

    public static void addListener(BiConsumer<IEntityTypeProvider<?>, EntityProfile> removeHandler, BiConsumer<IEntityTypeProvider<?>, EntityProfile> insertHandler) {
        REMOVE_HANDLERS.add(removeHandler);
        INSERT_HANDLERS.add(insertHandler);
        // if it add listener after the loading, we need manual send a notification.
        ALL_ENTITIES.forEach(insertHandler);
    }

    @Nullable
    public static <T extends Entity> EntityProfile getProfile(T entity) {
        return getProfile(entity.getType());
    }

    @Nullable
    public static <T extends Entity> EntityProfile getProfile(EntityType<T> entityType) {
        return ArmatureTransformerManager.find(ALL_ENTITIES, entityType, IEntityTypeProvider::get);
    }

    @Nullable
    public static EntityProfile getProfile(IResourceLocation registryName) {
        return ALL_ENTITY_PROFILES.get(registryName);
    }

    public static class SimpleLoader implements IDataPackBuilder {

        private boolean locked = false;

        private final IResourceLocation registryName;

        private final ArrayList<IEntityTypeProvider<?>> entities = new ArrayList<>();
        private final LinkedHashMap<SkinSlotType, Function<SkinSlotType, Integer>> supports = new LinkedHashMap<>();

        public SimpleLoader(IResourceLocation registryName) {
            this.registryName = ModConstants.key(SkinFileUtils.getBaseName(registryName.getPath()));
        }

        @Override
        public void append(IODataObject object, IResourceLocation location) {
            if (object.get("replace").boolValue()) {
                locked = false;
                supports.clear();
                entities.clear();
            }
            object.get("locked").ifPresent(o -> {
                locked = o.boolValue();
            });
            object.get("slots").entrySet().forEach(it -> {
                SkinSlotType type = SkinSlotType.byName(it.getKey());
                String name = it.getValue().stringValue();
                if (type == null) {
                    return; // ignore when can't found slot type.
                }
                if (name.equals("default_mob_slots")) {
                    supports.put(type, type1 -> ModConfig.Common.prefersWardrobeMobSlots);
                } else if (name.equals("default_player_slots")) {
                    supports.put(type, type1 -> ModConfig.Common.prefersWardrobePlayerSlots);
                } else {
                    int count = it.getValue().intValue();
                    supports.put(type, type1 -> count);
                }
            });
            object.get("entities").allValues().forEach(o -> {
                entities.add(IEntityTypeProvider.of(o.stringValue()));
            });
        }

        @Override
        public void build() {
            EntityProfile profile = new EntityProfile(registryName, supports, entities, locked);
            entities.forEach(entityType -> PENDING_ENTITIES.put(entityType, profile));
            PENDING_ENTITY_PROFILES.put(registryName, profile);
        }


        private static void clean() {
        }

        private static void freeze() {
            //
            difference(ALL_ENTITY_PROFILES, PENDING_ENTITY_PROFILES, (registryName, entityProfile) -> {
                ModLog.debug("Unregistering Entity Profile '{}'", registryName);
            }, (registryName, entityProfile) -> {
                ModLog.debug("Registering Entity Profile '{}'", registryName);
            });
            difference(ALL_ENTITIES, PENDING_ENTITIES, dispatch(REMOVE_HANDLERS), dispatch(INSERT_HANDLERS));
            // apply changes
            ALL_ENTITIES.clear();
            ALL_ENTITY_PROFILES.clear();
            ALL_ENTITIES.putAll(PENDING_ENTITIES);
            ALL_ENTITY_PROFILES.putAll(PENDING_ENTITY_PROFILES);
            PENDING_ENTITIES.clear();
            PENDING_ENTITY_PROFILES.clear();
        }


        private static <K, V> void difference(Map<K, V> oldValue, Map<K, V> newValue, BiConsumer<K, V> removeHandler, BiConsumer<K, V> insertHandler) {
            var insertEntities = new HashMap<K, V>();
            var removedEntities = new HashMap<K, V>(oldValue);
            newValue.forEach((key, value) -> {
                if (removedEntities.remove(key) == null) {
                    insertEntities.put(key, value);
                }
            });
            removedEntities.forEach(removeHandler);
            insertEntities.forEach(insertHandler);
        }
    }
}
