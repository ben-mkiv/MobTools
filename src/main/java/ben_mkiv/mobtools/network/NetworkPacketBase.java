package ben_mkiv.mobtools.network;

import ben_mkiv.mobtools.MobTools;
import ben_mkiv.mobtools.network.MobSpawner.MobSpawner_NetworkMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class NetworkPacketBase {
    private static final SimpleChannel netHandler = createChannel(new ResourceLocation(MobTools.MOD_ID, "net"));
    private static int index = 0;

    protected static SimpleChannel createChannel(ResourceLocation name) {
        return NetworkRegistry.ChannelBuilder.named(name)
                .clientAcceptedVersions(getProtocolVersion()::equals)
                .serverAcceptedVersions(getProtocolVersion()::equals)
                .networkProtocolVersion(NetworkPacketBase::getProtocolVersion)
                .simpleChannel();
    }

    private static String getProtocolVersion() {
        return MobTools.VERSION;
    }

    public static <MSG> void sendToServer(MSG message) {
        netHandler.sendToServer(message);
    }

    public static void init() {
        registerClientToServer(MobSpawner_NetworkMessage.class, MobSpawner_NetworkMessage::encode, MobSpawner_NetworkMessage::decode, MobSpawner_NetworkMessage::handle);
    }

    protected static <MSG> void registerClientToServer(Class<MSG> type, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder,
                                                BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer) {
        netHandler.registerMessage(index++, type, encoder, decoder, consumer, Optional.of(NetworkDirection.PLAY_TO_SERVER));
    }

    protected static <MSG> void registerServerToClient(Class<MSG> type, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder,
                                                BiConsumer<MSG, Supplier<NetworkEvent.Context>> consumer) {
        netHandler.registerMessage(index++, type, encoder, decoder, consumer, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }


}
