package moe.plushie.armourers_workshop.core.client.animation;

import moe.plushie.armourers_workshop.core.client.bake.BakedSkin;
import moe.plushie.armourers_workshop.core.client.other.BlockEntityRenderData;
import moe.plushie.armourers_workshop.core.client.other.EntityRenderData;
import moe.plushie.armourers_workshop.core.data.EntityAction;
import moe.plushie.armourers_workshop.core.data.EntityActionSet;
import moe.plushie.armourers_workshop.core.data.EntityActionTarget;
import moe.plushie.armourers_workshop.core.data.EntityActions;
import moe.plushie.armourers_workshop.core.skin.SkinDescriptor;
import moe.plushie.armourers_workshop.init.ModLog;
import moe.plushie.armourers_workshop.utils.TickUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class AnimationManager {

    private final HashMap<SkinDescriptor, Entry> entries = new HashMap<>();
    private final HashMap<AnimationController, AnimationState> states = new HashMap<>();

    private final ArrayList<Entry> triggerableEntries = new ArrayList<>();
    private final ArrayList<AnimationController> animations = new ArrayList<>();
    private final ArrayList<AnimationController> removeOnCompletion = new ArrayList<>();

    private EntityActionSet lastActionSet;

    public static AnimationManager of(Entity entity) {
        var renderData = EntityRenderData.of(entity);
        if (renderData != null) {
            return renderData.getAnimationManager();
        }
        return null;
    }

    public static AnimationManager of(BlockEntity blockEntity) {
        var renderData = BlockEntityRenderData.of(blockEntity);
        if (renderData != null) {
            return renderData.getAnimationManager();
        }
        return null;
    }

    public void load(Map<SkinDescriptor, BakedSkin> skins) {
        var oldEntries = new HashMap<>(entries);
        skins.forEach((key, skin) -> {
            oldEntries.remove(key);
            var entry = entries.computeIfAbsent(key, Entry::new);
            if (entry.isLoaded || skin == null) {
                return;
            }
            entry.isLoaded = true;
            entry.animations = skin.getAnimationControllers();
            if (entry.animations == null) {
                return;
            }
            entry.animations.forEach(animationController -> {
                // auto play
                if (animationController.isParallel()) {
                    play(animationController, TickUtils.animationTicks(), 0);
                }
            });
            entry.rebuild();
            animations.addAll(entry.animations);
        });
        oldEntries.forEach((key, entry) -> {
            entries.remove(key);
            if (entry.animations == null) {
                return;
            }
            entry.animations.forEach(this::stop);
            animations.removeAll(entry.animations);
        });
        reloadCache();
    }

    public void tick(Object source, float animationTicks) {
        // clear invalid animation.
        if (!removeOnCompletion.isEmpty()) {
            var animations = new ArrayList<>(removeOnCompletion);
            for (var animation : animations) {
                var state = states.get(animation);
                if (state != null && state.isCompleted(animationTicks)) {
                    stop(animation);
                }
            }
        }
        // play triggerable animation by the state.
        if (!triggerableEntries.isEmpty() && source instanceof Entity entity) {
            var actionSet = EntityActionSet.of(entity);
            if (actionSet != null) {
                actionSet.tick(entity);
                if (!actionSet.equals(lastActionSet)) {
                    ModLog.debug("{} => {} => {}", entity, actionSet, entity.getDeltaMovement());
                    play(actionSet, animationTicks);
                    lastActionSet = actionSet.copy();
                }
            }
        }
    }

    public void play(EntityActionSet tracker, float animationTicks) {
        // we allow each skin to perform a serial animation.
        for (var entry : triggerableEntries) {
            // If it's already locked, we won't switch.
            if (entry.isLocked && entry.selectedEntry != null) {
                continue;
            }
            var newValue = entry.findTriggerableEntry(tracker);
            var oldValue = entry.selectedEntry;
            if (oldValue != newValue) {
                if (oldValue != null) {
                    stop(oldValue.animationController);
                }
                entry.isLocked = false;
                entry.selectedEntry = newValue;
                if (newValue != null) {
                    play(newValue.animationController, animationTicks, newValue.getPlayCount());
                }
            }
        }
    }

    public void play(String name, float atTime, int playCount) {
        animations.forEach(animationController -> {
            if (!name.equals(animationController.getName())) {
                return;
            }
            if (animationController.isParallel()) {
                play(animationController, atTime, playCount);
                return;
            }
            entries.forEach((key, entry) -> {
                var entry1 = entry.findTriggerableEntry(animationController);
                if (entry1 != null && entry1 != entry.selectedEntry) {
                    if (entry.selectedEntry != null) {
                        stop(entry.selectedEntry.animationController);
                    }
                    entry.selectedEntry = entry1;
                    entry.isLocked = true;
                    play(animationController, atTime, playCount);
                }
            });
        });
    }

    public void stop(String name) {
        animations.forEach(animation -> {
            if (name.isEmpty() || name.equals(animation.getName())) {
                stop(animation);
            }
        });
    }

    public void play(AnimationController animationController, float atTime, int playCount) {
        var state = new AnimationState(animationController);
        if (playCount == 0) {
            playCount = switch (animationController.getLoop()) {
                case NONE -> 1;
                case LAST_FRAME -> 0;
                case LOOP -> -1;
            };
        }
        state.setStartTime(atTime);
        state.setPlayCount(playCount);
        states.put(animationController, state);
        ModLog.debug("start play {}", animationController);
        // automatically remove on animation completion.
        if (playCount > 0) {
            removeOnCompletion.add(animationController);
        } else {
            removeOnCompletion.remove(animationController);
        }
    }

    public void stop(AnimationController animationController) {
        var state = states.remove(animationController);
        if (state == null) {
            return;
        }
        ModLog.debug("stop play {}", animationController);
        removeOnCompletion.remove(animationController);
        entries.forEach((key, entry) -> {
            if (entry.selectedEntry != null && entry.selectedEntry.animationController == animationController) {
                entry.selectedEntry = null;
                lastActionSet = null; // mark to re calc.
            }
        });
    }

    public void mapping(String from, String to) {
        lastActionSet = null;
        entries.forEach((key, entry) -> {
            entry.addMapping(from, to);
            entry.rebuild();
        });
        reloadCache();
    }

    public AnimationState getAnimationState(AnimationController animationController) {
        return states.get(animationController);
    }

    private void reloadCache() {
        triggerableEntries.clear();
        triggerableEntries.addAll(entries.values().stream().filter(Entry::hasTriggerableAnimation).toList());
    }

    protected static class Entry {

        private final SkinDescriptor descriptor;

        private List<AnimationController> animations;
        private boolean isLoaded = false;

        private final HashMap<String, String> redirecteFrom = new HashMap<>();
        private final HashMap<String, String> redirecteTo = new HashMap<>();

        private final ArrayList<TriggerableEntry> triggerableAnimations = new ArrayList<>();

        private boolean isLocked = false;
        private TriggerableEntry selectedEntry;

        protected Entry(SkinDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        protected void addMapping(String from, String to) {
            if (from.equals(to)) {
                to = redirecteFrom.get(from);
                if (to != null) {
                    redirecteTo.remove(to);
                }
            } else {
                redirecteFrom.put(from, to);
                redirecteTo.put(to, from);
            }
        }

        protected String resolve(String name) {
            if (redirecteFrom.containsKey(name)) {
                return "disable:" + name;
            }
            return redirecteTo.getOrDefault(name, name);
        }

        protected void rebuild() {
            var newValues = new ArrayList<TriggerableEntry>();
            for (var animationController : animations) {
                if (!animationController.isParallel()) {
                    var name = animationController.getName();
                    var entry = new TriggerableEntry(resolve(name), animationController);
                    newValues.add(entry);
                }
            }
            newValues.sort(Comparator.comparingDouble(TriggerableEntry::getPriority).reversed());
            triggerableAnimations.clear();
            triggerableAnimations.addAll(newValues);
            if (selectedEntry != null) {
                selectedEntry = findTriggerableEntry(selectedEntry.animationController);
                isLoaded = false;
            }
        }

        protected TriggerableEntry findTriggerableEntry(EntityActionSet tracker) {
            for (var entry : triggerableAnimations) {
                if (entry.isIdle || entry.test(tracker)) {
                    return entry;
                }
            }
            return null;
        }

        protected TriggerableEntry findTriggerableEntry(AnimationController animationController) {
            for (var entry : triggerableAnimations) {
                if (entry.animationController == animationController) {
                    return entry;
                }
            }
            return null;
        }

        protected boolean hasTriggerableAnimation() {
            return !triggerableAnimations.isEmpty();
        }
    }

    protected static class TriggerableEntry {

        private final String name;
        private final EntityActionTarget target;
        private final boolean isIdle;

        private AnimationController animationController;

        protected TriggerableEntry(String name, AnimationController animationController) {
            this.name = name;
            this.target = EntityActions.by(name);
            this.animationController = animationController;
            this.isIdle = target.getActions().contains(EntityAction.IDLE);
        }

        protected boolean test(EntityActionSet tracker) {
            for (var action : target.getActions()) {
                if (!tracker.get(action)) {
                    return false;
                }
            }
            return true;
        }

        protected String getName() {
            return name;
        }

        protected double getPriority() {
            return target.getPriority();
        }

        protected int getPlayCount() {
            return target.getPlayCount();
        }
    }
}
