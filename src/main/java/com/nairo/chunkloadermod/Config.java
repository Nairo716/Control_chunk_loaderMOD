package com.nairo.chunkloadermod;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String greeting = "Hello World";

    // チャンクローダーの読み込み範囲(ブロック単位の半径)。この範囲を含むチャンクがすべて強制ロードされます。
    public static int loadRadiusBlocks = 1;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        greeting = configuration.getString("greeting", Configuration.CATEGORY_GENERAL, greeting, "How shall I greet?");

        loadRadiusBlocks = configuration.getInt(
            "loadRadiusBlocks",
            "chunkloader",
            loadRadiusBlocks,
            0,
            128,
            "チャンクローダーの読み込み範囲(ブロック単位の半径)。設定ブロックからこの範囲を含むチャンクがすべて強制ロードされます。");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
