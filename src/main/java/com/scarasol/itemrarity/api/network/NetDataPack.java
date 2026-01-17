package com.scarasol.itemrarity.api.network;

import net.minecraft.network.FriendlyByteBuf;

/**
 * 用于网络传输的数据类型
 * @author Scarasol
 */
public interface NetDataPack {

    /**
     * 将一般数据转成网络数据
     * @param buf 网络数据
     */
    void encode(FriendlyByteBuf buf);

    /**
     * 收到网络包后进行处理
     */
    void handle();
}
