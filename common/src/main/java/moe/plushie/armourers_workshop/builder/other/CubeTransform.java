package moe.plushie.armourers_workshop.builder.other;

import moe.plushie.armourers_workshop.core.math.OpenMath;
import moe.plushie.armourers_workshop.core.math.OpenQuaternion3f;
import moe.plushie.armourers_workshop.core.math.Vector3i;
import moe.plushie.armourers_workshop.core.math.Vector4f;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;

public class CubeTransform {

    public final Level level;
    public final BlockPos blockPos;
    public final Direction direction;
    public final Rotation rotation;
    public final Rotation invRotation;
    public final OpenQuaternion3f rotationDegrees;

    public CubeTransform(Level level, BlockPos blockPos, Direction direction) {
        this.level = level;
        this.blockPos = blockPos;
        this.direction = direction;
        this.rotation = getRotation(direction, false);
        this.invRotation = getRotation(direction, true);
        this.rotationDegrees = getRotationDegrees(direction);
    }

    public static Rotation getRotation(Direction dir, boolean flags) {
        return switch (dir) {
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> flags ? Rotation.CLOCKWISE_90 : Rotation.COUNTERCLOCKWISE_90;
            case EAST -> flags ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public static OpenQuaternion3f getRotationDegrees(Direction dir) {
        return switch (dir) {
            case SOUTH -> new OpenQuaternion3f(0, 180, 0, true);
            case WEST -> new OpenQuaternion3f(0, 90, 0, true);
            case EAST -> new OpenQuaternion3f(0, -90, 0, true);
            default -> OpenQuaternion3f.ONE;
        };
    }

    public Direction rotate(Direction dir) {
        return rotation.rotate(dir);
    }

    public Direction invRotate(Direction dir) {
        return invRotation.rotate(dir);
    }

    public BlockPos mul(Vector3i pos) {
        return mul(pos.getX(), pos.getY(), pos.getZ());
    }

    public BlockPos mul(int x, int y, int z) {
        // in this case not need to apply matrix transform.
        if (rotationDegrees == OpenQuaternion3f.ONE) {
            return blockPos.offset(x, y, z);
        }
        // we increase 0.5 offset to avoid down-cast incorrect by float accuracy problems.
        var off = new Vector4f(x + 0.5f, y + 0.5f, z + 0.5f, 1);
        off.transform(rotationDegrees);
        return blockPos.offset(OpenMath.floori(off.getX()), OpenMath.floori(off.getY()), OpenMath.floori(off.getZ()));
    }

}
