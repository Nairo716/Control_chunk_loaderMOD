package com.nairo.chunkloadermod;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.ChunkCoordIntPair;

public enum RangeMode {

    SIZE_1("1×1", 0),
    SIZE_3("3×3", 1),
    SIZE_5("5×5", 2),
    SIZE_8("8×8", 4),
    SIZE_10("10×10", 5),
    ONE_CHUNK("1チャンク", -1); // -1は特別扱い:設置チャンクのみを直接指定

    public final String displayName;
    private final int blockRadius;

    RangeMode(String displayName, int blockRadius) {
        this.displayName = displayName;
        this.blockRadius = blockRadius;
    }

    /** ブロック座標(x, z)を中心にロードすべきチャンク一覧を返す */
    public List<ChunkCoordIntPair> getChunksToLoad(int x, int z) {
        List<ChunkCoordIntPair> list = new ArrayList<>();

        if (this == ONE_CHUNK) {
            list.add(new ChunkCoordIntPair(x >> 4, z >> 4));
            return list;
        }

        int minChunkX = (x - blockRadius) >> 4;
        int maxChunkX = (x + blockRadius) >> 4;
        int minChunkZ = (z - blockRadius) >> 4;
        int maxChunkZ = (z + blockRadius) >> 4;

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                list.add(new ChunkCoordIntPair(cx, cz));
            }
        }
        return list;
    }

    public static RangeMode byOrdinalSafe(int ordinal) {
        RangeMode[] values = values();
        if (ordinal < 0 || ordinal >= values.length) {
            return SIZE_1;
        }
        return values[ordinal];
    }
}
