package moe.plushie.armourers_workshop.core.skin.serializer.v20;

import moe.plushie.armourers_workshop.api.core.utils.IPair;
import moe.plushie.armourers_workshop.api.skin.ISkinType;
import moe.plushie.armourers_workshop.api.skin.property.ISkinProperties;
import moe.plushie.armourers_workshop.core.data.transform.SkinItemTransforms;
import moe.plushie.armourers_workshop.core.math.OpenTransform3f;
import moe.plushie.armourers_workshop.core.math.Rectangle3f;
import moe.plushie.armourers_workshop.core.skin.Skin;
import moe.plushie.armourers_workshop.core.skin.SkinMarker;
import moe.plushie.armourers_workshop.core.skin.SkinPreviewData;
import moe.plushie.armourers_workshop.core.skin.SkinTypes;
import moe.plushie.armourers_workshop.core.skin.animation.SkinAnimation;
import moe.plushie.armourers_workshop.core.skin.paint.SkinPaintData;
import moe.plushie.armourers_workshop.core.skin.part.SkinPart;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperties;
import moe.plushie.armourers_workshop.core.skin.property.SkinProperty;
import moe.plushie.armourers_workshop.core.skin.property.SkinSettings;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IInputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.io.IOutputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkAnimationData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkContext;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkGeometryData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkInputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkOutputStream;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPaintData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPaletteData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPartData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkPreviewData;
import moe.plushie.armourers_workshop.core.skin.serializer.v20.chunk.ChunkType;
import moe.plushie.armourers_workshop.core.utils.Collections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * file header:           0x534b494e (SKIN)
 * file format:           | header(4B) | version(4B) | reserved data(8B) | skin type |{ chunk }| crc32 |
 * chunk format:          |< length(4B) | name(4B) | flag(2B) >[ chunk data ]|
 * part chunk format:     |< length(4B) | name(4B) | flag(2B) | id(4B) >[ chunk data ]|
 * skin properties :      | length | PPTS | flag |[ key/value ]|
 * geometry data:         | length | CCBO | flag |< id(VB) | opt(VB) >[ geometry entry(VB) ]|
 * geometry data preview: | length | VCBO | flag |< id(VB) | transform(VB) >[ geometry sel(8B) ]|
 * animation data:        | length | ANIM | flag |[ id(VB) | duration(4B) | loop(VB) |< bone(VB) | channel(VB) >[ time(4B) | opt(VB) |[ type(1B) | value(VB) ]|]|]|
 * skin part:             | length | SKPR | flag |[ id(VB) | pid(VB) | name(VB) | type(VB) | transform(VB) |[ geometry sel(8B) ]]|{ part chunk }|
 * skin part markers:     | length | PRMK | flag |[ x(1B)/y(1B)/z(1B) | meta(1B) ]|
 * skin paint data:       | length | PADT | flag | opt(VB)/total width(VB)/total height(VB) |< width(VB) | height(VB) >[ color index ]|
 * skin security data:    | length | SSDT | flag | algorithm(3B)/signature(VB) |
 * palette data:          | length | PALE | flag | opt(VB)/reserved(VB) |< paint type(1B)/used bytes(1B) >[ palette entry(VB) ]|
 * geometry entry (v1):   | x(1B)/y(1B)/z(1B) |[ face options(1B) | color index(VB) ](1-6)]|
 * geometry entry (v2):   | origin(12B)/size(12B) | type(4b)/translate(12B)/rotation(12B)/scale(12B)/pivot(12B)/offset(12B) |[ face options(1B) | u(VB)/v(VB):first or s(VB)/t(VB):second(optional) ]|
 * geometry entry (v3):   | type(4B)/transform(4B)/vertex count(4B)/index count(4B)/reserved(52B) |[ vertex(12B)/normal(12B)/uv(VB) ]|[ index(4B) ]|
 * palette entry (v1):    | RRGGBB/AARRGGBB |
 * palette entry (v2):    | id(VB)/parent id(VB)/x(4B)/y(4B)/w(4B)/h(4B)/ani(4B)/opt(VB) | data count(4B)/raw data(nB) |
 * chunk flag:            1 encrypt, 2 gzip, 3 encrypt+gzip
 * symbol:                {n} = (length(4B) + byte[length]) * n + 0(4B)
 * #                      [data] = count(VB) + data[count]
 * #                      <header>[data] = (count(VB) + header + data[count]) * n + 0(VB)
 */
@SuppressWarnings("unused")
public class ChunkSerializers {

    public static final ChunkSerializer<Skin, Void> SKIN = register(new ChunkSerializer<Skin, Void>(ChunkType.SKIN) {

        @Override
        public Skin read(ChunkInputStream stream, Void obj) throws IOException {
            var context = stream.getContext();
            var skinType = stream.readType(SkinTypes::byName);
            return stream.readChunk(it -> {
                // we need to  check the secret key is correct first.
                var settings = it.read(SKIN_SETTINGS);
                if (settings.getSecurityData() != null && !settings.getSecurityData().equals(context.getSecurityData())) {
                    throw new IOException("Can't decrypt skin by the security key.");
                }
                var palette = it.read(SKIN_TEXTURE_DATA);
                var geometryData = it.read(SKIN_GEOMETRY_DATA, palette);
                var builder = new Skin.Builder(skinType);
                builder.properties(it.read(SKIN_PROPERTIES));
                builder.settings(settings);
                builder.paintData(it.read(SKIN_PAINT_DATA, palette));
                builder.previewData(it.read(SKIN_PREVIEW_DATA, geometryData));
                builder.parts(it.read(SKIN_PART, geometryData));
                builder.animations(it.read(SKIN_ANIMATION_DATA));
                builder.blobs(it.readBlobs());
                builder.id(geometryData.getId());
                builder.version(context.getFileVersion());
                return builder.build();
            });
        }

        @Override
        public void write(Skin skin, Void obj, ChunkOutputStream stream) throws IOException {
            var context = stream.getContext();
            stream.writeType(skin.getType());
            stream.writeChunk(it -> {
                var palette = new ChunkPaletteData();
                var geometryData = new ChunkGeometryData(skin.getId(), palette);
                it.write(SKIN_PROPERTIES, skin.getProperties());
                it.write(SKIN_SETTINGS, skin.getSettings().copyWithOptions(context.getOptions()));
                it.write(SKIN_TEXTURE_DATA, palette);
                it.write(SKIN_PAINT_DATA, skin.getPaintData(), palette);
                it.write(SKIN_GEOMETRY_DATA, geometryData, palette);
                it.write(SKIN_ANIMATION_DATA, skin.getAnimations());
                it.write(SKIN_PART, skin.getParts(), geometryData);
                // whether to enable preview data by the options.
                if (context.isEnablePreviewData()) {
                    it.write(SKIN_PREVIEW_DATA, SkinPreviewData.of(skin), geometryData);
                }
                it.writeBlobs(skin.getBlobs());
            });
        }
    });

    public static final ChunkSerializer<IPair<ISkinType, ISkinProperties>, Void> SKIN_INFO = register(new ChunkSerializer<>(ChunkType.SKIN) {

        @Override
        public IPair<ISkinType, ISkinProperties> read(ChunkInputStream stream, Void obj) throws IOException {
            var skinType = stream.readType(SkinTypes::byName);
            return stream.readChunk(it -> {
                var properties = it.read(SKIN_PROPERTIES);
                var settings = it.read(SKIN_SETTINGS);
                if (settings.getSecurityData() != null) {
                    properties = properties.copy();
                    properties.put(SkinProperty.SECURITY_DATA, settings.getSecurityData());
                }
                return IPair.of(skinType, properties);
            });
        }

        @Override
        public void write(IPair<ISkinType, ISkinProperties> info, Void obj, ChunkOutputStream stream) throws IOException {
            // we never call write method!!!
        }
    });

    public static final ChunkSerializer<List<SkinPart>, ChunkGeometryData> SKIN_PART = register(new ChunkSerializer<>(ChunkType.SKIN_PART) {

        @Override
        public List<SkinPart> read(ChunkInputStream stream, ChunkGeometryData geometryData) throws IOException {
            var partData = new ChunkPartData(geometryData);
            return partData.readFromStream(stream, (it, builder) -> {
                builder.markers(it.read(SKIN_MARKERS));
                // TODO: impl @SAGESSE
//                builder.paintData(it.read(SKIN_PAINT_DATA, palette));
//                builder.properties(it.read(SKIN_PROPERTIES));
                builder.blobs(it.readBlobs());
            });
        }

        @Override
        public void write(List<SkinPart> parts, ChunkGeometryData geometryData, ChunkOutputStream stream) throws IOException {
            var partData = new ChunkPartData(geometryData);
            partData.writeToStream(stream, parts, (it, part) -> {
                it.write(SKIN_MARKERS, part.getMarkers());
                // TODO: impl @SAGESSE
//                it.write(SKIN_PAINT_DATA, part.getPaintData(), palette);
//                it.write(SKIN_PROPERTIES, part.getProperties());
                it.writeBlobs(part.getBlobs());
            });
        }
    });

    public static final ChunkSerializer<List<SkinMarker>, Void> SKIN_MARKERS = register(new ChunkSerializer<>(ChunkType.MARKER) {

        @Override
        public List<SkinMarker> read(ChunkInputStream stream, Void obj) throws IOException {
            int size = stream.readInt();
            var markers = new ArrayList<SkinMarker>();
            for (int i = 0; i < size; ++i) {
                markers.add(new SkinMarker(stream));
            }
            return markers;
        }

        @Override
        public void write(List<SkinMarker> value, Void obj, ChunkOutputStream stream) throws IOException {
            stream.writeInt(value.size());
            for (var marker : value) {
                marker.writeToStream(stream);
            }
        }
    });


    public static final ChunkSerializer<ChunkGeometryData, ChunkPaletteData> SKIN_GEOMETRY_DATA = register(new ChunkSerializer<>(ChunkType.GEOMETRY_DATA) {

        @Override
        public ChunkGeometryData read(ChunkInputStream stream, ChunkPaletteData palette) throws IOException {
            var geometryData = new ChunkGeometryData(Skin.Builder.generateId(), palette);
            geometryData.readFromStream(stream);
            return geometryData;
        }

        @Override
        public void write(ChunkGeometryData geometryData, ChunkPaletteData palette, ChunkOutputStream stream) throws IOException {
            stream.writeVariable(geometryData);
        }
    });

    public static final ChunkSerializer<SkinPaintData, ChunkPaletteData> SKIN_PAINT_DATA = register(new ChunkSerializer<>(ChunkType.PAINT_DATA) {

        @Override
        public SkinPaintData read(ChunkInputStream stream, ChunkPaletteData palette) throws IOException {
            var chunkPaintData = new ChunkPaintData(palette);
            return chunkPaintData.readFromStream(stream);
        }

        @Override
        public void write(SkinPaintData value, ChunkPaletteData palette, ChunkOutputStream stream) throws IOException {
            var chunkPaintData = new ChunkPaintData(palette);
            chunkPaintData.writeToStream(value, stream);
        }
    });

    public static final ChunkSerializer<SkinPreviewData, ChunkGeometryData> SKIN_PREVIEW_DATA = register(new ChunkSerializer<>(ChunkType.PREVIEW_DATA) {

        @Override
        public SkinPreviewData read(ChunkInputStream stream, ChunkGeometryData geometryData) throws IOException {
            var chunkPreviewData = new ChunkPreviewData(geometryData);
            return chunkPreviewData.readFromStream(stream);
        }

        @Override
        public void write(SkinPreviewData previewData, ChunkGeometryData geometryData, ChunkOutputStream stream) throws IOException {
            var chunkPreviewData = new ChunkPreviewData(geometryData);
            chunkPreviewData.writeToStream(previewData, stream);
        }
    });

    public static final ChunkSerializer<ChunkPaletteData, Void> SKIN_TEXTURE_DATA = register(new ChunkSerializer<>(ChunkType.PALETTE) {

        @Override
        public ChunkPaletteData read(ChunkInputStream stream, Void obj) throws IOException {
            var palette = new ChunkPaletteData();
            palette.readFromStream(stream);
            return palette;
        }

        @Override
        public void write(ChunkPaletteData value, Void obj, ChunkOutputStream stream) throws IOException {
            stream.writeVariable(value);
        }
    });

    public static final ChunkSerializer<List<SkinAnimation>, Void> SKIN_ANIMATION_DATA = register(new ChunkSerializer<>(ChunkType.ANIMATION_DATA) {

        @Override
        public List<SkinAnimation> read(ChunkInputStream stream, Void obj) throws IOException {
            return ChunkAnimationData.readFromStream(stream);
        }

        @Override
        public void write(List<SkinAnimation> value, Void obj, ChunkOutputStream stream) throws IOException {
            ChunkAnimationData.writeToStream(value, stream);
        }
    });

    public static final ChunkSerializer<SkinProperties, Void> SKIN_PROPERTIES = register(new ChunkSerializer<>(ChunkType.PROPERTIES) {

        @Override
        public SkinProperties read(ChunkInputStream stream, Void obj) throws IOException {
            var properties = new SkinProperties();
            properties.readFromStream(stream);
            return properties;
        }

        @Override
        public void write(SkinProperties value, Void obj, ChunkOutputStream stream) throws IOException {
            value.writeToStream(stream);
        }

        @Override
        public SkinProperties getDefaultValue() {
            return SkinProperties.EMPTY;
        }
    });

    public static final ChunkSerializer<SkinSettings, Void> SKIN_SETTINGS = register(new ChunkSerializer<>(ChunkType.SKIN_SETTINGS) {

        @Override
        public void config() {
            // DEPRECATED: "3.0.0-beta.1"
            encoders.put("SET2", (stream, obj) -> {
                SkinItemTransforms itemTransforms = null;
                int size1 = stream.readVarInt();
                if (size1 != 0) {
                    itemTransforms = new SkinItemTransforms();
                    for (int i = 1; i < size1; ++i) {
                        var name = stream.readString();
                        var translate = stream.readVector3f();
                        var rotation = stream.readVector3f();
                        var scale = stream.readVector3f();
                        itemTransforms.put(name, OpenTransform3f.create(translate, rotation, scale));
                    }
                }
                boolean isEditable = stream.readBoolean();
                var settings = new SkinSettings();
                settings.setEditable(isEditable);
                settings.setItemTransforms(itemTransforms);
                return settings;
            });
            // DEPRECATED: "3.0.0-beta.14"
            encoders.put("SET3", (stream, obj) -> {
                SkinItemTransforms itemTransforms = null;
                int dataVersion = stream.readVarInt();
                int size1 = stream.readVarInt();
                if (size1 != 0) {
                    itemTransforms = new SkinItemTransforms();
                    for (int i = 1; i < size1; ++i) {
                        var name = stream.readString();
                        var translate = stream.readVector3f();
                        var rotation = stream.readVector3f();
                        var scale = stream.readVector3f();
                        itemTransforms.put(name, OpenTransform3f.create(translate, rotation, scale));
                    }
                }
                ArrayList<Rectangle3f> collisionBox = null;
                int size2 = stream.readVarInt();
                if (size2 != 0) {
                    collisionBox = new ArrayList<>();
                    for (int i = 1; i < size2; ++i) {
                        var rect = new Rectangle3f(stream.readRectangle3i());
                        collisionBox.add(rect);
                    }
                }
                boolean isEditable = stream.readBoolean();
                var settings = new SkinSettings();
                settings.setEditable(isEditable);
                settings.setItemTransforms(itemTransforms);
                settings.setCollisionBox(collisionBox);
                return settings;
            });
        }

        @Override
        public SkinSettings read(ChunkInputStream stream, Void obj) throws IOException {
            var settings = new SkinSettings();
            settings.readFromStream(stream);
            return settings;
        }

        @Override
        public void write(SkinSettings value, Void obj, ChunkOutputStream stream) throws IOException {
            value.writeToStream(stream);
        }

        @Override
        public SkinSettings getDefaultValue() {
            return SkinSettings.EMPTY;
        }
    });

    public static void writeToStream(Skin skin, IOutputStream stream, ChunkContext context) throws IOException {
        var stream1 = new ChunkOutputStream(context);
        SKIN.write(skin, null, stream1);
        stream1.transferTo(stream.getOutputStream());
    }

    public static Skin readFromStream(IInputStream stream, ChunkContext context) throws IOException {
        var stream1 = new ChunkInputStream(stream.getInputStream(), context, null);
        return SKIN.read(stream1, null);
    }

    public static IPair<ISkinType, ISkinProperties> readInfoFromStream(IInputStream stream, ChunkContext context) throws IOException {
        var allows = Collections.newList(ChunkType.PROPERTIES.getName(), ChunkType.SKIN_SETTINGS.getName());
        var stream1 = new ChunkInputStream(stream.getInputStream(), context, allows::contains);
        return SKIN_INFO.read(stream1, null);
    }

    private static <T, C> ChunkSerializer<T, C> register(ChunkSerializer<T, C> serializer) {
        return serializer;
    }
}
