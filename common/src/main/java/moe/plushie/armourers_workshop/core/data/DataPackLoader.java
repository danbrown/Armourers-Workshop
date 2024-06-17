package moe.plushie.armourers_workshop.core.data;

import moe.plushie.armourers_workshop.api.core.IResourceLocation;
import moe.plushie.armourers_workshop.api.core.IResourceManager;
import moe.plushie.armourers_workshop.api.data.IDataPackBuilder;
import moe.plushie.armourers_workshop.init.ModConstants;
import moe.plushie.armourers_workshop.init.ModLog;
import moe.plushie.armourers_workshop.utils.SkinFileUtils;
import moe.plushie.armourers_workshop.utils.StreamUtils;
import moe.plushie.armourers_workshop.utils.ext.OpenResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class DataPackLoader implements PreparableReloadListener {

    protected final ArrayList<Entry> entries = new ArrayList<>();

    public void add(Entry entry) {
        this.entries.add(entry);
        this.entries.sort(Comparator.comparingInt(it -> it.order));
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    @Override
    public CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller2, Executor executor, Executor executor2) {
        var allJobs = new ArrayList<CompletableFuture<?>>();
        var allCompletes = new ArrayList<Runnable>();
        build((supplier, consumer) -> {
            var job = CompletableFuture.supplyAsync(supplier, executor);
            allJobs.add(job);
            allCompletes.add(() -> consumer.accept(job.join()));
        }, resourceManager);
        return CompletableFuture.allOf(allJobs.toArray(new CompletableFuture[0])).thenCompose(barrier::wait).thenAcceptAsync(it -> allCompletes.forEach(Runnable::run), executor2);
    }

    public void build(TaskQueue taskQueue, ResourceManager resourceManager) {
        var resourceManager1 = resourceManager.asResourceManager();
        entries.forEach(entry -> taskQueue.accept(entry.prepare(resourceManager1), entry::load));
    }

    public static class Entry {

        private final IResourceLocation target;
        private final Function<IResourceLocation, IDataPackBuilder> provider;
        private final Runnable willLoadHandler;
        private final Runnable didLoadHandler;
        private final int order;

        public Entry(String path, Function<IResourceLocation, IDataPackBuilder> provider, Runnable willLoadHandler, Runnable didLoadHandler, int order) {
            this.target = ModConstants.key(path);
            this.provider = provider;
            this.willLoadHandler = willLoadHandler;
            this.didLoadHandler = didLoadHandler;
            this.order = order;
        }

        public Supplier<Map<IResourceLocation, IDataPackBuilder>> prepare(IResourceManager resourceManager) {
            if (willLoadHandler != null) {
                willLoadHandler.run();
            }
            return () -> {
                var results = new HashMap<IResourceLocation, IDataPackBuilder>();
                resourceManager.readResources(target, s -> s.endsWith(".json"), (location, resource) -> {
                    var object = StreamUtils.fromPackObject(resource);
                    if (object == null) {
                        return;
                    }
                    var path = SkinFileUtils.removeExtension(location.getPath());
                    var location1 = OpenResourceLocation.create(location.getNamespace(), path);
                    ModLog.debug("Load entry '{}' in '{}'", location1, resource.getSource());
                    results.computeIfAbsent(location1, provider).append(object, location);
                });
                return results;
            };
        }

        public void load(Map<IResourceLocation, IDataPackBuilder> results) {
            results.forEach((key, builder) -> builder.build());
            if (didLoadHandler != null) {
                didLoadHandler.run();
            }
        }
    }

    protected interface TaskQueue {

        void accept(Supplier<Map<IResourceLocation, IDataPackBuilder>> supplier, Consumer<Map<IResourceLocation, IDataPackBuilder>> consumer);
    }
}
